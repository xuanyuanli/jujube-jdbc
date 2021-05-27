package org.jujubeframework.jdbc.binding;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jujubeframework.exception.DaoProxyException;
import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.base.BaseDaoSupport;
import org.jujubeframework.jdbc.base.jpa.JpaBaseDaoSupport;
import org.jujubeframework.jdbc.base.jpa.strategy.JpaQuerier;
import org.jujubeframework.jdbc.spring.SpringContextHolder;
import org.jujubeframework.jdbc.support.pagination.Pageable;
import org.jujubeframework.jdbc.support.pagination.PageableRequest;
import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Beans;
import org.jujubeframework.util.Exceptions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StopWatch;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Dao接口代理类
 *
 * @author John Li
 */
@Slf4j
public class DaoProxy<T extends BaseDao> implements InvocationHandler {

    private static final ConcurrentMap<Class, JpaBaseDaoSupport> JPA_BASEDAO_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Class, BaseDaoSupport> BASEDAO_CACHE = new ConcurrentHashMap<>();

    private final Class<T> daoInterfaceClass;

    public DaoProxy(Class<T> daoInterfaceClass) {
        this.daoInterfaceClass = daoInterfaceClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Class<?> declaringClass = method.getDeclaringClass();
        try {
            if (method.isDefault()) {
                return Beans.invokeDefaultMethod(proxy, method, args);
            }
            BaseDaoSupport baseDaoSupport = getBaseDaoSupport(daoInterfaceClass);
            // 先看方法是否在BaseDaoSupport中，如果在，则直接调用
            Method declaredMethod = Beans.getSelfDeclaredMethod(baseDaoSupport.getClass(), method.getName(), method.getParameterTypes());
            if (declaredMethod != null) {
                return Beans.invoke(declaredMethod, baseDaoSupport, args);
            } else if (DaoSqlRegistry.isJpaMethod(method.getName())) {
                // 如果以find开头，则属于jpa查询，调用JpaQueryProxyDao
                JpaBaseDaoSupport recordEntityBaseDaoSupport = getJpaBaseDao(daoInterfaceClass);
                return JpaQuerier.query(recordEntityBaseDaoSupport, method, args);
            } else {
                // 以上两种情况都不符合，则属于sql查询，关联sql文件进行查询
                return sqlQuery(baseDaoSupport, method, args);
            }
        } catch (Exception e) {
            String builder = "Proxy class:" + declaringClass.getName() + ",method:" + (method == null ? null : method.getName()) + ",args:" + StringUtils.join(args, ",")
                    + ",error:" + Exceptions.exceptionToString(e);
            throw new DaoProxyException(builder);
        } finally {
            if (log.isDebugEnabled()) {
                stopWatch.stop();
                log.debug("{},执行时间：{}ms", declaringClass.getSimpleName() + "." + method.getName(), stopWatch.getLastTaskTimeMillis());
            }
        }
    }

