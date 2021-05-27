package org.jujubeframework.jdbc.base.util;

import com.google.common.collect.Lists;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sql解析工具
 *
 * @author John Li Email：jujubeframework@163.com
 */
public class Sqls {

    private Sqls() {
    }

    private static final ConcurrentMap<String, String> GET_COUNT_SQL_CACHE = new ConcurrentHashMap<>();

    /**
     * 解析一条sql，返回获取表数据总数的sql
     */
    public static String getCountSql(final String sql) {
        // 此处缓存可能存在一个问题，就是in()条件是多变的，现在暂时不影响，因为getCount中的in()变化比较少。后期做处理
        return GET_COUNT_SQL_CACHE.computeIfAbsent(sql, k -> {
            try {
                Select select = (Select) CCJSqlParserUtil.parse(sql);
                SelectBody selectBody = select.getSelectBody();
                if (selectBody instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) selectBody;
                    if (plainSelect.getGroupBy() != null) {
                        Expression selectBodyHaving = plainSelect.getHaving();
                        String having = selectBodyHaving != null ? selectBodyHaving.toString() : "";
                        String formAndWhere = getFormAndWhere(plainSelect);
                        return "SELECT COUNT(*) FROM (SELECT 1 FROM " + formAndWhere + " " + plainSelect.getGroupBy() + " " + having + ") getcountsql_t_t";
                    } else {
                        return "SELECT COUNT(*) FROM " + getFormAndWhere(plainSelect);
                    }
                } else if (selectBody instanceof SetOperationList) {
                    return "SELECT COUNT(*) FROM (" + selectBody + ") getcountsql_t_t";
                }
                return null;
            } catch (JSQLParserException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String getFormAndWhere(PlainSelect plainSelect) {
        String formAndWhere = plainSelect.getFromItem().toString();
        formAndWhere += getJoin(plainSelect.getJoins());
        if (plainSelect.getWhere() != null) {
            formAndWhere += " WHERE " + plainSelect.getWhere();
        }
        return formAndWhere;
    }

    private static String getJoin(List<Join> joins) {
        StringBuilder joinStr = new StringBuilder();
        if (joins != null) {
            for (Join join : joins) {
                joinStr.append(join).append(" ");
            }
            joinStr.insert(0, " ");
        }
        return joinStr.toString();
    }

    /**
     * 用子查询把对原始sql进行包裹
     */
    public static String wrapSql(String sql) {
        return "select * from (" + sql + ") t_" + Thread.currentThread().getId() + "_" + System.currentTimeMillis();
    }

    /**
     * 填充命名参数，获得真实sql
     */
    public static String realSql(String sql, List<Object> namedParam) {
        if (namedParam == null || namedParam.isEmpty()) {
            return sql;
        }
        StringBuilder result = new StringBuilder();
        List<String> arr = Lists.newArrayList(StringUtils.splitByWholeSeparator(sql, "?"));
        String suffix = "?";
        if (sql.endsWith(suffix)) {
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
    private static Object singleQuotes(Object obj) {
        if (obj instanceof String) {
            String sobj = (String) obj;
            // 如果包含反斜杠，则进行转义
            if (sobj.contains("\\")) {
                sobj = StringUtils.replace(sobj, "\\", "\\\\");
            }
            // 如果包含单引号，则进行转义
            if (sobj.contains("'")) {
                sobj = StringUtils.replace(sobj, "'", "\\'");
            }
            return ("'" + sobj + "'");
        }
        return obj;
    }

    /**
     * 获得安全的字段值
     */
    public static String getSecurityFieldName(String fieldName) {
        int index = fieldName.indexOf(".");
        if (index != -1) {
            index++;
            return fieldName.substring(0, index) + "`" + fieldName.substring(index) + "`";
        } else {
            return "`" + fieldName + "`";
        }
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
            sb.append(obj).append(",");
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

    public static String likeWrap(String name) {
        return "%" + name + "%";
    }

    public static String leftLikeWrap(String name) {
        return "%" + name;
    }

    public static String rightLikeWrap(String name) {
        return name + "%";
    }

}
