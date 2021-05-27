package org.jujubeframework.jdbc.base.jpa.strategy;

import org.jujubeframework.jdbc.base.jpa.JpaBaseDaoSupport;
import org.jujubeframework.jdbc.base.spec.Spec;

import java.lang.reflect.Method;

/**
 * @author John Li
 */
public class GetCountByAnyQuery extends BaseQueryStrategy {

    private static final String GET_COUNT_BY = "getCountBy";

    @Override
    public boolean accept(String methodName) {
        return methodName.startsWith(GET_COUNT_BY);
    }

    @Override
    Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        String mname = method.getName();
        String tmname = mname.substring(GET_COUNT_BY.length());
        Spec spec = getSpecOfAllHandler(method,args,tmname);
        long count = proxyDao.getCount(spec);
        if (method.getReturnType().equals(int.class) || method.getReturnType().equals(Integer.class)) {
            return (int) count;
        } else {
            return count;
        }
    }

}
