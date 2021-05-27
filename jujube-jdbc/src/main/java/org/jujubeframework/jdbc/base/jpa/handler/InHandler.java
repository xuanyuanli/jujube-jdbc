package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * In Handler
 *
 * @author John Li
 */
public class InHandler implements Handler {
    public static final String IN = "In";

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(IN)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - IN.length());
            field = getDbColumn(method, field);
            spec.in(field, toIterable(args.get(0)));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

    private Iterable<?> toIterable(Object o) {
        if (o instanceof Iterable) {
            return (Iterable<?>) o;
        }
        List<Object> list = new ArrayList<>();
        if (o != null) {
            if (o.getClass().isArray()) {
                int length = Array.getLength(o);
                for (int i = 0; i < length; i++) {
                    list.add(Array.get(o, i));
                }
            }
        }
        return list;
    }

}
