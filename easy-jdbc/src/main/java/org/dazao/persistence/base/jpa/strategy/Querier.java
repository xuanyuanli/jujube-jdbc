package org.dazao.persistence.base.jpa.strategy;

import org.dazao.persistence.base.jpa.JpaQueryProxyDaoHolder;

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

    public Object query(Method method, Object[] args) {
        for (BaseQueryStrategy strategy : strategies) {
            if (strategy.accept(method)) {
                strategy.setProxyDao(JpaQueryProxyDaoHolder.getJpaQueryProxyDao());
                Object query = strategy.query(method, args);
                JpaQueryProxyDaoHolder.remove();
                return query;
            }
        }
        return null;
    }

    public void addStrategy(BaseQueryStrategy strategy) {
        strategies.add(strategy);
    }
}
