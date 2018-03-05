package org.dazao.persistence.base.jpa.handler;

import java.util.ArrayList;
import java.util.List;

/** Handler的一些集合 */
public class HandlerContext {
    /** 简单处理，如eq、like等 */
    public static final List<Handler> simpleHandler = new ArrayList<>();
    /** 复杂处理，如and */
    public static final List<Handler> complexHandler = new ArrayList<>();
    /** 前置处理，如limit、sort */
    public static final List<Handler> prepositionHandler = new ArrayList<>();
    static {
        simpleHandler.add(new LikeHandler());
        simpleHandler.add(new NotHandler());
        simpleHandler.add(new IsNullHandler());
        simpleHandler.add(new IsNotNullHandler());
        simpleHandler.add(new IsNotEmptyHandler());
        simpleHandler.add(new BetweenHandler());
        simpleHandler.add(new GteHandler());
        simpleHandler.add(new GtHandler());
        simpleHandler.add(new LteHandler());
        simpleHandler.add(new LtHandler());
        simpleHandler.add(new InHandler());
        simpleHandler.add(new EqHandler());

        complexHandler.add(new AndHandler());

        prepositionHandler.add(new LimitHandler());
        prepositionHandler.add(new SortHandler());
    }
}
