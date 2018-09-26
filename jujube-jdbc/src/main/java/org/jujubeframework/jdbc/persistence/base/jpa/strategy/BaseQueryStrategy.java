package org.jujubeframework.jdbc.persistence.base.jpa.strategy;

import org.jujubeframework.jdbc.persistence.base.jpa.JpaQueryProxyDao;
import org.jujubeframework.util.CamelCase;

import java.lang.reflect.Method;

/**
 * 查询策略
 *
 * @author John Li
 */
public abstract class BaseQueryStrategy {
    static final String EMPTY = "";
    static final String FIND = "find";

    protected JpaQueryProxyDao proxyDao;

    void setProxyDao(JpaQueryProxyDao proxyDao) {
        this.proxyDao = proxyDao;
    }

    /**
     * 是否承认
     * @param method 方法
     * @return boolean
     */
    abstract boolean accept(Method method);

    /**
     * 执行查询
     * @param  method 方法
     * @param args 方法的入参
     * @return 查询结果
     */
    abstract Object query(Method method, Object[] args);

    /**
     * 需要用``来包含字段
     */
    protected String buildQueryField(String field) {
        return "`" + field + "`";
    }

    /**
     * 大写转为下划杠写法
     * @param queryField 查询的字段
     * @return 转换后的值
     */
    public static String realField(String queryField) {
        queryField = Character.toLowerCase(queryField.charAt(0)) + queryField.substring(1);
        queryField = CamelCase.toUnderlineName(queryField);
        return queryField;
    }
}
