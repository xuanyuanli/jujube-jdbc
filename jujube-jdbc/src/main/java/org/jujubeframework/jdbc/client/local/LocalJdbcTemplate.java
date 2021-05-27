package org.jujubeframework.jdbc.client.local;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jujubeframework.util.CamelCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Jdbc相关工具
 *
 * @author John Li Email：jujubeframework@163.com
 */
public class LocalJdbcTemplate {

    private static final Logger logger = LoggerFactory.getLogger(LocalJdbcTemplate.class);
    public static final int INT_LIMIT = 10;

    private LocalJdbcTemplate() {
    }

    private static DriverManagerDataSource dataSource;

    static {
        setDataSourceInfo(LocalConfig.JDBC_URL, LocalConfig.JDBC_DRIVER_CLASS_NAME, LocalConfig.JDBC_USERNAME, LocalConfig.JDBC_PASSWORD);
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 可设置DataSource
     */
    public static void setDataSourceInfo(String jdbcUrl, String driverClassName, String username, String pwd) {
        logger.info("init datasource：url:{},driverClass:{},username:{},pwd:{}", jdbcUrl, driverClassName, username, pwd);

        dataSource = new DriverManagerDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(pwd);
        dataSource.setDriverClassName(driverClassName);
    }

    private final static ThreadLocal<JdbcTemplate> JDBC_TEMPLATE_THREAD_LOCAL = new ThreadLocal<JdbcTemplate>() {
        @Override
        protected JdbcTemplate initialValue() {
            return new JdbcTemplate(dataSource);
        }
    };

    public static JdbcTemplate getJdbcTemplate() {
        return JDBC_TEMPLATE_THREAD_LOCAL.get();
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
        long result;
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                if (args != null && args.length != 0) {
                    for (int j = 0; j < args.length; j++) {
                        ps.setObject(j + 1, args[j]);
                    }
                }
                return ps;
            }, keyHolder);
            result = keyHolder.getKey().longValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 获得数据库连接
     */
    public static Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    /**
     * 获得表注释
     */
    public static String getTableComment(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tableRet = metaData.getTables(null, "%", tableName, new String[]{"TABLE"});
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
        ResultSet tableRet = metaData.getTables(null, "%", tableName, new String[]{"TABLE"});
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
     * @author John Li Email：jujubeframework@163.com
     */
    public static List<Column> getTableStructure(Connection conn, String tableName, String schema, List<String> imports) throws SQLException {
        List<Column> columns = new ArrayList<>();

        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet colRet = metaData.getColumns(null, schema, tableName, "%");

        // 找到主键
        String primaryKey = getPkname(conn, tableName, schema);

        // 生成字段
        while (colRet.next()) {
            String colName = colRet.getString("COLUMN_NAME");
            int dataType = colRet.getInt("DATA_TYPE");
            String comment = colRet.getString("REMARKS");
            String columnSize = colRet.getString("COLUMN_SIZE");

            int precision = NumberUtils.toInt(columnSize, 255);
            String field = CamelCase.toSpecilCamelCase(colName.toLowerCase());
            String type = typeMappingOfMySql(dataType, precision, imports);
            boolean isPk = colName.equals(primaryKey);
            columns.add(new Column(field, colName, type, comment, precision, isPk));
        }
        return columns;
    }

    /**
     * 获得主键名称(数据库字段名)
     */
    public static String getPkname(Connection conn, String tableName, String schema) throws SQLException {
        String primaryKey = "";
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet pkSet = metaData.getPrimaryKeys(schema, schema, tableName);
        if (pkSet.first()) {
            // COLUMN_NAME
            primaryKey = pkSet.getString(4);
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
                if (precision > INT_LIMIT) {
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
            // oracle 链路
            case Types.DATALINK:
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

    public static String convertDatabaseCharsetType(String in, String type) {
        String dbUser;
        if (in != null) {
            if ("oracle".equals(type)) {
                dbUser = in.toUpperCase();
            } else if ("postgresql".equals(type)) {
                dbUser = "public";
            } else if ("mysql".equals(type)) {
                dbUser = null;
            } else if ("mssqlserver".equals(type)) {
                dbUser = null;
            } else if ("db2".equals(type)) {
                dbUser = in.toUpperCase();
            } else {
                dbUser = in;
            }
        } else {
            dbUser = "public";
        }
        return dbUser;
    }

    /**获得数据库的所有表*/
    public static List<String> getTables(String catalog) {
        Connection conn = getConnection();
        List<String> list = new ArrayList<>();
        try {
            DatabaseMetaData dbMetData = conn.getMetaData();
            ResultSet rs = dbMetData.getTables(catalog, convertDatabaseCharsetType("root", "mysql"), null, new String[]{"TABLE"});

            while (rs.next()) {
                boolean bool = rs.getString(4) != null && ("TABLE".equalsIgnoreCase(rs.getString(4)) || "VIEW".equalsIgnoreCase(rs.getString(4)));
                if (bool) {
                    String tableName = rs.getString(3).toLowerCase();
                    list.add(tableName);
                }
            }
        } catch (SQLException e) {
            logger.error("getTables",e);
        }
        return  list;
    }

    @Data
    public static class Column {
        private String field;
        private String colName;
        private String type;
        private String comment;
        private Integer precision;
        private Boolean primaryKey;

        public Column() {
        }

        public Column(String field, String originName, String type, String comment, Integer precision, Boolean primaryKey) {
            super();
            this.field = field;
            this.colName = originName;
            this.type = type;
            this.comment = comment;
            this.precision = precision;
            this.primaryKey = primaryKey;
        }

    }
}
