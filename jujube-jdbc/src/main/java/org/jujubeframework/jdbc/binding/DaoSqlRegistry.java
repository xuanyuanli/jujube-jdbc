package org.jujubeframework.jdbc.binding;

import org.apache.commons.io.IOUtils;
import org.jujubeframework.constant.Charsets;
import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.base.BaseDaoSupport;
import org.jujubeframework.util.Beans;
import org.jujubeframework.util.Resources;
import org.jujubeframework.util.Texts;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Dao与Sql之间对应关系的注册器
 *
 * @author John Li
 */
public class DaoSqlRegistry {
    public static final String FIND = "find";
    public static final String GET_COUNT = "getCount";

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
        Map<String, List<String>> methodSql = getMethodSql();
        classStream.forEach(cl -> {
            for (Method declaredMethod : cl.getDeclaredMethods()) {
                if (isSqlMethod(declaredMethod)) {
                    String key = cl.getSimpleName() + "." + declaredMethod.getName();
                    List<String> sql = methodSql.get(key);
                    if (sql == null) {
                        throw new RuntimeException(cl.getName() + "." + declaredMethod.getName() + "()方法没有找到对应的Sql语句");
                    }
                    METHOD_SQL_DATA.put(declaredMethod, new SqlBuilder(sql));
                }
            }
        });
    }

    /**
     * 是否是sql对应的方法
     */
    private static boolean isSqlMethod(Method method) {
        Method declaredMethod = Beans.getSelfDeclaredMethod(BaseDaoSupport.class, method.getName(), method.getParameterTypes());
        if (declaredMethod == null && !method.getName().startsWith(FIND) && !method.getName().startsWith(GET_COUNT)) {
            return true;
        }
        return false;
    }

    /**
     * 获得方法名与sql的对应map
     */
    private static Map<String, List<String>> getMethodSql() {
        Resource[] sqlResources = Resources.getClassPathAllResources(ClassUtils.convertClassNameToResourcePath(sqlBasePackage) + "/**/*.sql");
        Map<String, List<String>> result = new HashMap<>(sqlResources.length * 2);
        for (Resource sqlResource : sqlResources) {
            try {
                String filename = sqlResource.getFilename();
                filename = filename.substring(0, filename.length() - 4);
                List<String> lines = IOUtils.readLines(sqlResource.getInputStream(), Charsets.UTF_8.name());
                Map<String, List<String>> group = Texts.group(lines, t -> t.startsWith("##") ? t.substring(2).trim() : "");
                for (String key : group.keySet()) {
                    result.put(filename + "." + key, group.get(key));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
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

    /**
     * 获得sql构建器
     *
     * @param method
     * @return
     */
    public static SqlBuilder getSqlBuilder(Method method) {
        return METHOD_SQL_DATA.get(method);
    }
}
