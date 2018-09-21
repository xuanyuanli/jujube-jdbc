package org.dazao.persistence.base.jpa.handler;

import org.dazao.persistence.base.spec.Spec;

import java.util.List;

/** Handler的责任链 */

public interface HandlerChain {

    public void handler(Spec spec, String methodName, List<Object> args);

}
