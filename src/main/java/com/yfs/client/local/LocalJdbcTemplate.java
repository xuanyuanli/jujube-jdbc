package com.yfs.client.local;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.yfs.util.CamelCase;

/**
 * Jdbc相关工具
 * 
 * @author 李衡 Email：li15038043160@163.com
 */
public class LocalJdbcTemplate {

    private static final Logger logger = LoggerFactory.getLogger(LocalJdbcTemplate.class);

    private LocalJdbcTemplate() {
    }

    private static DriverManagerDataSource dataSource;
    static {
        setDataSourceInfo(LocalConfig.JDBC_URL, LocalConfig.JDBC_DRIVER_CLASS_NAME, LocalConfig.JDBC_USERNAME, LocalConfig.JDBC_PASSWORD);
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    /** 可设置DataSource */
    public static void setDataSourceInfo(String jdbcUrl, String driverClassName, String username, String pwd) {
        logger.info("init datasource：url:{},driverClass:{},username:{},pwd:{}", jdbcUrl, driverClassName, username, pwd);

        dataSource = new DriverManagerDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(pwd);
        dataSource.setDriverClassName(driverClassName);
    }

    private static ThreadLocal<JdbcTemplate> jdbcTemplates = new ThreadLocal<JdbcTemplate>() {
        @Override
        protected JdbcTemplate initialValue() {
            return new JdbcTemplate(dataSource);
        }
    };

    public static JdbcTemplate getJdbcTemplate() {
        return jdbcTemplates.get();
    }

    /**
     * 处理过异常的queryForObject()方法
     */
    public static <T> T queryForObject(JdbcTemplate jdbcTemplate, String sql, Class<T> cl, Object... args) {
        try {
            return jdbcTemplate.queryForObject(sql, cl, args);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 可以返回id的更新方法,如果更新失败，会返回-1
     */
    public static long save(JdbcTemplate jdbcTemplate, final String sql, final Object... args) {
        long result = -1;
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    if (args != null && args.length != 0) {
                        for (int j = 0; j < args.length; j++) {
                            ps.setObject(j + 1, args[j]);
                        }
                    }
                    return ps;
                }
            }, keyHolder);
            result = keyHolder.getKey().longValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 获得数据库连接
     * 
     * @param applicationProperties
     *            环境配置文件
     */
    public static Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    /**
     * 获得表注释
     */
    public static String getTableComment(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tableRet = metaData.getTables(null, "%", tableName, new String[] { "TABLE" });
        String tableComment = "";
        if (tableRet.first()) {
            tableComment = tableRet.getString("REMARKS");
        }
        return tableComment;
    }

    /**
     * 获得schema
     */
    public static String getSchema(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tableRet = metaData.getTables(null, "%", tableName, new String[] { "TABLE" });
        String schema = "";
        if (tableRet.first()) {
            schema = tableRet.getString("TABLE_CAT");
            if (StringUtils.isBlank(schema)) {
                schema = tableRet.getString("TABLE_SCHEM");
            }
        }
        return schema;
    }

    /**
     * 获得表结构
     * 
     * @since 2014年1月7日 上午11:18:29
     * @author 李衡 Email：li15038043160@163.com
     */
    public static List<Column> getTableStructure(Connection conn, String tableName, String schema, List<String> imports) throws SQLException {
        List<Column> columns = new ArrayList<>();

        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet colRet = metaData.getColumns(null, schema, tableName, "%");

        // 找到主键
        String primaryKey = getPKname(conn, tableName, schema);

        // 生成字段
        while (colRet.next()) {
            String colName = colRet.getString("COLUMN_NAME");
            int dataType = colRet.getInt("DATA_TYPE");
            String comment = colRet.getString("REMARKS");
            String columnSize = colRet.getString("COLUMN_SIZE");

            int precision = NumberUtils.toInt(columnSize, 255);
            String field = CamelCase.toSpecilCamelCase(colName);
            String type = typeMappingOfMySql(dataType, precision, imports);
            boolean isPK = colName.equals(primaryKey) ? true : false;
            columns.add(new Column(field, colName, type, comment, precision, isPK));
        }
        return columns;
    }

    /**
     * 获得主键名称(数据库字段名)
     */
    public static String getPKname(Connection conn, String tableName, String schema) throws SQLException {
        String primaryKey = "";
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet pkRSet = metaData.getPrimaryKeys(schema, schema, tableName);
        if (pkRSet.first()) {
            primaryKey = pkRSet.getString(4); // COLUMN_NAME
        }
        return primaryKey;
    }

    /**
     * 类型映射（把数据库类型映射为Java类型）<br>
     * 参考：<a href=
     * "http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-type-conversions.html"
     * >Java, JDBC and MySQL Types</a>
     */
    public static String typeMappingOfMySql(int dataType, int precision, List<String> imports) {
        String javaType = "";
        if (imports == null) {
            imports = new ArrayList<>();
        }
        switch (dataType) {
        case Types.INTEGER:
        case Types.BIT:
        case Types.SMALLINT:
        case Types.TINYINT:
        case Types.BOOLEAN:
            if (precision > 10) {
                javaType = "Long";
            } else {
                javaType = "Integer";
            }
            break;
        case Types.BIGINT:
            javaType = "Long";
            break;
        case Types.FLOAT:
        case Types.REAL:
            javaType = "Float";
            break;
        case Types.DECIMAL:
        case Types.DOUBLE:
        case Types.NUMERIC:
            javaType = "Double";
            break;
        case Types.BLOB:
        case Types.BINARY:
        case Types.LONGVARBINARY:
        case Types.VARBINARY:
            javaType = "Byte[]";
            break;
        case Types.CHAR:
        case Types.CLOB:
        case Types.VARCHAR:
        case Types.LONGNVARCHAR:
        case Types.LONGVARCHAR:
        case Types.NCHAR:
        case Types.NCLOB:
        case Types.NVARCHAR:
            javaType = "String";
            break;
        case Types.TIME:
            imports.add("java.sql.Time");
            javaType = "Time";
            break;
        case Types.TIMESTAMP:
            imports.add("java.sql.Timestamp");
            javaType = "Timestamp";
            break;
        case Types.DATE:
            imports.add("java.sql.Date");
            javaType = "Date";
            break;
        case Types.DATALINK: // oracle 链路
        case Types.DISTINCT:
        case Types.JAVA_OBJECT:
        case Types.NULL:
        case Types.OTHER:
        case Types.REF:
        case Types.ROWID:
        case Types.SQLXML:
        case Types.STRUCT:
            break;
        default:
            break;
        }
        return javaType;
    }

    public static class Column {
        private String field;
        private String colName;
        private String type;
        private String comment;
        private Integer precision;
        private Boolean isPrimaryKey;

        public Column(String field, String originName, String type, String comment, Integer precision, Boolean isPrimaryKey) {
            super();
            this.field = field;
            this.colName = originName;
            this.type = type;
            this.comment = comment;
            this.precision = precision;
            this.isPrimaryKey = isPrimaryKey;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getColName() {
            return colName;
        }

        public void setColName(String colName) {
            this.colName = colName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Integer getPrecision() {
            return precision;
        }

        public void setPrecision(Integer precision) {
            this.precision = precision;
        }

        public Boolean getIsPrimaryKey() {
            return isPrimaryKey;
        }

        public void setIsPrimaryKey(Boolean isPrimaryKey) {
            this.isPrimaryKey = isPrimaryKey;
        }
    }
}
