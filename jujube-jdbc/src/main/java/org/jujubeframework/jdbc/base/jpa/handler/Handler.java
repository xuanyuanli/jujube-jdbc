package org.jujubeframework.jdbc.base.jpa.handler;

import java.lang.reflect.Method;
import java.util.List;

import org.jujubeframework.jdbc.base.jpa.strategy.BaseQueryStrategy;
import org.jujubeframework.jdbc.base.spec.Spec;

/**
 * 方法名到Spec的处理
 *
 * @author John Li
 */
public interface Handler {
    String EMPTY = "";

    /**
     * 处理
     *
     * @param method
     *            方法
     * @param spec
     *            规格
     * @param truncationMethodName
     *            被截断的方法名
     * @param args
     *            方法实参列表
     * @param chain
     *            Handler责任链
     */
    void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain);

    /**
     * @see BaseQueryStrategy#getDbColumnName(Method, String)
     */
    default String getDbColumn(Method method, String queryField) {
        return BaseQueryStrategy.getDbColumnName(method, queryField);
    }
}
