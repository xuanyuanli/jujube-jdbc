package com.yfs.persistence.base.jpa.strategy;

import java.lang.reflect.Method;

import com.yfs.persistence.base.jpa.JpaQueryProxyDao;

/** 查询策略 */
public abstract class QueryStrategy {
    static final String EMPTY = "";
    static final String FIND = "find";

    protected JpaQueryProxyDao proxyDao;

    public QueryStrategy(JpaQueryProxyDao proxyDao) {
        super();
        this.proxyDao = proxyDao;
    }

    /** 是否承认 */
    abstract boolean accept(Method method);

    /** 执行查询 */
    abstract Object query(Method method, Object[] args);

    /** 需要用``来包含字段 */
    protected String buildQueryField(String field) {
        return "`" + field + "`";
    }
}
