package org.dazao.persistence.base.jpa.handler;

import java.util.List;

import org.dazao.persistence.base.spec.Spec;

/** Handler的责任链 */
public interface HandlerChain {

    public void handler(Spec spec, String methodName, List<Object> args);

}
