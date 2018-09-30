package org.jujubeframework.jdbc.binding;

import org.jujubeframework.jdbc.base.BaseDaoSupport;

import java.lang.reflect.Proxy;

/**
 * Dao代理类工厂
 *
 * @author John Li
 */
public class DaoProxyFactory<T> {
    private final Class<T> daoInterface;

    public DaoProxyFactory(Class<T> daoInterface) {
        this.daoInterface = daoInterface;
    }

    protected T newInstance(DaoProxy<T> mapperProxy) {
        return (T) Proxy.newProxyInstance(daoInterface.getClassLoader(), new Class[]{daoInterface}, mapperProxy);
    }

    public T newInstance(BaseDaoSupport<?,?> baseDaoSupport) {
        final DaoProxy<T> mapperProxy = new DaoProxy<T>(daoInterface, baseDaoSupport);
        return newInstance(mapperProxy);
    }
}
