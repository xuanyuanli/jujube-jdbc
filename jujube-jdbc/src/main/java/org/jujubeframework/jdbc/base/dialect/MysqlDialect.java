package org.jujubeframework.jdbc.base.dialect;

import org.apache.commons.lang3.StringUtils;
import org.jujubeframework.lang.Record;

import java.util.List;
import java.util.Map.Entry;

/**
 * MysqlDialect.
 *
 * @author John Li
 */
public class MysqlDialect implements Dialect {

    private static final String SQL_CONTAIN_SYMBOL = "`";
    public static final String DOT = ".";

    @Override
    public String getSecurityTableName(String tableName) {
        String result = tableName.trim();
        if (!result.contains(DOT)) {
            result = SQL_CONTAIN_SYMBOL + result + SQL_CONTAIN_SYMBOL;
        }
        return result;
    }

    @Override
    public String forDbSimpleQuery(String fields, String tableName, String filters) {
        return "select " + fields + " from " + getSecurityTableName(tableName) + " where " + filters;
    }

    @Override
    public String forDbSimpleQuery(String fields, String tableName) {
        return "select " + fields + " from " + getSecurityTableName(tableName);
    }

    @Override
    public String forDbFindById(String tableName, String primaryKey, String columns) {
        StringBuilder sql = new StringBuilder("select ");
        String symbol = "*";
        if (symbol.equals(columns.trim())) {
            sql.append(columns);
        } else {
            String[] columnsArray = StringUtils.splitByWholeSeparator(columns, ",");
            for (int i = 0; i < columnsArray.length; i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(SQL_CONTAIN_SYMBOL).append(columnsArray[i].trim()).append(SQL_CONTAIN_SYMBOL);
            }
        }
        sql.append(" from ");
        sql.append(getSecurityTableName(tableName));
        sql.append(" where `").append(primaryKey).append("` = ?");
        return sql.toString();
    }

    @Override
    public String forDbDeleteById(String tableName, String primaryKey) {
        return "delete from " + getSecurityTableName(tableName) +
                " where `" + primaryKey + "` = ?";
    }

    @Override
    public String forDbSave(String tableName, Record record, List<Object> paras) {
        if (record.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ");
        sql.append(getSecurityTableName(tableName)).append("(");
        StringBuilder temp = new StringBuilder();
        temp.append(") values(");

        for (Entry<String, Object> e : record.entrySet()) {
            if (paras.size() > 0) {
                sql.append(", ");
                temp.append(", ");
            }
            sql.append(SQL_CONTAIN_SYMBOL).append(e.getKey()).append(SQL_CONTAIN_SYMBOL);
            temp.append("?");
            paras.add(e.getValue());
        }
        sql.append(temp.toString()).append(")");
        return sql.toString();
    }

    @Override
    public String forDbUpdate(String tableName, String primaryKey, Object id, Record record, List<Object> paras) {
        if (record.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(getSecurityTableName(tableName)).append(" set ");
        for (Entry<String, Object> e : record.entrySet()) {
            String colName = e.getKey();
            if (!primaryKey.equalsIgnoreCase(colName)) {
                if (paras.size() > 0) {
                    sql.append(", ");
                }
                sql.append(SQL_CONTAIN_SYMBOL).append(colName).append("` = ? ");
                paras.add(e.getValue());
            }
        }
        sql.append(" where `").append(primaryKey).append("` = ?");
        paras.add(id);
        return sql.toString();
    }

    @Override
    public String forDbDelete(String tableName, String filters) {
        return "delete from " + getSecurityTableName(tableName) +
                " where " + filters;
    }

    @Override
    public String forDbPaginationQuery(String origSql, int start, int size) {
        StringBuilder pageSql = new StringBuilder();
        String lowerSql = origSql.toLowerCase().trim();
        int index = lowerSql.lastIndexOf(")");
        if (index > 0) {
            lowerSql = lowerSql.substring(index);
            int indexLimit = lowerSql.lastIndexOf(" limit ");
            if (indexLimit > 0) {
                origSql = origSql.substring(0, index + indexLimit);
            }
        } else {
            int indexLimit = lowerSql.lastIndexOf(" limit ");
            if (indexLimit > 0) {
                origSql = origSql.substring(0, indexLimit);
            }
        }
        pageSql.append(origSql);
        pageSql.append(" limit ").append(start).append(",").append(size);
        return pageSql.toString();
    }

}
