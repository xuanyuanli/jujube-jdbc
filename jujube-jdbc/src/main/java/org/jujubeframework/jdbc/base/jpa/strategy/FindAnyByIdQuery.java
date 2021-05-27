package org.jujubeframework.jdbc.base.jpa.strategy;

import org.jujubeframework.jdbc.base.jpa.JpaBaseDaoSupport;
import org.jujubeframework.jdbc.base.jpa.handler.GroupByHandler;
import org.jujubeframework.jdbc.base.jpa.handler.OrderByHandler;
import org.jujubeframework.jdbc.base.util.JdbcPojos;
import org.jujubeframework.jdbc.base.util.Strings;
import org.jujubeframework.jdbc.base.jpa.entity.RecordEntity;
import org.jujubeframework.util.Beans;
import org.jujubeframework.util.DataGenerator;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * find*ById方法
 *
 * @author John Li
 */

public class FindAnyByIdQuery extends BaseQueryStrategy {

    private static final String FIND_BY_ID2_PREFIX = "^find(.+?)ById$";

    @Override
    public boolean accept(String methodName) {
        return Strings.find(methodName, FIND_BY_ID2_PREFIX) && !methodName.contains(OrderByHandler.ORDER_BY) && !methodName.contains(GroupByHandler.GROUP_BY);
    }

    @Override
    public Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        String mname = method.getName();
        String[] arr = Strings.getGroups(FIND_BY_ID2_PREFIX, mname);
        List<String> fieldList = getDbColumnNames(method, arr[1]);
        Serializable id = (Serializable) args[0];
        if (id != null) {
            if (fieldList.size() > 1) {
                String newQueryField = fieldList.stream().map(this::buildQueryField).collect(Collectors.joining(", "));
                RecordEntity record = proxyDao.findById(newQueryField, id);
                return JdbcPojos.mapping(record, proxyDao.getOriginalRealGenericType());
            } else {
                Class<?> returnType = method.getReturnType();
                String queryField = fieldList.get(0);
                RecordEntity record = proxyDao.findById(buildQueryField(queryField), id);
                return getOneValue(returnType, queryField, record);
            }
        } else {
            return null;
        }
    }

    public static Object getOneValue(Class<?> returnType, String queryField, RecordEntity record) {
        boolean useDefaultVal = returnType.isPrimitive() && (record == null || record.get(queryField) == null);
        if (useDefaultVal) {
            return DataGenerator.generateDefaultValueByParamType(returnType);
        }
        if (record != null) {
            return Beans.getExpectTypeValue(record.get(queryField), returnType);
        }
        return null;
    }

}
