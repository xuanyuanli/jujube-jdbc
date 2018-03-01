package com.yfs.persistence.base.jpa.strategy;

import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.Lists;
import com.yfs.persistence.base.jpa.JpaQueryProxyDao;
import com.yfs.persistence.base.jpa.handler.DefaultHandlerChain;
import com.yfs.persistence.base.jpa.handler.HandlerContext;
import com.yfs.persistence.base.spec.Spec;
import com.yfs.util.Pojos;

public class FindByAnyQuery extends QueryStrategy {

    public FindByAnyQuery(JpaQueryProxyDao proxyDao) {
        super(proxyDao);
    }

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
        selfChain.addHandlers(HandlerContext.prepositionHandler);
        selfChain.addHandlers(HandlerContext.complexHandler);
        selfChain.addHandlers(HandlerContext.simpleHandler);
        selfChain.handler(spec, tmname, Lists.newArrayList(args));

        if (isFindOne) {
            return Pojos.mapping(proxyDao.findOne(spec), proxyDao.getClazz());
        } else {
            return Pojos.mappingArray(proxyDao.find(spec), proxyDao.getClazz());
        }
    }

}
