package org.jujubeframework.jdbc.generator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jujubeframework.jdbc.client.local.LocalJdbcTemplate;
import org.jujubeframework.util.CamelCase;
import org.jujubeframework.util.Files;
import org.jujubeframework.util.Ftls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 代码生成工具
 *
 * @author John Li Email：jujubeframework@163.com
 */
public class EntityGenerator {
    private static Logger logger = LoggerFactory.getLogger(EntityGenerator.class);

    private EntityGenerator() {
    }

    /**
     * 表名中要统一替换为empty的部分
     */
    public static final String REPLACE_TABLE_NAME = "";

    /**
     * 代码生成:Java类和Service
     *
     * @param config
     *            配置
     */
    public static void generateEntity(Config config) {
        Validate.notBlank(config.getTableName());

        List<String> imports = Lists.newArrayList();
        Map<String, Object> root = Maps.newHashMap();

        Connection conn = null;
        try {
            conn = LocalJdbcTemplate.getConnection();

            String schema = LocalJdbcTemplate.getSchema(conn, config.getTableName());
            String tableComment = LocalJdbcTemplate.getTableComment(conn, config.getTableName());
            String className = config.getTableName().replace(REPLACE_TABLE_NAME, "");
            className = CamelCase.toCapitalizeCamelCase(className);
            List<LocalJdbcTemplate.Column> columns = LocalJdbcTemplate.getTableStructure(conn, config.getTableName(), schema, imports);
            if (config.isRemoveColumnPrefix()) {
                columns.forEach(cl -> {
                    if (cl.getColName().startsWith(config.getColumnPrefix())) {
                        cl.setField(CamelCase.toSpecilCamelCase(cl.getColName().substring(config.getColumnPrefix().length())));
                    }
                });
            }
            if (config.isRemoveColumnSuffix()) {
                columns.forEach(cl -> {
                    if (cl.getColName().endsWith(config.getColumnSuffix())) {
                        cl.setField(CamelCase.toSpecilCamelCase(cl.getColName().substring(0, cl.getColName().length() - config.getColumnSuffix().length())));
                    }
                });
            }
            root.put("needComment", config.isNeedComment());
            root.put("isAddColumnAnnotation", config.isAddColumnAnnotation());
            root.put("entityPackage", config.getEntityPackageName());
            root.put("tableName", config.getTableName());
            root.put("schemaName", schema);
            if (config.needComment && StringUtils.isNotBlank(tableComment)) {
                root.put("classComment", tableComment);
            }
            root.put("className", className);
            root.put("columns", columns);
            root.put("imports", imports);

            String filePath = getPath(config, className);
            boolean isExists = new File(filePath).exists();
            boolean bool = (isExists && config.isForceCoverEntity()) || !isExists;
            if (bool) {
                Ftls.processFileTemplateToFile(getEntityTemplateName(), filePath, root);
                logger.info("生成entity文件：" + filePath);
            }

            if (config.isCreateDao()) {
                createDaoFile(className, config, columns.stream().filter(c -> c.getPrimaryKey()).findFirst().orElse(new LocalJdbcTemplate.Column()));
            }
        } catch (SQLException e1) {
            logger.error(e1.getMessage(), e1);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 创建Dao文件
     */
    private static void createDaoFile(String entityName, Config config, LocalJdbcTemplate.Column pk) {
        Map<String, Object> root = Maps.newHashMap();
        root.put("basePackage", config.getDaoPackageName());
        root.put("entityPackage", config.getEntityPackageName());
        root.put("className", entityName);
        root.put("pk", pk);
        root.put("tableName", config.getTableName());

        String filePath = getPath(config, entityName + "Dao");
        boolean isExists = new File(filePath).exists();
        boolean bool = (isExists && config.isForceCoverDao()) || !isExists;
        if (bool) {
            Ftls.processFileTemplateToFile("codeGenerator/dao.ftl", filePath, root);
            logger.info("生成dao文件：" + filePath);
        }
        if (config.isCreateDaoSqlFile()) {
            createDaoSqlFile(entityName, config);
        }
    }

    private static void createDaoSqlFile(String entityName, Config config) {
        Path daoSql = Paths.get(config.getProjectRootPath()).getParent().resolve("resources").resolve("dao-sql").resolve(entityName + "Dao.sql");
        Files.createFile(daoSql.toString());
        logger.info("生成dao sql文件：" + daoSql);
    }

    /**
     * 字符串连接
     * <p>
     * 如果涉及到并发编程，不推荐使用这个方法
     */
    public static String join(Object... elements) {
        if (elements == null || elements.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(elements.length * 16);
        for (Object t : elements) {
            sb.append(t == null ? "" : t);
        }
        return sb.toString();
    }

    /**
     * 获得输出路径
     */
    private static String getPath(Config config, String className) {
        String fileName = "";
        String projectRootPath = config.getProjectRootPath();
        boolean bool = !projectRootPath.endsWith("/") && !projectRootPath.endsWith("\\");
        if (bool) {
            projectRootPath += File.separator;
        }
        String dao = "Dao";
        if (className.endsWith(dao)) {
            fileName = join(projectRootPath, config.getDaoPackageName().replace(".", File.separator), File.separator, className, ".java");
        } else {
            fileName = join(projectRootPath, config.getEntityPackageName().replace(".", File.separator), File.separator, className, ".java");
        }
        return fileName;
    }

    private static String getEntityTemplateName() {
        return "codeGenerator/entity.ftl";
    }

    /**
     * 代码生成工具的配置
     */
    @Data
    @Accessors(chain = true)
    public static class Config {
        /**
         * 数据库表名
         */
        private String tableName;
        /**
         * 注释
         */
        private boolean needComment = true;
        /**
         * 创建对应Dao
         */
        private boolean createDao = true;
        /**
         * 创建对应Dao Sql文件
         */
        private boolean createDaoSqlFile = true;
        /**
         * 如果已经存在，则强制覆盖entity文件
         */
        private boolean forceCoverEntity = false;
        /**
         * 如果已经存在，则强制覆盖dao文件
         */
        private boolean forceCoverDao = false;
        /**
         * entity包名称
         */
        private String entityPackageName;
        /**
         * dao包名称
         */
        private String daoPackageName;
        /**
         * 项目代码放置的根目录
         */
        private String projectRootPath;

        /**是否添加Column注解*/
        private boolean addColumnAnnotation;

        /** Entity生成字段名称的时候是否移除数据库表字段前缀 */
        private boolean removeColumnPrefix;
        /** 数据库表字段前缀 */
        private String columnPrefix;

        /** Entity生成字段名称的时候是否移除数据库表字段后缀 */
        private boolean removeColumnSuffix;
        /** 数据库表字段后缀 */
        private String columnSuffix;

        /**
         * @param tableName
         *            数据库表名
         * @param projectRootPath
         *            要生成到的项目代码根目录
         * @param entityPackageName
         *            entity所在包
         * @param daoPackageName
         *            dao所在包
         */
        public Config(String tableName, String projectRootPath, String entityPackageName, String daoPackageName) {
            super();
            this.tableName = tableName;
            this.entityPackageName = entityPackageName;
            this.projectRootPath = projectRootPath;
            this.daoPackageName = daoPackageName;
        }

    }

    /**
     * 首字母小写
     *
     * @param source
     * @return
     * @author John Li Email：jujubeframework@163.com
     */
    public static String initialToLowerCase(String source) {
        return source.substring(0, 1).toLowerCase() + source.substring(1);
    }

}
