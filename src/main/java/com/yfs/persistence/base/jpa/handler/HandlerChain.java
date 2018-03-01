package com.yfs.persistence.base.jpa.handler;

import java.util.List;

import com.yfs.persistence.base.spec.Spec;

/** Handler的责任链 */
public interface HandlerChain {

    public void handler(Spec spec, String methodName, List<Object> args);

}
