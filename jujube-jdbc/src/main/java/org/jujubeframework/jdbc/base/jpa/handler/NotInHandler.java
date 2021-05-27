package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Notin Handler
 *
 * @author John Li
 */
public class NotInHandler implements Handler {
    public static final String NOTIN = "NotIn";

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(NOTIN)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - NOTIN.length());
            field = getDbColumn(method, field);
            spec.notin(field, (Iterable<?>) args.get(0));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
