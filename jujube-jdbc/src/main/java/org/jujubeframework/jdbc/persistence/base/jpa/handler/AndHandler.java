package org.jujubeframework.jdbc.persistence.base.jpa.handler;

import org.jujubeframework.jdbc.persistence.base.spec.Spec;

import java.util.List;

/**
 * @author John Li
 */
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
                selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
                selfChain.handler(tSpec, field, args);
                specArr[i++] = tSpec;
            }
            spec.and(specArr);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
