package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Like Handler
 *
 * @author John Li
 */
public class LteHandler implements Handler {
    public static final String LTE = "Lte";

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(LTE)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - LTE.length());
            field = getDbColumn(method, field);
            spec.lte(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(method,spec, truncationMethodName, args);
        }
    }

}
