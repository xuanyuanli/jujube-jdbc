package org.dazao.persistence.base.jpa.handler;

import java.util.ArrayList;
import java.util.List;

import org.dazao.persistence.base.spec.Spec;

public class DefaultHandlerChain implements HandlerChain {

    private List<Handler> chain = new ArrayList<>();
    private int pos = 0;

    public void handler(Spec spec, String methodName, List<Object> args) {
        if (pos < chain.size()) {
            chain.get(pos++).handler(spec, methodName, args, this);
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