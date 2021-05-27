package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Is Null Handler
 *
 * @author John Li
 */
public class IsNullHandler implements Handler {
    public static final String IS_NULL = "IsNull";

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(IS_NULL)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - IS_NULL.length());
            field = getDbColumn(method, field);
            spec.isNull(field);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
