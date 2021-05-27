package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.base.util.Strings;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author John Li
 */
public class AndHandler implements Handler {

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        String[] sarr = Strings.splitByAnd(truncationMethodName);
        if (sarr.length > 1) {
            Spec[] specArr = new Spec[sarr.length];
            int i = 0;
            for (String field : sarr) {
                Spec tSpec = new Spec();
                DefaultHandlerChain selfChain = new DefaultHandlerChain();
                selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
                selfChain.handler(method, tSpec, field, args);
                specArr[i++] = tSpec;
            }
            spec.and(specArr);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
