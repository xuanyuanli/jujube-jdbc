package org.jujubeframework.jdbc.persistence.base.jpa.handler;

import org.jujubeframework.jdbc.persistence.base.spec.Spec;

import java.util.List;

/**
 * Handler的责任链
 *
 * @author John Li
 */
public interface HandlerChain {

    /**
     * 处理
     *
     * @param spec
     * @param methodName
     * @param args
     */
    public void handler(Spec spec, String methodName, List<Object> args);

}
