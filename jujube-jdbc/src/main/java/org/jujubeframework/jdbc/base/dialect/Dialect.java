package org.jujubeframework.jdbc.base.dialect;

import org.jujubeframework.lang.Record;

import java.util.List;

/**
 * jdbc方言
 *
 * @author John Li
 */
public interface Dialect {

    Dialect DEFAULT = new MysqlDialect();

    String ALL_FIELDS = "*";

    String getSecurityTableName(String tableName);

    /**
     * 获得简单查询sql
     *
     * @param fields
     *            要查询的字段
     * @param tableName
     *            表名
     * @return 简单查询sql
     */
    String forDbSimpleQuery(String fields, String tableName);

    /**
     * 获得简单查询sql
     *
     * @param fields
     *            要查询的字段
     * @param tableName
     *            表名
     * @param filters
     *            where后的过滤
     * @return 简单查询sql
     */
    String forDbSimpleQuery(String fields, String tableName, String filters);

    /**
     * findById Sql
     *
     * @param tableName
     *            表名
     * @param primaryKey
     *            主键key
     * @param fields
     *            要查询的字段
     * @return Sql
     */
    String forDbFindById(String tableName, String primaryKey, String fields);

    /**
     * deleteById Sql
     *
     * @param tableName
     *            表名
     * @param primaryKey
     *            主键key
     * @return Sql
     */
    String forDbDeleteById(String tableName, String primaryKey);

    /**
     * delete sql
     *
     * @param tableName
     *            表名
     * @param filters
     *            where后的过滤
     * @return Sql
     */
    String forDbDelete(String tableName, String filters);

    /**
     * save sql
     *
     * @param tableName
     *            表名
     * @param record
     *            保持的内容
     * @param paras
     *            保持的值
     * @return Sql
     */
    String forDbSave(String tableName, Record record, List<Object> paras);

    /**
     * update sql
     *
     * @param tableName
     *            表名
     * @param primaryKey
     *            主键key
     * @param id
     *            主键值
     * @param record
     *            更新的内容
     * @param paras
     *            更新的值
     * @return Sql
     */
    String forDbUpdate(String tableName, String primaryKey, Object id, Record record, List<Object> paras);

    /**
     * 获取分页sql
     *
     * @param origSql
     *            原sql
     * @param start
     *            开始
     * @param size
     *            每页个数
     * @return 分页sql
     */
    String forDbPaginationQuery(String origSql, int start, int size);

}
