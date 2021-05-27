package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Between Handler
 *
 * @author John Li
 */
public class BetweenHandler implements Handler {
    public static final String BETWEEN = "Between";

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(BETWEEN)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - BETWEEN.length());
            field = getDbColumn(method,field);
            spec.between(field, args.get(0), args.get(1));
            args.remove(0);
            args.remove(0);
        } else {
            chain.handler(method,spec, truncationMethodName, args);
        }
    }

}
