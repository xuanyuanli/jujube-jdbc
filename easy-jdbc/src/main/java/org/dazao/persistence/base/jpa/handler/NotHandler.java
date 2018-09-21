package org.dazao.persistence.base.jpa.handler;

import org.dazao.persistence.base.spec.Spec;

import java.util.List;

/** Not Handler */

public class NotHandler implements Handler {
    private static final String NOT = "Not";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(NOT)) {
            String field = methodName.replace(NOT, EMPTY);
            field = Handler.realField(field);
            spec.not(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
