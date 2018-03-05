package org.dazao.persistence.base.jpa.handler;

import java.util.List;

import org.dazao.persistence.base.spec.Spec;

/** Like Handler */
public class LteHandler implements Handler {
    private static final String LTE = "Lte";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(LTE)) {
            String field = methodName.replace(LTE, EMPTY);
            field = Handler.realField(field);
            spec.lte(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
