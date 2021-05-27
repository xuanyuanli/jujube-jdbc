package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Eq Handler(因为是默认，所以在Chain中一定要是最后一个)
 *
 * @author John Li
 */

public class EqHandler implements Handler {

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        String field = getDbColumn(method, truncationMethodName);
        Object value = args.get(0);
        spec.eq(field, value);
        args.remove(0);
    }

}
