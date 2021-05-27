package org.jujubeframework.jdbc.base.jpa.strategy;

import org.jujubeframework.jdbc.base.jpa.JpaBaseDaoSupport;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.base.util.JdbcPojos;
import org.jujubeframework.jdbc.base.util.Strings;
import org.jujubeframework.jdbc.base.jpa.entity.RecordEntity;
import org.jujubeframework.util.Collections3;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author John Li
 */
public class FindAnyByAnyQuery extends BaseQueryStrategy {

    private static final String FIND_BY2 = "^find(.+?)By(.+)";

    @Override
    public boolean accept(String methodName) {
        return Strings.find(methodName, FIND_BY2);
    }

    @Override
    Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        String mname = method.getName();
        String[] arr = Strings.getGroups(FIND_BY2, mname);
        Spec spec = getSpecOfAllHandler(method, args, arr[2]);
        boolean isFindOne = !List.class.equals(method.getReturnType());
        List<String> fieldList = getDbColumnNames(method, arr[1]);
        if (fieldList.size() > 1) {
            String newQueryField = fieldList.stream().map(this::buildQueryField).collect(Collectors.joining(", "));
            if (isFindOne) {
                RecordEntity record = proxyDao.findOne(newQueryField, spec);
                return JdbcPojos.mapping(record, proxyDao.getOriginalRealGenericType());
            } else {
                List<RecordEntity> list = proxyDao.find(newQueryField, spec);
                return JdbcPojos.mappingArray(list, proxyDao.getOriginalRealGenericType());
            }
        } else {
            Class<?> returnType = method.getReturnType();
            String queryField = fieldList.get(0);
            String newQueryField = buildQueryField(queryField);
            if (isFindOne) {
                RecordEntity record = proxyDao.findOne(newQueryField, spec);
                return FindAnyByIdQuery.getOneValue(returnType, queryField, record);
            } else {
                List<RecordEntity> list = proxyDao.find(newQueryField, spec);
                ParameterizedType genericReturnType = (ParameterizedType) method.getGenericReturnType();
                Type[] actualTypeArguments = genericReturnType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                    Type actualTypeArgument = actualTypeArguments[0];
                    return Collections3.extractToList(list, queryField, (Class<?>) actualTypeArgument);
                } else {
                    return Collections3.extractToList(list, queryField);
                }
            }
        }
    }

}
