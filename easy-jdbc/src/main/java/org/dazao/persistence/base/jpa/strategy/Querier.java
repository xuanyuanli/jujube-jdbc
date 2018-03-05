package org.dazao.persistence.base.jpa.strategy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Query Context */
public class Querier {
    private List<QueryStrategy> strategies = new ArrayList<>();

    public Object query(Method method, Object[] args) {
        for (QueryStrategy strategy : strategies) {
            if (strategy.accept(method)) {
                return strategy.query(method, args);
            }
        }
        return null;
    }

    public void addStrategy(QueryStrategy strategy) {
        strategies.add(strategy);
    }
}
