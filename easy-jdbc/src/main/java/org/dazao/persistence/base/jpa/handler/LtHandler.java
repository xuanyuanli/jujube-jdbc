package org.dazao.persistence.base.jpa.handler;

import java.util.List;

import org.dazao.persistence.base.spec.Spec;

/** Like Handler */
public class LtHandler implements Handler {
    private static final String LT = "Lt";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(LT)) {
            String field = methodName.replace(LT, EMPTY);
            field = Handler.realField(field);
            spec.lt(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
