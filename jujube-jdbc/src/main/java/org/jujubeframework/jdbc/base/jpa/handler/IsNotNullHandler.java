package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Is Not Null Handler
 *
 * @author John Li
 */
public class IsNotNullHandler implements Handler {
    public static final String IS_NOT_NULL = "IsNotNull";

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(IS_NOT_NULL)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - IS_NOT_NULL.length());
            field = getDbColumn(method, field);
            spec.isNotNull(field);
        } else {
            chain.handler(method,spec, truncationMethodName, args);
        }
    }

}
