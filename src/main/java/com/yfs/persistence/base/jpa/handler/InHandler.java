package com.yfs.persistence.base.jpa.handler;

import java.util.List;

import com.yfs.persistence.base.spec.Spec;

/** In Handler */
public class InHandler implements Handler {
    private static final String IN = "In";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(IN)) {
            String field = methodName.replace(IN, EMPTY);
            field = Handler.realField(field);
            spec.in(field, (Iterable<?>) args.get(0));
            args.remove(0);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
