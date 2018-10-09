package org.jujubeframework.jdbc.base.jpa.strategy;

import org.jujubeframework.jdbc.base.jpa.JpaBaseDaoSupport;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Query Context
 *
 * @author John Li
 */
public class Querier {
    private List<BaseQueryStrategy> strategies = new ArrayList<>();

    public Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        for (BaseQueryStrategy strategy : strategies) {
            if (strategy.accept(method)) {
                strategy.setProxyDao(proxyDao);
                Object query = strategy.query(method, args);
                return query;
            }
        }
        return null;
    }

    public void addStrategy(BaseQueryStrategy strategy) {
        strategies.add(strategy);
    }
}
