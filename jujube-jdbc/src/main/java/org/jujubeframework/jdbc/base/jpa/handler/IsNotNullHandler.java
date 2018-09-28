package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.util.List;

/**
 * Is Not Null Handler
 *
 * @author John Li
 */
public class IsNotNullHandler implements Handler {
    private static final String IS_NOT_NULL = "IsNotNull";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(IS_NOT_NULL)) {
            String field = methodName.replace(IS_NOT_NULL, EMPTY);
            field = realField(field);
            spec.isNotNull(field);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
