package org.dazao.persistence.base.util;

import java.util.List;

import org.dazao.persistence.base.dialect.MysqlDialect;
import org.dazao.util.Texts;
import org.dazao.util.Texts.RegexQueryInfo;

import com.google.common.collect.Lists;

/**
 * Sql解析工具
 * 
 * @author 李衡 Email：li15038043160@163.com
 * @since 2015年1月27日 上午11:20:24
 */
public class Sqls {

    private Sqls() {
    }

    /**
     * 解析一条sql，返回获取表数据总数的sql
     * <p>
     * 下面的子查询方式，不能兼容group by的情况。如果需要，请特殊处理
     */
    public static String getCountSql(String sql) {
        // 查询中出现两种情况：
        // 1：SELECT name FROM (SELECT * FROM a) t
        // 2：SELECT name,(SELECT * FROM a ) FROM t
        // 3:SELECT name,(SELECT * FROM a ) FROM (SELECT * FROM a) t

        // 需要处理两种情况：1、from后又子查询；2、from后没有子查询

        String result = "";
        List<RegexQueryInfo> list = Texts.regQuery("from(\\s*?)\\(+(\\s*?)select", sql.toLowerCase());
        // 如果没有from后的子查询,则做如下处理
        if (list.isEmpty()) {
            // from前的2个或条件
            // 1、^(\\s*?)select(.*)
            // 2、^(\\s*?)
            result = Texts.regReplace("(^(\\s*?)select(.*)|^(\\s*?))from(?=\\s{1,})", "select count(*) ct from", sql);
        } else { // 如果有from后的子查询，则截取字符串即可
            result = "select count(*) ct " + sql.substring(list.get(0).getStart());
        }
        // 截去order by后面的内容
        list = Texts.regQuery("(\\s+order(\\s+?)by([^\\)]*?)$)", result.trim());
        if (!list.isEmpty()) {
            RegexQueryInfo info = list.get(list.size() - 1);
            result = result.substring(0, info.getStart());
        }
        return result;
    }

    /** 用子查询把对原始sql进行包裹 */
    public static String warpSql(String sql) {
        return "select * from (" + sql + ") t_" + Thread.currentThread().getId() + "_" + System.currentTimeMillis();
    }

    /**
     * 获取分页sql
     */
    public static String paginationSql(String origSql, int start, int size) {
        return new MysqlDialect().forDbPaginationQuery(origSql, start, size);
    }

    /**
     * 剔除sql语句中的别名
     */
    public static String removeAlias(String sql) {
        sql = sql.replace("this_ ", "");
        sql = sql.replace("this_.", "");
        if (!sql.contains("count(") && !sql.contains("max(") && !sql.contains("min(") && !sql.contains("avg(") && !sql.contains("sum(")) {
            sql = Texts.regReplace("(select)(.{4,}?)from", "select * from", sql);
        }
        sql = Texts.regReplace("as.+from", "from", sql);
        return sql;
    }

    /**
     * 填充命名参数，获得真实sql
     */
    public static String realSql(String sql, List<Object> namedParam) {
        if (namedParam == null || namedParam.isEmpty()) {
            return sql;
        }
        StringBuilder result = new StringBuilder();
        List<String> arr = Lists.newArrayList(sql.split("\\?"));
        if (sql.endsWith("?")) {
            arr.add("");
        }
        for (int i = 0; i < arr.size(); i++) {
            if (i + 1 != arr.size()) {
                result.append(arr.get(i)).append(singleQuotes(namedParam.get(i)));
            } else {
                result.append(arr.get(i));
            }
        }
        return result.toString();
    }

    /**
     * 如果是String类型，则用单引号包裹
     */
    public static Object singleQuotes(Object obj) {
        if (obj instanceof String) {
            return ("'" + obj.toString() + "'");
        }
        return obj;
    }

    public static String getSecurityFieldName(String fieldName) {
        return "`" + fieldName + "`";
    }

    /**
     * 获得in括号中的条件
     */
    public static String inJoin(Object obj) {
        StringBuilder sb = new StringBuilder();
        if (obj instanceof Iterable) {
            @SuppressWarnings("rawtypes")
            Iterable coll = (Iterable) obj;
            for (Object object : coll) {
                sb.append(singleQuotes(object)).append(",");
            }
        } else if (obj instanceof String) {
            sb.append(obj.toString()).append(",");
        } else if (obj instanceof Object[]) {
            Object[] objects = (Object[]) obj;
            for (Object object : objects) {
                sb.append(singleQuotes(object)).append(",");
            }
        } else {
            throw new RuntimeException("in的值格式不正确。可以为String，Object[]，Iterable");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
