package org.jujubeframework.jdbc.base.jpa.strategy;

import com.google.common.collect.Lists;
import org.jujubeframework.jdbc.base.jpa.JpaBaseDaoSupport;
import org.jujubeframework.jdbc.base.jpa.handler.DefaultHandlerChain;
import org.jujubeframework.jdbc.base.jpa.handler.HandlerContext;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.base.util.JdbcPojos;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author John Li
 */
public class FindAllQuery extends BaseQueryStrategy {
    public static final String FIND_ALL = "findAll";

    @Override
    public boolean accept(String methodName) {
        return methodName.startsWith(FIND_ALL);
    }

    @Override
    Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        if (args == null) {
            args = new Object[0];
        }
        boolean isFindOne = !List.class.equals(method.getReturnType());
        String mname = method.getName();
        String tmname = mname.substring(FIND_ALL.length());
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.handler(method, spec, tmname, Lists.newArrayList(args));
        List<?> list = JdbcPojos.mappingArray(proxyDao.find(spec), proxyDao.getOriginalRealGenericType());
        if (isFindOne) {
            return list.get(0);
        } else {
            return list;
        }
    }

}
