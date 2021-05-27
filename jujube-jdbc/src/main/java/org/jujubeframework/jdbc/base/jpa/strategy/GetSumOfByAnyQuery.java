package org.jujubeframework.jdbc.base.jpa.strategy;

import org.jujubeframework.jdbc.base.jpa.JpaBaseDaoSupport;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.base.util.Strings;

import java.lang.reflect.Method;

/**
 * @author John Li
 */
public class GetSumOfByAnyQuery extends BaseQueryStrategy {

    private static final String GET_SUM_OF = "^getSumOf(.+?)By(.+)";

    @Override
    public boolean accept(String methodName) {
        return Strings.find(methodName, GET_SUM_OF);
    }

    @Override
    Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        String mname = method.getName();
        String[] groups = Strings.getGroups(GET_SUM_OF, mname);
        String sumField = getDbColumnName(method, groups[1]);
        Spec spec = getSpecOfAllHandler(method, args,  groups[2]);
        return proxyDao.getSumOf(sumField, spec);
    }

}
