package org.jujubeframework.jdbc.spring;

import org.apache.poi.ss.formula.functions.T;
import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.base.BaseDaoSupport;
import org.jujubeframework.jdbc.binding.DaoProxyFactory;
import org.jujubeframework.util.Beans;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author John Li
 */
public class DaoFactoryBean<T> implements FactoryBean<T> {

    private Class<T> daoInterface;

    @Override
    public T getObject() throws Exception {
        DaoProxyFactory<T> daoProxyFactory = new DaoProxyFactory<>(daoInterface);
        Class<?> realGenericType = Beans.getClassGenericType(daoInterface, 0);
        Class<?> realPrimayKeyType = Beans.getClassGenericType(daoInterface, 1);
        String tableName = (String)Beans.invokeInterfaceDefault(Beans.getDeclaredMethod(daoInterface, "getTableName"));
        BaseDaoSupport<?,?> baseDaoSupport = new BaseDaoSupport(realGenericType,realPrimayKeyType,tableName);
        return daoProxyFactory.newInstance(baseDaoSupport);
    }

    @Override
    public Class<?> getObjectType() {
        return daoInterface;
    }

    public void setDaoInterface(Class<T> daoInterface) {
        this.daoInterface = daoInterface;
    }

}
