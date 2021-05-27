package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author John Li
 */
public class DefaultHandlerChain implements HandlerChain {

    private final List<Handler> chain = new ArrayList<>();
    private int pos = 0;

    @Override
    public void handler(Method method,Spec spec, String truncationMethodName, List<Object> args) {
        if (pos < chain.size()) {
            chain.get(pos++).handler(method,spec, truncationMethodName, args, this);
        }
    }

    public List<Handler> getChain() {
        return chain;
    }

    public void addHandler(Handler handler) {
        this.chain.add(handler);
    }

    public void addHandlers(List<Handler> handlers) {
        this.chain.addAll(handlers);
    }

    public int getPos() {
        return pos;
    }

}
