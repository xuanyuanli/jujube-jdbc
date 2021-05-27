package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Like Handler
 *
 * @author John Li
 */
public class NotLikeHandler implements Handler {
    public static final String NOT_LIKE = "NotLike";

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(NOT_LIKE)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - NOT_LIKE.length());
            field = getDbColumn(method, field);
            spec.notlike(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
