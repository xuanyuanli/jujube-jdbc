package com.yfs.persistence.base.jpa.handler;

import java.util.List;

import com.yfs.persistence.base.spec.Spec;

/** Eq Handler(因为是默认，所以在Chain中一定要是最后一个) */
public class EqHandler implements Handler {

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        String field = Handler.realField(methodName);
        spec.eq(field, args.get(0));
        args.remove(0);
    }

}
