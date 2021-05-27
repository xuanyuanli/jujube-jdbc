package org.jujubeframework.jdbc.base.jpa.strategy;

import org.jujubeframework.jdbc.base.jpa.JpaBaseDaoSupport;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.base.util.JdbcPojos;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author John Li
 */
public class FindByAnyQuery extends BaseQueryStrategy {

    private static final String FIND_ONE_BY = "findOneBy";
    private static final String FIND_BY = "findBy";

    @Override
    public boolean accept(String methodName) {
        return methodName.startsWith(FIND_ONE_BY) || methodName.startsWith(FIND_BY);
    }

    @Override
    Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        String mname = method.getName();
        boolean startsWithFindOne = mname.startsWith(FIND_ONE_BY);
        boolean isFindOne = startsWithFindOne || !List.class.equals(method.getReturnType());
        String tmname = startsWithFindOne ? mname.substring(FIND_ONE_BY.length()) : mname.substring(FIND_BY.length());
        Spec spec = getSpecOfAllHandler(method,args,tmname);
        if (isFindOne) {
            return JdbcPojos.mapping(proxyDao.findOne(spec), proxyDao.getOriginalRealGenericType());
        } else {
            return JdbcPojos.mappingArray(proxyDao.find(spec), proxyDao.getOriginalRealGenericType());
        }
    }

}
