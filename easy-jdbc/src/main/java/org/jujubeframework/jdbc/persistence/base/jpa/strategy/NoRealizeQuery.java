package org.jujubeframework.jdbc.persistence.base.jpa.strategy;

import java.lang.reflect.Method;


/**
 * @author John Li
 */
public class NoRealizeQuery extends BaseQueryStrategy {

    @Override
    boolean accept(Method method) {
        return true;
    }

    @Override
    Object query(Method method, Object[] args) {
        throw new RuntimeException("还未实现");
    }

}
