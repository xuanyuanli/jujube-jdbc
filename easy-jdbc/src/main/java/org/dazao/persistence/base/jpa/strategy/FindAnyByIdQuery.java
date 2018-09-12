package org.dazao.persistence.base.jpa.strategy;

import org.dazao.persistence.base.jpa.handler.Handler;
import org.dazao.support.entity.RecordEntity;

import java.lang.reflect.Method;

/** find*ById方法 */
public class FindAnyByIdQuery extends QueryStrategy {

    private static final String FIND_BY_ID = "findById";
    private static final String BY_ID = "ById";

    @Override
    public boolean accept(Method method) {
        String methodName = method.getName();
        return methodName.endsWith(BY_ID) && !methodName.equals(FIND_BY_ID);
    }

    @Override
    public Object query(Method method, Object[] args) {
        String methodName = method.getName();
        String queryField = methodName.replaceAll(BY_ID + "$", EMPTY).replaceAll("^" + FIND, EMPTY);
        queryField = Handler.realField(queryField);
        Long id = (Long) args[0];
        if (id != null && id > 0) {
            RecordEntity record = proxyDao.findById(buildQueryField(queryField), id);
            if (record != null) {
                return record.get(queryField);
            }
        }
        return null;
    }

}
