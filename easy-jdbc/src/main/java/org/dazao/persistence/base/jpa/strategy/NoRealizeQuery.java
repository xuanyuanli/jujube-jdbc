package org.dazao.persistence.base.jpa.strategy;

import java.lang.reflect.Method;

public class NoRealizeQuery extends QueryStrategy {

    @Override
    boolean accept(Method method) {
        return true;
    }

    @Override
    Object query(Method method, Object[] args) {
        throw new RuntimeException("还未实现");
    }

}
