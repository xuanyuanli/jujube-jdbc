package com.yfs.persistence.base.jpa.strategy;

import java.lang.reflect.Method;

import com.yfs.persistence.base.jpa.JpaQueryProxyDao;

public class NoRealizeQuery extends QueryStrategy {

    public NoRealizeQuery(JpaQueryProxyDao proxyDao) {
        super(proxyDao);
    }

    @Override
    boolean accept(Method method) {
        return true;
    }

    @Override
    Object query(Method method, Object[] args) {
        throw new RuntimeException("还未实现");
    }

}
