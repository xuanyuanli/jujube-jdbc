package org.jujubeframework.jdbc.persistence.base.jpa;

import org.jujubeframework.jdbc.persistence.base.BaseDaoSupport;
import org.jujubeframework.jdbc.persistence.base.spec.Spec;
import org.jujubeframework.jdbc.support.entity.RecordEntity;
import org.jujubeframework.util.Beans;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Jpa查询的代理Dao
 *
 * @author John Li
 */
public class JpaQueryProxyDao extends BaseDaoSupport<RecordEntity> {

    private BaseDaoSupport<?> target;
    private Method curMethod;
    private Object[] methodArgs;

    public JpaQueryProxyDao(BaseDaoSupport<?> target, Method curMethod, Object[] methodArgs) {
        super();
        this.target = target;
        this.curMethod = curMethod;
        this.methodArgs = methodArgs;
        JpaQueryProxyDaoHolder.setJpaQueryProxyDao(this);
    }

    @Override
    public String getTableName() {
        Method tmethod = Beans.getDeclaredMethod(target.getClass(), "getTableName");
        if (!tmethod.isAccessible()) {
            tmethod.setAccessible(true);
        }
        return (String) Beans.invoke(tmethod, target);
    }

    /**
     * 这个类（JpaQueryProxyDao不在spring容器中，所以无法注入JdbcTemplate。这里重写getJdbcTemplate方法，以获取真实的JdbcTemplate）
     */
    @Override
    public JdbcTemplate getJdbcTemplate() {
        return target.getJdbcTemplate();
    }

    public Object jpaQuery() {
        return JpaQueryProxyDaoHolder.getQuerier().query(curMethod, methodArgs);
    }

    /**
     * 获得真实Dao的泛型
     */
    public Class<?> getClazz() {
        return target.getRealGenericType();
    }

    @Override
    public RecordEntity findOne(Spec spec) {
        return super.findOne(spec);
    }

    @Override
    public List<RecordEntity> find(Spec spec) {
        return super.find(spec);
    }

    @Override
    public RecordEntity findOne(String fields, Spec spec) {
        return super.findOne(fields, spec);
    }

    @Override
    public List<RecordEntity> find(String fields, Spec spec) {
        return super.find(fields, spec);
    }

    @Override
    public long getCount(Spec spec) {
        return super.getCount(spec);
    }
}
