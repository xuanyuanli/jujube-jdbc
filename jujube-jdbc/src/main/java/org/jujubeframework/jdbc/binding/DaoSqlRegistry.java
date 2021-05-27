package org.jujubeframework.jdbc.binding;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jujubeframework.constant.Charsets;
import org.jujubeframework.exception.DaoInitializeException;
import org.jujubeframework.exception.DaoQueryException;
import org.jujubeframework.exception.RepeatException;
import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.base.BaseDaoSupport;
import org.jujubeframework.jdbc.base.SqlQueryPostHandler;
import org.jujubeframework.jdbc.base.jpa.strategy.JpaQuerier;
import org.jujubeframework.jdbc.base.util.JdbcPojos;
import org.jujubeframework.jdbc.support.pagination.Pageable;
import org.jujubeframework.lang.Record;
import org.jujubeframework.util.*;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * Dao与Sql之间对应关系的注册器
 *
 * @author John Li
 */
@Slf4j
public class DaoSqlRegistry {
    private static final Set<SqlQueryPostHandler> SQL_QUERY_POST_HANDLERS = new HashSet<>();

    /** 模板方法支持的返回类型 */
    private final static List<Class<?>> METHOD_SQL_ALLOW_RETURN_TYPE = Dynamics.listOf(Record.class, Pageable.class, List.class, Double.class, Float.class, String.class,
            Integer.class, Long.class, BigDecimal.class, double.class, float.class, int.class, long.class);

    /** JPA方法支持的返回类型 */
    private final static List<Class<?>> JPA_METHOD_ALLOW_RETURN_TYPE = Dynamics.listOf(Double.class, Float.class, String.class, Integer.class, Long.class, BigDecimal.class,
            double.class, float.class, int.class, long.class);

    private static final ConcurrentMap<String, String> DB_COLUMN_CACHE = new ConcurrentHashMap<>();

    /**
     * Dao接口所在的package
     */
    private static String basePackage;

    /**
     * Dao Sql所在的package。如果不设置，默认为{#basePackage}.sql
     */
    private static String sqlBasePackage;

    private static int methodSize;

    /** 方法与Sql语句的对应数据 */
    private static final Map<Method, SqlBuilder> METHOD_SQL_DATA = new HashMap(16);

