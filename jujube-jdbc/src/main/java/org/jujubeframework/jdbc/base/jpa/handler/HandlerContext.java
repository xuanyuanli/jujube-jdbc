package org.jujubeframework.jdbc.base.jpa.handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler的一些集合
 *
 * @author John Li
 */
public class HandlerContext {
    /**
     * 简单处理，如eq、like等
     */
    public static final List<Handler> SIMPLE_HANDLER = new ArrayList<>();
    /**
     * 复杂处理，如and
     */
    public static final List<Handler> COMPLEX_HANDLER = new ArrayList<>();
    /**
     * 前置处理，如limit、sort
     */
    public static final List<Handler> PREPOSITION_HANDLER = new ArrayList<>();

    static {
        SIMPLE_HANDLER.add(new LikeHandler());
        SIMPLE_HANDLER.add(new NotHandler());
        SIMPLE_HANDLER.add(new IsNullHandler());
        SIMPLE_HANDLER.add(new IsNotNullHandler());
        SIMPLE_HANDLER.add(new IsNotEmptyHandler());
        SIMPLE_HANDLER.add(new BetweenHandler());
        SIMPLE_HANDLER.add(new GteHandler());
        SIMPLE_HANDLER.add(new GtHandler());
        SIMPLE_HANDLER.add(new LteHandler());
        SIMPLE_HANDLER.add(new LtHandler());
        SIMPLE_HANDLER.add(new InHandler());
        SIMPLE_HANDLER.add(new EqHandler());

        COMPLEX_HANDLER.add(new AndHandler());

        PREPOSITION_HANDLER.add(new LimitHandler());
        PREPOSITION_HANDLER.add(new OrderByHandler());
    }
}
