package org.jujubeframework.jdbc.base.jpa.strategy;

import com.google.common.collect.Lists;
import org.jujubeframework.jdbc.base.jpa.handler.DefaultHandlerChain;
import org.jujubeframework.jdbc.base.jpa.handler.HandlerContext;
import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Method;


/**
 * @author John Li
 */
public class GetCountByAnyQuery extends BaseQueryStrategy {

    private static final String GET_COUNT_BY = "getCountBy";

    @Override
    boolean accept(Method method) {
        return method.getName().startsWith(GET_COUNT_BY);
    }

    @Override
    Object query(Method method, Object[] args) {
        String mname = method.getName();
        String tmname = mname.replaceAll(GET_COUNT_BY, EMPTY);
        Spec spec = Spec.newS();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        selfChain.handler(spec, tmname, Lists.newArrayList(args));
        long count = proxyDao.getCount(spec);
        if (method.getReturnType().equals(int.class) || method.getReturnType().equals(Integer.class)) {
            return (int) count;
        } else {
            return count;
        }
    }

}
