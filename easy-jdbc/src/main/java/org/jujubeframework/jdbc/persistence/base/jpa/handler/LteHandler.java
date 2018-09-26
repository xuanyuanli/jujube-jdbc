package org.jujubeframework.jdbc.persistence.base.jpa.handler;

import org.jujubeframework.jdbc.persistence.base.spec.Spec;

import java.util.List;

/**
 * Like Handler
 *
 * @author John Li
 */
public class LteHandler implements Handler {
    private static final String LTE = "Lte";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(LTE)) {
            String field = methodName.replace(LTE, EMPTY);
            field = realField(field);
            spec.lte(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
