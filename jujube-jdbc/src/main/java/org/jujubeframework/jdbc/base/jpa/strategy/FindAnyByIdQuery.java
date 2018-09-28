package org.jujubeframework.jdbc.base.jpa.strategy;

import org.jujubeframework.jdbc.support.entity.RecordEntity;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * find*ById方法
 *
 * @author John Li
 */

public class FindAnyByIdQuery extends BaseQueryStrategy {

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
        queryField = BaseQueryStrategy.realField(queryField);
        Serializable id =  (Serializable)args[0];
        if (id != null) {
            RecordEntity record = proxyDao.findById(buildQueryField(queryField), id);
            if (record != null) {
                return record.get(queryField);
            }
        }
        return null;
    }

}
