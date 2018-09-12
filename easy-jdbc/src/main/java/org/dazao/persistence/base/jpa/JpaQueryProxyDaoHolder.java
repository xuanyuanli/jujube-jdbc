package org.dazao.persistence.base.jpa;

import org.dazao.persistence.base.jpa.strategy.*;

/**jpa query代理类的上下文保存者*/
public abstract class JpaQueryProxyDaoHolder {
    private static final ThreadLocal<JpaQueryProxyDao> HOLDER = new ThreadLocal<>();

    public static void setJpaQueryProxyDao(JpaQueryProxyDao target) {
        HOLDER.set(target);
    }

    public static JpaQueryProxyDao getJpaQueryProxyDao() {
        return HOLDER.get();
    }

    /**
     * 查询上下文
     */
    private static final Querier QUERIER = new Querier();

    static {
        QUERIER.addStrategy(new FindAnyByIdQuery());
        QUERIER.addStrategy(new FindByAnyQuery());
        QUERIER.addStrategy(new FindAnyByAnyQuery());
        QUERIER.addStrategy(new GetCountByAnyQuery());
        QUERIER.addStrategy(new NoRealizeQuery());
    }

    public static Querier getQuerier() {
        return QUERIER;
    }
}
