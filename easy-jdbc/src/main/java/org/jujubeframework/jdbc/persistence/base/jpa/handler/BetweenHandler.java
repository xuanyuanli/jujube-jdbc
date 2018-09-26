package org.jujubeframework.jdbc.persistence.base.jpa.handler;

import org.jujubeframework.jdbc.persistence.base.spec.Spec;

import java.util.List;

/**
 * Between Handler
 *
 * @author John Li
 */
public class BetweenHandler implements Handler {
    private static final String BETWEEN = "Between";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(BETWEEN)) {
            String field = methodName.replace(BETWEEN, EMPTY);
            field = Handler.realField(field);
            spec.between(field, args.get(0), args.get(1));
            args.remove(0);
            args.remove(0);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
