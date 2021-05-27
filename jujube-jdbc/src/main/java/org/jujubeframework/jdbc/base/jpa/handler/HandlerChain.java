package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Handler的责任链
 *
 * @author John Li
 */
public interface HandlerChain {

    /**
     * 处理
     *
     * @param method 方法
     * @param spec 规格
     * @param truncationMethodName 截断后的方法名（By之后的字符）
     * @param args 方法实参列表
     */
    void handler(Method method, Spec spec, String truncationMethodName, List<Object> args);

}
