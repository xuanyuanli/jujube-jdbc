/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jujubeframework.jdbc.base.dialect;

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

    private String getSecurityTableName(String tableName) {
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
            String[] columnsArray = columns.split(",");
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
        StringBuilder sql = new StringBuilder("delete from ");
        sql.append(getSecurityTableName(tableName));
        sql.append(" where `").append(primaryKey).append("` = ?");
        return sql.toString();
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
        StringBuilder sql = new StringBuilder("delete from ");
        sql.append(getSecurityTableName(tableName));
        sql.append(" where ").append(filters);
        return sql.toString();
    }

    @Override
    public String forDbPaginationQuery(String origSql, int start, int size) {
        StringBuffer pageSql = new StringBuffer();
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
