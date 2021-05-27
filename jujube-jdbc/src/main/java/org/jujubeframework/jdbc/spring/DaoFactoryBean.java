package org.jujubeframework.jdbc.spring;

import java.lang.reflect.Proxy;

import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.binding.DaoProxy;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author John Li
 */
public class DaoFactoryBean<T extends BaseDao> implements FactoryBean<T> {

    private Class<T> daoInterfaceClass;

    @Override
    public T getObject() {
        DaoProxy<T> mapperProxy = new DaoProxy<>(daoInterfaceClass);
        T t = (T) Proxy.newProxyInstance(daoInterfaceClass.getClassLoader(), new Class[] {daoInterfaceClass}, mapperProxy);
        ProxyBeanContext.setCurrentProxy(daoInterfaceClass, t);
        return t;
    }

    @Override
    public Class<?> getObjectType() {
        return daoInterfaceClass;
    }

    public void setDaoInterfaceClass(Class<T> daoInterfaceClass) {
        this.daoInterfaceClass = daoInterfaceClass;
    }

}
