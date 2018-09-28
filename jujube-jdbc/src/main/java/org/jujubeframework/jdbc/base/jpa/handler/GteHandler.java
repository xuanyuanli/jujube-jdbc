package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.util.List;

/**
 * Like Handler
 *
 * @author John Li
 */
public class GteHandler implements Handler {
    private static final String GTE = "Gte";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(GTE)) {
            String field = methodName.replace(GTE, EMPTY);
            field = realField(field);
            spec.gte(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
