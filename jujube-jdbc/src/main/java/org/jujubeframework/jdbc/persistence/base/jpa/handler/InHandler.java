package org.jujubeframework.jdbc.persistence.base.jpa.handler;

import org.jujubeframework.jdbc.persistence.base.spec.Spec;

import java.util.List;

/**
 * In Handler
 *
 * @author John Li
 */
public class InHandler implements Handler {
    private static final String IN = "In";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(IN)) {
            String field = methodName.replace(IN, EMPTY);
            field = realField(field);
            spec.in(field, (Iterable<?>) args.get(0));
            args.remove(0);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
