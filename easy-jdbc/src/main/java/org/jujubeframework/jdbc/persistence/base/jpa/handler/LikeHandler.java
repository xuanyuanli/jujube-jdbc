package org.jujubeframework.jdbc.persistence.base.jpa.handler;

import org.jujubeframework.jdbc.persistence.base.spec.Spec;

import java.util.List;

/**
 * Like Handler
 *
 * @author John Li
 */
public class LikeHandler implements Handler {
    private static final String LIKE = "Like";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.endsWith(LIKE)) {
            String field = methodName.replace(LIKE, EMPTY);
            field = realField(field);
            spec.like(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(spec, methodName, args);
        }
    }

}
