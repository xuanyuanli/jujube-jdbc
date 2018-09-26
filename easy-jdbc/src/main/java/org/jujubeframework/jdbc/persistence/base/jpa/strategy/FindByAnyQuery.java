package org.jujubeframework.jdbc.persistence.base.jpa.strategy;

import com.google.common.collect.Lists;
import org.jujubeframework.jdbc.persistence.base.jpa.handler.DefaultHandlerChain;
import org.jujubeframework.jdbc.persistence.base.jpa.handler.HandlerContext;
import org.jujubeframework.jdbc.persistence.base.spec.Spec;
import org.jujubeframework.util.Pojos;

import java.lang.reflect.Method;
import java.util.List;


/**
 * @author John Li
 */
public class FindByAnyQuery extends BaseQueryStrategy {

    private static final String FIND_ONE_BY = "findOneBy";
    private static final String FIND_BY = "findBy";

    @Override
    boolean accept(Method method) {
        String methodName = method.getName();
        return methodName.startsWith(FIND_ONE_BY) || methodName.startsWith(FIND_BY);
    }

    @Override
    Object query(Method method, Object[] args) {
        String mname = method.getName();
        boolean isFindOne = false;
        // 如果以findOneBy开头，或返回值是Record与BaseEntity
        if (mname.startsWith(FIND_ONE_BY) || !List.class.equals(method.getReturnType())) {
            isFindOne = true;
        } else if (mname.startsWith(FIND_BY)) {
            isFindOne = false;
        }
        String tmname = mname.replaceAll(FIND_BY, EMPTY).replaceAll(FIND_ONE_BY, EMPTY);

        Spec spec = Spec.newS();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        selfChain.handler(spec, tmname, Lists.newArrayList(args));

        if (isFindOne) {
            return Pojos.mapping(proxyDao.findOne(spec), proxyDao.getClazz());
        } else {
            return Pojos.mappingArray(proxyDao.find(spec), proxyDao.getClazz());
        }
    }

}
