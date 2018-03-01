package com.yfs.persistence.base.jpa.handler;

import java.util.List;

import com.yfs.persistence.base.spec.Spec;

/** Is Null Handler */
public class IsNullHandler implements Handler {
    private static final String IS_NULL = "IsNull";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(IS_NULL)) {
            String field = methodName.replace(IS_NULL, EMPTY);
            field = Handler.realField(field);
            spec.isNull(field);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
