package com.yfs.persistence.base.util;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yfs.client.local.LocalJdbcTemplate;
import com.yfs.client.local.LocalJdbcTemplate.Column;
import com.yfs.constant.Constants;
import com.yfs.util.CamelCase;
import com.yfs.util.Ftls;
import com.yfs.util.Utils;

public class CodeGenerate {
    private static Logger logger = LoggerFactory.getLogger(CodeGenerate.class);

    private CodeGenerate() {
    }

    /** 表名中要统一替换为empty的部分 */
    public static final String REPLACE_TABLE_NAME = "";

    /**
     * 代码生成:Java类和Service
     * 
     * @param config
     *            配置
     */
    public static List<String> generateEntity(Config config) {
        Validate.notBlank(config.getTableName());
        ArrayList<String> pathList = Lists.newArrayList();
        List<String> imports = Lists.newArrayList();
        Map<String, Object> root = Maps.newHashMap();

        Connection conn = null;
        try {
            conn = LocalJdbcTemplate.getConnection();

            String schema = LocalJdbcTemplate.getSchema(conn, config.getTableName());
            String tableComment = LocalJdbcTemplate.getTableComment(conn, config.getTableName());
            String className = config.getTableName().replace(REPLACE_TABLE_NAME, "");
            className = CamelCase.toCapitalizeCamelCase(className);
            List<Column> columns = LocalJdbcTemplate.getTableStructure(conn, config.getTableName(), schema, imports);

            root.put("isCache", config.isCache());
            root.put("needComment", config.isNeedComment());
            root.put("baseEntity", Constants.BASEENTITY_NAME);
            root.put("basePackage", Constants.BASE_PACKAGE_NAME);
            root.put("currentPackage", getBasePackage(config));
            root.put("tableName", config.getTableName());
            root.put("schemaName", schema);
            if (config.needComment && StringUtils.isNotBlank(tableComment)) {
                root.put("classComment", tableComment);
            }
            root.put("className", className);
            root.put("columns", columns);
            root.put("imports", imports);

            String filePath = getPath(className, getBasePackage(config) + ".generatecode");
            boolean isExists = new File(filePath).exists();
            if ((isExists && config.isForceCoverEntity()) || !isExists) {
                Ftls.processFileTemplateToFile(getEntityTemplateName(config.isOriginal()), filePath, root);
                pathList.add(filePath);
                logger.info("生成entity文件：" + filePath);
            }

            if (config.isCreateDao) {
                String primaryKeyType = "";
                for (Column column : columns) {
                    if (column.getIsPrimaryKey()) {
                        primaryKeyType = column.getType();
                        break;
                    }
                }
                createDaoFile(className, primaryKeyType, config);
                pathList.add(getPath(className + "Dao", getBasePackage(config) + ".generatecode"));
            }
            if (config.isCreateService) {
                String primaryKeyType = "";
                for (Column column : columns) {
                    if (column.getIsPrimaryKey()) {
                        primaryKeyType = column.getType();
                        break;
                    }
                }
                createServiceFile(className, primaryKeyType, config);
                pathList.add(getPath(className + "Service", getBasePackage(config) + ".generatecode"));
            }
            if (config.isCreateServiceImpl) {
                String primaryKeyType = "";
                for (Column column : columns) {
                    if (column.getIsPrimaryKey()) {
                        primaryKeyType = column.getType();
                        break;
                    }
                }
                createServiceImplFile(className, primaryKeyType, config);
                pathList.add(getPath(className + "ServiceImpl", getBasePackage(config) + ".generatecode"));
            }
            if (config.isCreateController) {
                String primaryKeyType = "";
                for (Column column : columns) {
                    if (column.getIsPrimaryKey()) {
                        primaryKeyType = column.getType();
                        break;
                    }
                }
                createControllerlFile(className, primaryKeyType, config);
                pathList.add(getPath(className + "Controller", getBasePackage(config) + ".generatecode"));
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
        return pathList;
    }

    /**
     * 创建Service文件
     * 
     * @param config
     */
    private static void createDaoFile(String entityName, String primaryKeyType, Config config) {
        Map<String, Object> root = Maps.newHashMap();
        root.put("basePackage", Constants.BASE_PACKAGE_NAME);
        root.put("currentPackage", getBasePackage(config));
        root.put("className", entityName);
        root.put("tableName", config.getTableName());
        root.put("primaryKeyType", primaryKeyType);

        String filePath = getPath(entityName + "Dao", getBasePackage(config) + ".generatecode");
        boolean isExists = new File(filePath).exists();
        if ((isExists && config.isForceCoverDao()) || !isExists) {
            Ftls.processFileTemplateToFile("codeGenerator/generate_dao.ftl", filePath, root);
            logger.info("生成dao文件：" + filePath);
        }
    }

    /**
     * 创建Service文件
     * 
     * @param config
     */
    private static void createServiceFile(String entityName, String primaryKeyType, Config config) {
        Map<String, Object> root = Maps.newHashMap();
        root.put("basePackage", Constants.BASE_PACKAGE_NAME);
        root.put("currentPackage", getBasePackage(config));
        root.put("className", entityName);
        root.put("tableName", config.getTableName());
        root.put("primaryKeyType", primaryKeyType);

        String filePath = getPath(entityName + "Service", getBasePackage(config) + ".generatecode");
        boolean isExists = new File(filePath).exists();
        if ((isExists && config.isForceCoverService()) || !isExists) {
            Ftls.processFileTemplateToFile("codeGenerator/generate_service.ftl", filePath, root);
            logger.info("生成Service文件：" + filePath);
        }
    }

    /**
     * 创建ServiceImpl文件
     * 
     * @param config
     */
    private static void createServiceImplFile(String entityName, String primaryKeyType, Config config) {
        Map<String, Object> root = Maps.newHashMap();
        root.put("basePackage", Constants.BASE_PACKAGE_NAME);
        root.put("currentPackage", getBasePackage(config));
        root.put("className", entityName);
        root.put("tableName", config.getTableName());
        root.put("primaryKeyType", primaryKeyType);

        String filePath = getPath(entityName + "ServiceImpl", getBasePackage(config) + ".generatecode");
        boolean isExists = new File(filePath).exists();
        if ((isExists && config.isForceCoverServiceImpl()) || !isExists) {
            Ftls.processFileTemplateToFile("codeGenerator/generate_serviceImpl.ftl", filePath, root);
            logger.info("生成ServiceImpl文件：" + filePath);
        }
    }

    /**
     * 创建ServiceController文件
     * 
     * @param config
     */
    private static void createControllerlFile(String entityName, String primaryKeyType, Config config) {
        Map<String, Object> root = Maps.newHashMap();
        root.put("basePackage", Constants.BASE_PACKAGE_NAME);
        root.put("currentPackage", getBasePackage(config));
        root.put("className", entityName);
        root.put("tableName", config.getTableName());
        root.put("primaryKeyType", primaryKeyType);

        String filePath = getPath(entityName + "Controller", getBasePackage(config) + ".generatecode");
        boolean isExists = new File(filePath).exists();
        if ((isExists && config.isForceCoverController()) || !isExists) {
            Ftls.processFileTemplateToFile("codeGenerator/generate_controller.ftl", filePath, root);
            logger.info("生成controller文件：" + filePath);
        }
    }

    private static String getBasePackage(Config config) {
        return StringUtils.isBlank(config.getBasePackage()) ? Constants.BASE_PACKAGE_NAME : config.getBasePackage();
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
     * 
     * @param className
     *            类名
     * @param packageName
     *            包名
     * @return
     * @throws IOException
     */
    private static String getPath(String className, String packageName) {
        String fileName = "";
        String packagePath = packageName.replace(".", File.separator);
        if (Constants.IS_MAVEN_PROJECT) {
            fileName = join(Utils.getProjectPath(), File.separator, "src", File.separator, "main", File.separator, "java", File.separator, packagePath, File.separator, className,
                    ".java");
        } else {
            fileName = join(Utils.getProjectPath(), File.separator, "src", File.separator, packagePath, File.separator, className, ".java");
        }
        return fileName;
    }

    private static String getEntityTemplateName(boolean isOriginal) {
        String templateName = "codeGenerator/generate_entity.ftl";
        if (isOriginal) {
            templateName = "codeGenerator/generate_entity_original.ftl";
        }
        return templateName;
    }

    /**
     * 代码生成工具的配置 <br>
     * 默认开启缓存、注释、创建Service问题。<br>
     * 默认生成增删改查页面
     */
    public static class Config {
        private String tableName;
        /** 缓存 */
        private boolean isCache = false;
        /** 注释 */
        private boolean needComment = true;
        /** 创建对应Dao */
        private boolean isCreateDao = false;//
        private boolean isCreateService = false;//
        private boolean isCreateServiceImpl = false;//
        private boolean isCreateController = false;//
        /** 如果已经存在，则强制覆盖 */
        private boolean forceCoverEntity = false;
        private boolean forceCoverDao = false;
        private boolean forceCoverService = false;
        private boolean forceCoverServiceImpl = false;
        private boolean forceCoverController = false;
        /** 是否是原始版，也就是不要jpa注解的，只是一个普通的entity */
        private boolean isOriginal = true;

        private String basePackage;

        protected String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public boolean isCache() {
            return isCache;
        }

        public void setCache(boolean isCache) {
            this.isCache = isCache;
        }

        public boolean isNeedComment() {
            return needComment;
        }

        public void setNeedComment(boolean needComment) {
            this.needComment = needComment;
        }

        public boolean isCreateDao() {
            return isCreateDao;
        }

        public void setCreateDao(boolean isCreateDao) {
            this.isCreateDao = isCreateDao;
        }

        public boolean isForceCoverEntity() {
            return forceCoverEntity;
        }

        public void setForceCoverEntity(boolean forceCoverEntity) {
            this.forceCoverEntity = forceCoverEntity;
        }

        public boolean isForceCoverDao() {
            return forceCoverDao;
        }

        public void setForceCoverDao(boolean forceCoverDao) {
            this.forceCoverDao = forceCoverDao;
        }

        public boolean isOriginal() {
            return isOriginal;
        }

        public void setOriginal(boolean isOriginal) {
            this.isOriginal = isOriginal;
        }

        public String getBasePackage() {
            return basePackage;
        }

        public void setBasePackage(String basePackage) {
            this.basePackage = basePackage;
        }

        public boolean isCreateService() {
            return isCreateService;
        }

        public void setCreateService(boolean isCreateService) {
            this.isCreateService = isCreateService;
        }

        public boolean isCreateServiceImpl() {
            return isCreateServiceImpl;
        }

        public void setCreateServiceImpl(boolean isCreateServiceImpl) {
            this.isCreateServiceImpl = isCreateServiceImpl;
        }

        public boolean isCreateController() {
            return isCreateController;
        }

        public void setCreateController(boolean isCreateController) {
            this.isCreateController = isCreateController;
        }

        public boolean isForceCoverService() {
            return forceCoverService;
        }

        public void setForceCoverService(boolean forceCoverService) {
            this.forceCoverService = forceCoverService;
        }

        public boolean isForceCoverServiceImpl() {
            return forceCoverServiceImpl;
        }

        public void setForceCoverServiceImpl(boolean forceCoverServiceImpl) {
            this.forceCoverServiceImpl = forceCoverServiceImpl;
        }

        public boolean isForceCoverController() {
            return forceCoverController;
        }

        public void setForceCoverController(boolean forceCoverController) {
            this.forceCoverController = forceCoverController;
        }

    }

    /**
     * 首字母小写
     * 
     * @since 2014年6月19日 下午3:13:39
     * @author 李衡 Email：li15038043160@163.com
     * @param source
     * @return
     */
    public static String initialToLowerCase(String source) {
        return source.substring(0, 1).toLowerCase() + source.substring(1);
    }

    /**
     * 生成类型（增删改查页面）
     */
    public enum GeneratePageType {
        SELECT, UPDATE, DELETE, ADD
    }

    /**
     * 页面字段，用于代码生成
     * 
     * @author 李衡 Email：li15038043160@163.com
     * @since 2014年6月19日 下午3:21:44
     */
    public static class PageField {
        private String field;// 字段名
        private String cnField;// 字段中文名
        private int fieldType;// 字段类型。目前只有两种：1、text，2、select

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public int getFieldType() {
            return fieldType;
        }

        public void setFieldType(int fieldType) {
            this.fieldType = fieldType;
        }

        public String getCnField() {
            return cnField;
        }

        public void setCnField(String cnField) {
            this.cnField = cnField;
        }

        @Override
        public String toString() {
            return "PageField [field=" + field + ", cnField=" + cnField + ", fieldType=" + fieldType + "]";
        }

    }
}
