package com.yfs.persistence.base.jpa.strategy;

import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.Lists;
import com.yfs.persistence.base.jpa.JpaQueryProxyDao;
import com.yfs.persistence.base.jpa.handler.DefaultHandlerChain;
import com.yfs.persistence.base.jpa.handler.Handler;
import com.yfs.persistence.base.jpa.handler.HandlerContext;
import com.yfs.persistence.base.spec.Spec;
import com.yfs.support.entity.RecordEntity;
import com.yfs.util.Collections3;
import com.yfs.util.Texts;

public class FindAnyByAnyQuery extends QueryStrategy {

    private static final String FIND_BY2 = "find(.+?)By(.+)";
    private static final String FIND_BY2_PREFIX = "find(.+?)By";

    public FindAnyByAnyQuery(JpaQueryProxyDao proxyDao) {
        super(proxyDao);
    }

    @Override
    boolean accept(Method method) {
        String mname = method.getName();
        return Texts.find(mname, FIND_BY2);
    }

    @Override
    Object query(Method method, Object[] args) {
        String mname = method.getName();
        String[] arr = Texts.getGroups(FIND_BY2, mname);
        String queryField = arr[1];
        queryField = Handler.realField(queryField);
        String tmname = mname.replaceAll(FIND_BY2_PREFIX, EMPTY);

        Spec spec = Spec.newS();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.prepositionHandler);
        selfChain.addHandlers(HandlerContext.complexHandler);
        selfChain.addHandlers(HandlerContext.simpleHandler);
        selfChain.handler(spec, tmname, Lists.newArrayList(args));

        boolean isFindOne = false;
        // 如果以findOneBy开头，或返回值是Record与BaseEntity
        if (!List.class.equals(method.getReturnType())) {
            isFindOne = true;
        }

        String newQueryField = buildQueryField(queryField);
        if (isFindOne) {
            RecordEntity record = proxyDao.findOne(newQueryField, spec);
            if (record != null) {
                return record.get(queryField);
            }
        } else {
            List<RecordEntity> list = proxyDao.find(newQueryField, spec);
            return Collections3.extractToList(list, queryField);
        }
        return null;
    }

}
