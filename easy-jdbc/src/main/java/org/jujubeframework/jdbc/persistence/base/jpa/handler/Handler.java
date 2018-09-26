package org.jujubeframework.jdbc.persistence.base.jpa.handler;

import org.jujubeframework.jdbc.persistence.base.spec.Spec;
import org.jujubeframework.util.CamelCase;

import java.util.List;

/**
 * 方法名到Spec的处理
 *
 * @author John Li
 */
public interface Handler {
    static final String EMPTY = "";

    /**
     * 大写转为下划杠写法
     * @param queryField 查询的字段
     * @return 转换后的值
     */
    default String realField(String queryField) {
        queryField = Character.toLowerCase(queryField.charAt(0)) + queryField.substring(1);
        queryField = CamelCase.toUnderlineName(queryField);
        return queryField;
    }

    /**
     * 处理
     *
     * @param spec
     * @param methodName
     * @param args
     * @param chain
     */
    void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain);
}
