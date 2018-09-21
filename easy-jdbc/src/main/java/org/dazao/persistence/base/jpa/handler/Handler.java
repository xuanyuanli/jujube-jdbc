package org.dazao.persistence.base.jpa.handler;

import com.yfs.util.CamelCase;
import org.dazao.persistence.base.spec.Spec;

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
     */
    public static String realField(String queryField) {
        queryField = Character.toLowerCase(queryField.charAt(0)) + queryField.substring(1);
        queryField = CamelCase.toUnderlineName(queryField);
        return queryField;
    }

    void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain);
}