    /** 初始化Dao方法与SqlBuilder的对应关系 */
    public static void init() {
        // 获得basePackage下的所有Class，并遍历
        List<Class<?>> packageClasses = Resources.getPackageClasses(basePackage);
        Stream<Class<?>> classStream = packageClasses.stream().filter(cl -> cl.isInterface() && BaseDao.class.isAssignableFrom(cl));
        Map<String, List<String>> methodSql = getMethodSql();
        classStream.forEach(daoClass -> {
            // 缓存热启动
            JdbcPojos.getFieldColumns(Beans.getClassGenericType(daoClass));
            // 获得所有方法，并遍历
            for (Method declaredMethod : daoClass.getDeclaredMethods()) {
                if (Modifier.isStatic(declaredMethod.getModifiers()) || declaredMethod.isDefault()) {
                    continue;
                }
                boolean errMethod = true;
                boolean jpaMethod = isJpaMethod(declaredMethod.getName());
                boolean baseDaoSupportMethod = isBaseDaoSupportMethod(declaredMethod);
                if (jpaMethod) {
                    validateJpaMethod(declaredMethod);
                    errMethod = false;
                } else if (!baseDaoSupportMethod) {
                    String key = daoClass.getSimpleName() + "." + declaredMethod.getName();
                    List<String> sql = methodSql.get(key);
                    if (sql == null) {
                        throw new DaoInitializeException(daoClass.getName() + "." + declaredMethod.getName() + "()方法没有找到对应的Sql语句");
                    }
                    METHOD_SQL_DATA.put(declaredMethod, new SqlBuilder(sql));
                    errMethod = false;
                }
                if (errMethod) {
                    throw new DaoInitializeException(daoClass.getName() + "." + declaredMethod.getName() + "()方法非JPA方法也非SQL方法");
                }
            }
        });
        validateMethodSql();
        if (methodSize != METHOD_SQL_DATA.size()) {
            log.info("DaoSqlRegistry initialize the {} Method", METHOD_SQL_DATA.size());
            methodSize = METHOD_SQL_DATA.size();
        }
        // 注册SQL查询后置处理器
        Resource[] resources = Resources.getClassPathAllResources("META-INF/jujube/org.jujubeframework.jdbc.base.SqlQueryPostHandler");
        if (resources != null) {
            for (Resource resource : resources) {
                try {
                    String classPath = IOUtils.toString(resource.getInputStream(), Charsets.UTF_8);
                    String[] arr = classPath.split("\n");
                    if (arr != null) {
                        for (String line : arr) {
                            line = line.trim();
                            Class<SqlQueryPostHandler> sqlQueryPostHandlerClass = (Class<SqlQueryPostHandler>) Beans.getDefaultClassLoader().loadClass(line);
                            if (SQL_QUERY_POST_HANDLERS.stream().noneMatch(e -> e.getClass().equals(sqlQueryPostHandlerClass))) {
                                SQL_QUERY_POST_HANDLERS.add(Beans.getInstance(sqlQueryPostHandlerClass));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("读取后置处理器失败", e);
                }
            }
        }
    }

    /** 验证Jpa方法的正确性 */
    private static void validateJpaMethod(Method declaredMethod) {
        String methodName = declaredMethod.getName();
        Class<Object> classGenericType = Beans.getClassGenericType(declaredMethod.getDeclaringClass());
        // 验证返回值的正确性
        Class<?> returnType = declaredMethod.getReturnType();
        if (returnType.isAssignableFrom(List.class)) {
            Type[] params = ((ParameterizedType) declaredMethod.getGenericReturnType()).getActualTypeArguments();
            if (params.length > 0) {
                List<Class<?>> returnTypes = new ArrayList<>(JPA_METHOD_ALLOW_RETURN_TYPE);
                returnTypes.add(classGenericType);
                if (returnTypes.stream().noneMatch(t -> t.equals(params[0]))) {
                    throw new DaoInitializeException(Texts.format("{}.{}方法的返回值有误", declaredMethod.getDeclaringClass().getName(), methodName));
                }
            }
        } else if (returnType.equals(classGenericType)) {
            // ignore
        } else if (JPA_METHOD_ALLOW_RETURN_TYPE.stream().noneMatch(t -> t.equals(returnType))) {
            throw new DaoInitializeException(Texts.format("{}.{}方法的返回值有误", declaredMethod.getDeclaringClass().getName(), methodName));
        }
    }

    /** 验证模板方法的正确性 */
    private static void validateMethodSql() {
        for (Method method : DaoSqlRegistry.METHOD_SQL_DATA.keySet()) {
            // 验证支持的返回类型
            Class<?> returnType = method.getReturnType();
            if (!METHOD_SQL_ALLOW_RETURN_TYPE.contains(returnType)) {
                throw new DaoInitializeException(Texts.format("{}.{}方法的返回类型有误", method.getDeclaringClass().getName(), method.getName()));
            }
            if (returnType.equals(List.class)) {
                ParameterizedType genericReturnType = (ParameterizedType) method.getGenericReturnType();
                try {
                    Class<?> actualTypeArgument = (Class<?>) genericReturnType.getActualTypeArguments()[0];
                    if (JPA_METHOD_ALLOW_RETURN_TYPE.stream().noneMatch(t -> t.equals(actualTypeArgument)) && !Record.class.isAssignableFrom(actualTypeArgument)) {
                        throw new DaoInitializeException(Texts.format("{}.{}方法的返回类型有误", method.getDeclaringClass().getName(), method.getName()));
                    }
                } catch (ClassCastException e) {
                    throw new DaoInitializeException(Texts.format("{}.{}方法的返回类型有误", method.getDeclaringClass().getName(), method.getName()), e);
                }
            }
            // 只对非分页方法进行验证，因为分页方法的Map形参是动态的
            if (!returnType.equals(Pageable.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                String[] methodParamNames = Beans.getMethodParamNames(method);
                SqlBuilder sqlBuilder = DaoSqlRegistry.METHOD_SQL_DATA.get(method);
                Map map = new HashMap(parameterTypes.length);
                for (int i = 0; i < parameterTypes.length; i++) {
                    Object value = DataGenerator.generateRandomValueByParamType(parameterTypes[i]);
                    if (value != null) {
                        map.put(methodParamNames[i], value);
                    }
                }
                try {
                    if (map.size() == parameterTypes.length) {
                        sqlBuilder.builder(map);
                    }
                } catch (Exception e) {
                    throw new DaoInitializeException(Texts.format("{}.{}方法对应的Sql模板错误，请检查", method.getDeclaringClass().getName(), method.getName()), e);
                }
            }
        }
    }

    /** 方法是否需要验证 */
    private static boolean baseNeedValidate(Method method) {
        return !Modifier.isStatic(method.getModifiers()) && !method.isDefault();
    }

    /**
     * 是否是BaseDaoSupport中的方法
     */
    private static boolean isBaseDaoSupportMethod(Method method) {
        return Beans.getSelfDeclaredMethod(BaseDaoSupport.class, method.getName(), method.getParameterTypes()) != null;
    }

    /** 是否是Jpa方法 */
    public static boolean isJpaMethod(String methodName) {
        return JpaQuerier.getStrategies().stream().anyMatch(e -> e.accept(methodName));
    }

    /**
     * 获得方法名与sql的对应关系
     */
    private static Map<String, List<String>> getMethodSql() {
        Resource[] sqlResources = Resources.getClassPathAllResources(ClassUtils.convertClassNameToResourcePath(sqlBasePackage) + "/**/*.sql");
        Map<String, List<String>> result = new HashMap<>(sqlResources.length * 2);
        for (Resource sqlResource : sqlResources) {
            try (InputStream inputStream = sqlResource.getInputStream()) {
                String filename = sqlResource.getFilename();
                filename = filename.substring(0, filename.length() - 4);
                List<String> lines = IOUtils.readLines(inputStream, Charsets.UTF_8.name());
                Map<String, List<String>> group;
                try {
                    group = Texts.group(lines, t -> t.startsWith("##") ? t.substring(2).trim() : "", false);
                } catch (RepeatException e) {
                    throw new RepeatException("同一个Dao Sql中不允许存在同名方法：" + filename + "." + e.getMessage());
                }
                for (String key : group.keySet()) {
                    String key1 = filename + "." + key;
                    result.put(key1, group.get(key));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public static void setBasePackage(String basePackage) {
        DaoSqlRegistry.basePackage = basePackage;
    }

    public static void setSqlBasePackage(String sqlBasePackage) {
        DaoSqlRegistry.sqlBasePackage = sqlBasePackage;
    }

    /**
     * 获得sql构建器
     */
    public static SqlBuilder getSqlBuilder(Method method) {
        return METHOD_SQL_DATA.get(method);
    }

    /**
     * 获得数据库表列字段
     *
     * @param daoClass
     *            Dao Class
     * @param entityFieldName
     *            Entity Class Field name
     * @return 数据库字段
     */
    public static String getDbColumnName(Class<? extends BaseDao> daoClass, String entityFieldName) {
        return DB_COLUMN_CACHE.computeIfAbsent(daoClass.getName() + "#" + entityFieldName, (key) -> {
            String curQueryField = StringUtils.uncapitalize(entityFieldName);
            List<JdbcPojos.FieldColumn> fieldColumns = JdbcPojos.getFieldColumns(Beans.getClassGenericType(daoClass));
            Optional<JdbcPojos.FieldColumn> first = fieldColumns.stream().filter(fc -> fc.getField().equals(curQueryField)).findFirst();
            if (first.isPresent()) {
                return first.get().getColumn();
            } else {
                throw new DaoQueryException("获取对应字段出错");
            }
        });
    }

    public static Set<SqlQueryPostHandler> getSqlQueryPostHandlers() {
        return SQL_QUERY_POST_HANDLERS;
    }

}
