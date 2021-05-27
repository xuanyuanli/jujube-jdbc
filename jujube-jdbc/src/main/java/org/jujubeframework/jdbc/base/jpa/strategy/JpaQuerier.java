package org.jujubeframework.jdbc.base.jpa.strategy;

import org.jujubeframework.jdbc.base.jpa.JpaBaseDaoSupport;
import org.jujubeframework.jdbc.base.jpa.event.JpaQueryPreEvent;
import org.jujubeframework.jdbc.spring.SpringContextHolder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Query Context
 *
 * @author John Li
 */
public class JpaQuerier {
    private static final List<BaseQueryStrategy> STRATEGIES = new ArrayList<>();

    static {
        // Query类必须为线程安全的，也就是无状态的
        STRATEGIES.add(new GetCountByAnyQuery());
        STRATEGIES.add(new GetSumOfByAnyQuery());
        STRATEGIES.add(new FindAllQuery());
        STRATEGIES.add(new FindAnyByIdQuery());
        STRATEGIES.add(new FindByAnyQuery());
        STRATEGIES.add(new FindAnyByAnyQuery());
    }

    public static Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        for (BaseQueryStrategy strategy : STRATEGIES) {
            if (strategy.accept(method.getName())) {
                if (args == null) {
                    args = new Object[0];
                }
                SpringContextHolder.getApplicationContext().publishEvent(new JpaQueryPreEvent(method, args));
                return strategy.query(proxyDao, method, args);
            }
        }
        return null;
    }

    public static List<BaseQueryStrategy> getStrategies() {
        return STRATEGIES;
    }

}
