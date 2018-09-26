package org.jujubeframework.jdbc.persistence.base.jpa.handler;

import org.jujubeframework.jdbc.persistence.base.spec.Spec;

import java.util.List;

/**
 * Like Handler
 *
 * @author John Li
 */
public class GtHandler implements Handler {
    private static final String GT = "Gt";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(GT)) {
            String field = methodName.replace(GT, EMPTY);
            field = realField(field);
            spec.gt(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
