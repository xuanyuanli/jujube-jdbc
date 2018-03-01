package com.yfs.persistence.base.jpa.handler;

import java.util.List;

import com.yfs.persistence.base.spec.Spec;

/** Is Not Null Handler */
public class IsNotEmptyHandler implements Handler {
    private static final String IS_NOT_EMPTY = "IsNotEmpty";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(IS_NOT_EMPTY)) {
            String field = methodName.replace(IS_NOT_EMPTY, EMPTY);
            field = Handler.realField(field);
            spec.isNotEmpty(field);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
