package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Not Handler
 *
 * @author John Li
 */
public class NotHandler implements Handler {
    public static final String NOT = "Not";

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(NOT)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - NOT.length());
            field = getDbColumn(method, field);
            spec.not(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
