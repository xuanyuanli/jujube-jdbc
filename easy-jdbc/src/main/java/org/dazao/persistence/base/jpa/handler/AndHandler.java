package org.dazao.persistence.base.jpa.handler;

import java.util.List;

import org.dazao.persistence.base.spec.Spec;

public class AndHandler implements Handler {

    private static final String AND = "And";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.contains(AND)) {
            String[] sarr = methodName.split(AND);
            Spec[] specArr = new Spec[sarr.length];
            int i = 0;
            for (String field : sarr) {
                Spec tSpec = Spec.newS();
                DefaultHandlerChain selfChain = new DefaultHandlerChain();
                selfChain.addHandlers(HandlerContext.simpleHandler);
                selfChain.handler(tSpec, field, args);
                specArr[i++] = tSpec;
            }
            spec.and(specArr);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
