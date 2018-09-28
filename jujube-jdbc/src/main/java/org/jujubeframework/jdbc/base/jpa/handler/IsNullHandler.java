package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.util.List;

/**
 * Is Null Handler
 *
 * @author John Li
 */
public class IsNullHandler implements Handler {
    private static final String IS_NULL = "IsNull";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(IS_NULL)) {
            String field = methodName.replace(IS_NULL, EMPTY);
            field = realField(field);
            spec.isNull(field);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
