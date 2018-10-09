package org.jujubeframework.jdbc.spring;

import org.jujubeframework.jdbc.base.BaseDaoSupport;
import org.jujubeframework.jdbc.binding.DaoProxyFactory;
import org.jujubeframework.util.Beans;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;

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
        Method getTableNameMethod = Beans.getDeclaredMethod(daoInterface, "getTableName");
        if (getTableNameMethod == null) {
            throw new RuntimeException("Dao必须用default覆写getTableName方法");
        }
        String tableName = (String) Beans.invokeInterfaceDefault(getTableNameMethod);
        BaseDaoSupport<?, ?> baseDaoSupport = new BaseDaoSupport(realGenericType, realPrimayKeyType, tableName);
        Method getPrimayKeyNameMethod = Beans.getDeclaredMethod(daoInterface, "getPrimayKeyName");
        if (getPrimayKeyNameMethod != null) {
            String primaryKeyName = (String) Beans.invokeInterfaceDefault(getPrimayKeyNameMethod);
            baseDaoSupport.setPrimaryKeyName(primaryKeyName);
        }
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
