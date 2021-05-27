package org.jujubeframework.jdbc.base.jpa.strategy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.base.jpa.JpaBaseDaoSupport;
import org.jujubeframework.jdbc.base.jpa.handler.DefaultHandlerChain;
import org.jujubeframework.jdbc.base.jpa.handler.HandlerContext;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.base.util.Strings;
import org.jujubeframework.jdbc.binding.DaoSqlRegistry;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * 查询策略
 *
 * @author John Li
 */
@Slf4j
public abstract class BaseQueryStrategy {
    private static final ConcurrentMap<String, List<String>> QUERY_FIELD_LIST_CACHE = new ConcurrentHashMap<>();

    /**
     * 是否承认
     *
     * @param methodName
     *            方法名称
     * @return boolean
     */
    public abstract boolean accept(String methodName);

    /**
     * 执行查询
     *
     * @param proxyDao
     *            basedao
     * @param method
     *            方法
     * @param args
     *            方法的入参
     * @return 查询结果
     */
    abstract Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args);

    /**
     * 需要用``来包含字段
     */
    protected String buildQueryField(String field) {
        return "`" + field + "`";
    }

    /**
     * 数据库表对应的列名称
     *
     * @param method
     *            Dao中的方法
     * @param entityFieldName
     *            查询的Entity Class字段名称
     * @return 数据库列名
     */
    public static String getDbColumnName(Method method, String entityFieldName) {
        return DaoSqlRegistry.getDbColumnName((Class<? extends BaseDao>) method.getDeclaringClass(), entityFieldName);
    }

    /**
     * 获取数据库表列名
     *
     * @param method
     *            方法
     * @param queryField
     *            方法名中的查询字段
     */
    public static List<String> getDbColumnNames(Method method, String queryField) {
        return QUERY_FIELD_LIST_CACHE.computeIfAbsent(method.toString() + "#" + queryField, key -> {
            List<String> fieldList = new ArrayList<>();
            String[] arr = Strings.splitByAnd(queryField);
            if (arr.length > 1) {
                for (String field : arr) {
                    fieldList.add(getDbColumnName(method, field));
                }
            } else {
                fieldList.add(getDbColumnName(method, queryField));
            }
            return fieldList;
        });
    }

    /** 用所有的Handler处理当前方法 */
    Spec getSpecOfAllHandler(Method method, Object[] args, String tmname) {
        Spec spec = new Spec();
        DefaultHandlerChain handlerChain = new DefaultHandlerChain();
        handlerChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        handlerChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        handlerChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        handlerChain.handler(method, spec, tmname, Lists.newArrayList(args));
        return spec;
    }
}
