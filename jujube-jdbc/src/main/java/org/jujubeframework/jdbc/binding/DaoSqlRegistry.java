package org.jujubeframework.jdbc.binding;

import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.util.Beans;
import org.jujubeframework.util.Resources;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author John Li
 */
public class DaoSqlRegistry {
    /**
     * Dao接口所在的package
     */
    private static String basePackage;

    /**
     * Dao Sql所在的package。如果不设置，默认为{@value #basePackage}.sql
     */
    private static String sqlBasePackage;

    /**
     * 是否监听Dao Sql变化而动态刷新sql缓存
     */
    private static boolean watchSqlFile;

    private static Map<Method, SqlBuilder> METHOD_SQL_DATA = new HashMap();

    public static void init() {
        List<Class<?>> packageClasses = Resources.getPackageClasses(basePackage);
        Stream<Class<?>> classStream = packageClasses.stream().filter(cl -> cl.isInterface() && BaseDao.class.isAssignableFrom(cl));


    }

    public static void setBasePackage(String basePackage) {
        DaoSqlRegistry.basePackage = basePackage;
    }

    public static void setSqlBasePackage(String sqlBasePackage) {
        DaoSqlRegistry.sqlBasePackage = sqlBasePackage;
    }

    public static void setWatchSqlFile(boolean watchSqlFile) {
        DaoSqlRegistry.watchSqlFile = watchSqlFile;
    }
}
