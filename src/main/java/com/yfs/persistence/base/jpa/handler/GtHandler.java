package com.yfs.persistence.base.jpa.handler;

import java.util.List;

import com.yfs.persistence.base.spec.Spec;

/** Like Handler */
public class GtHandler implements Handler {
    private static final String GT = "Gt";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(GT)) {
            String field = methodName.replace(GT, EMPTY);
            field = Handler.realField(field);
            spec.gt(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
