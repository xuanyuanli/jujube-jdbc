package org.jujubeframework.jdbc.binding;

import org.jujubeframework.jdbc.base.BaseDaoSupport;
import org.jujubeframework.jdbc.base.jpa.JpaBaseDaoSupport;
import org.jujubeframework.jdbc.base.jpa.JpaQueryProxyDaoHolder;
import org.jujubeframework.jdbc.spring.SpringContextHolder;
import org.jujubeframework.jdbc.support.entity.RecordEntity;
import org.jujubeframework.util.Beans;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author John Li
 */
public class DaoProxy<T> implements InvocationHandler {
    private static final String FIND = "find";
    private static final String GET_COUNT = "getCount";

    private static final ConcurrentMap<String, JpaBaseDaoSupport> JPA_BASEDAO_CACHE = new ConcurrentHashMap<>(16);

    private final Class<T> daoInterface;
    private final BaseDaoSupport<?, ?> baseDaoSupport;

    public DaoProxy(Class<T> daoInterface, BaseDaoSupport<?, ?> baseDaoSupport) {
        this.daoInterface = daoInterface;
        this.baseDaoSupport = baseDaoSupport;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //先看方法是否在BaseDaoSupport中，如果在，则直接调用
        Method declaredMethod = Beans.getSelfDeclaredMethod(baseDaoSupport.getClass(), method.getName(), method.getParameterTypes());
        if (declaredMethod != null) {
            setBaseDaoSupportJdbcTemplate(baseDaoSupport);
            return Beans.invoke(declaredMethod, baseDaoSupport, args);
        } else if (method.getName().startsWith(FIND) || method.getName().startsWith(GET_COUNT)) {
            //如果以find开头，则属于jpa查询，调用JpaQueryProxyDao
            JpaBaseDaoSupport recordEntityBaseDaoSupport = getJpaBaseDaoFromCache(daoInterface.getName());
            return JpaQueryProxyDaoHolder.getQuerier().query(recordEntityBaseDaoSupport, method, args);
        } else {
            //以上两种情况都不符合，则属于sql查询，关联sql文件进行查询
            return null;
        }
    }

    private JpaBaseDaoSupport getJpaBaseDaoFromCache(String name) {
        if (JPA_BASEDAO_CACHE.containsKey(name)) {
            return JPA_BASEDAO_CACHE.get(name);
        } else {
            JpaBaseDaoSupport jpaBaseDaoSupport = new JpaBaseDaoSupport(this.baseDaoSupport.getRealGenericType(), this.baseDaoSupport.getRealPrimayKeyType(), this.baseDaoSupport.getTableName());
            setBaseDaoSupportJdbcTemplate(jpaBaseDaoSupport);
            JPA_BASEDAO_CACHE.put(name, jpaBaseDaoSupport);
            return jpaBaseDaoSupport;
        }
    }

    /**
     * Spring所有Bean默认都是单例，所以不存在性能问题，不用做缓存
     * @param baseDaoSupport
     */
    private void setBaseDaoSupportJdbcTemplate(BaseDaoSupport<?, ?> baseDaoSupport) {
        if (baseDaoSupport.getJdbcTemplate() == null) {
            baseDaoSupport.setJdbcTemplate(SpringContextHolder.getApplicationContext().getBean(JdbcTemplate.class));
        }
    }
}