    /** sql查询 */
    private Object sqlQuery(BaseDaoSupport baseDaoSupport, Method method, Object[] args) {
        SqlBuilder sqlBuilder = DaoSqlRegistry.getSqlBuilder(method);
        // 分页
        if (method.getReturnType().equals(Pageable.class)) {
            PageableRequest pageableRequest = Beans.getObjcetFromMethodArgs(args, PageableRequest.class);
            Map queryMap = Beans.getObjcetFromMethodArgs(args, Map.class);
            SqlBuilder.SqlResult sqlResult = sqlBuilder.builder(queryMap);
            if (sqlResult.isUnion()) {
                return baseDaoSupport.paginationBySqlOfUnion(pageableRequest, sqlResult.getSql(), sqlResult.getFilterParams(), sqlResult.getUnionAfterSqlInfo());
            } else {
                return baseDaoSupport.paginationBySql(sqlResult.getSql(), pageableRequest, sqlResult.getFilterParams());
            }
        }
        // 普通查询
        else {
            Map<String, Object> queryMap = Beans.getFormalParamSimpleMapping(method, args);
            SqlBuilder.SqlResult sqlResult = sqlBuilder.builder(queryMap);
            if (method.getReturnType().equals(List.class)) {
                List<Record> list = baseDaoSupport.findRecord(sqlResult.getSql(), sqlResult.getFilterParams());
                ParameterizedType genericReturnType = (ParameterizedType) method.getGenericReturnType();
                Type[] actualTypeArguments = genericReturnType.getActualTypeArguments();
                Class<?> actualListType = Record.class;
                if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                    actualListType = (Class<?>) actualTypeArguments[0];
                }
                if (actualListType.equals(Record.class)) {
                    return list;
                } else {
                    Class<?> finalActualListType = actualListType;
                    return list.stream().map(r -> Beans.getExpectTypeValue(r.values().iterator().next(), finalActualListType)).collect(Collectors.toList());
                }
            } else if (method.getReturnType().equals(Record.class)) {
                return baseDaoSupport.findRecordOne(sqlResult.getSql(), sqlResult.getFilterParams());
            } else {
                Record one = baseDaoSupport.findRecordOne(sqlResult.getSql(), sqlResult.getFilterParams());
                Object firstVal = one != null ? new ArrayList<>(one.values()).get(0) : null;
                if (method.getReturnType().equals(int.class) || method.getReturnType().equals(Integer.class)) {
                    return NumberUtils.toInt(String.valueOf(firstVal));
                } else if (method.getReturnType().equals(long.class) || method.getReturnType().equals(Long.class)) {
                    return NumberUtils.toLong(String.valueOf(firstVal));
                } else if (method.getReturnType().equals(double.class) || method.getReturnType().equals(Double.class)) {
                    return NumberUtils.toDouble(String.valueOf(firstVal));
                } else if (method.getReturnType().equals(String.class)) {
                    if (firstVal == null || String.class.equals(firstVal.getClass())) {
                        return firstVal;
                    } else {
                        return String.valueOf(firstVal);
                    }
                } else {
                    return one;
                }
            }
        }
    }

    /**
     * 缓存中获取Dao class对应的DaoSupport
     */
    private static JpaBaseDaoSupport getJpaBaseDao(Class<? extends BaseDao> daoInterfaceClass) {
        if (JPA_BASEDAO_CACHE.containsKey(daoInterfaceClass)) {
            return JPA_BASEDAO_CACHE.get(daoInterfaceClass).cloneSele();
        } else {
            BaseDaoSupport baseDaoSupport = getBaseDaoSupport(daoInterfaceClass);
            JpaBaseDaoSupport jpaBaseDaoSupport = new JpaBaseDaoSupport(baseDaoSupport.getRealGenericType(), baseDaoSupport.getRealPrimayKeyType(), baseDaoSupport.getTableName());
            jpaBaseDaoSupport.setPrimaryKeyName(baseDaoSupport.getPrimayKeyName());
            jpaBaseDaoSupport.setJdbcTemplate(baseDaoSupport.getJdbcTemplate());
            JPA_BASEDAO_CACHE.put(daoInterfaceClass, jpaBaseDaoSupport);
            return jpaBaseDaoSupport;
        }
    }

    private static BaseDaoSupport getBaseDaoSupport(Class<? extends BaseDao> daoInterfaceClass) {
        if (BASEDAO_CACHE.containsKey(daoInterfaceClass)) {
            // 每次都返回新对象，防止并发bug
            return BASEDAO_CACHE.get(daoInterfaceClass).cloneSelf();
        } else {
            Class<?> realGenericType = Beans.getClassGenericType(daoInterfaceClass, 0);
            Class<?> realPrimayKeyType = Beans.getClassGenericType(daoInterfaceClass, 1);
            Method getTableNameMethod = Beans.getDeclaredMethod(daoInterfaceClass, "getTableName");
            if (getTableNameMethod == null) {
                throw new RuntimeException("Dao必须用default覆写getTableName方法");
            }
            String tableName = (String) Beans.invokeDefaultMethod(getTableNameMethod);
            BaseDaoSupport baseDaoSupport = new BaseDaoSupport(realGenericType, realPrimayKeyType, tableName);
            Method getPrimayKeyNameMethod = Beans.getDeclaredMethod(daoInterfaceClass, "getPrimayKeyName");
            if (getPrimayKeyNameMethod != null) {
                String primaryKeyName = (String) Beans.invokeDefaultMethod(getPrimayKeyNameMethod);
                baseDaoSupport.setPrimaryKeyName(primaryKeyName);
            }
            baseDaoSupport.setJdbcTemplate(SpringContextHolder.getApplicationContext().getBean(JdbcTemplate.class));
            BASEDAO_CACHE.put(daoInterfaceClass, baseDaoSupport);
            return baseDaoSupport;
        }
    }
}
