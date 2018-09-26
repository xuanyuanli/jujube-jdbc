package org.jujubeframework.jdbc.generator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jujubeframework.jdbc.client.local.LocalJdbcTemplate;
import org.jujubeframework.util.CamelCase;
import org.jujubeframework.util.Ftls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 代码生成工具
 * 
 * @author John Li Email：jujubeframework@163.com
 */
public class EntityGenerator {
	private static Logger logger = LoggerFactory.getLogger(EntityGenerator.class);

	private EntityGenerator() {
	}

	/** 表名中要统一替换为empty的部分 */
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

			root.put("needComment", config.isNeedComment());
			root.put("basePackage", config.getEntityPackageName());
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
			if ((isExists && config.isForceCoverEntity()) || !isExists) {
				Ftls.processFileTemplateToFile(getEntityTemplateName(config.isOriginal()), filePath, root);
				logger.info("生成entity文件：" + filePath);
			}

			if (config.isCreateDao) {
				createDaoFile(className, config);
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
	 * 创建Service文件
	 * 
	 * @param config
	 */
	private static void createDaoFile(String entityName, Config config) {
		Map<String, Object> root = Maps.newHashMap();
		root.put("basePackage", config.getDaoPackageName());
		root.put("entityPackage", config.getEntityPackageName());
		root.put("className", entityName);
		root.put("tableName", config.getTableName());

		String filePath = getPath(config,entityName + "Dao");
		boolean isExists = new File(filePath).exists();
        boolean bool = (isExists && config.isForceCoverDao()) || !isExists;
        if (bool) {
			Ftls.processFileTemplateToFile("codeGenerator/dao.ftl", filePath, root);
			logger.info("生成dao文件：" + filePath);
		}
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
		if (className.endsWith("Dao")) {
			fileName = join(projectRootPath, config.getDaoPackageName().replace(".", File.separator), File.separator,className, ".java");
		} else {
			fileName = join(projectRootPath, config.getEntityPackageName().replace(".", File.separator),File.separator, className, ".java");
		}
		return fileName;
	}

	private static String getEntityTemplateName(boolean isOriginal) {
		String templateName = "codeGenerator/entity.ftl";
		if (isOriginal) {
			templateName = "codeGenerator/entity_original.ftl";
		}
		return templateName;
	}

	/**
	 * 代码生成工具的配置
	 */
	public static class Config {
		/** 数据库表名 */
		private String tableName;
		/** 注释 */
		private boolean needComment = true;
		/** 创建对应Dao */
		private boolean isCreateDao = true;//
		/** 如果已经存在，则强制覆盖 */
		private boolean forceCoverEntity = false;
		private boolean forceCoverDao = false;
		/** 是否是原始版，也就是不要jpa注解的，只是一个普通的entity */
		private boolean isOriginal = true;

		private String entityPackageName;
		private String daoPackageName;

		private String projectRootPath;

		/**
		 * 
		 * @param tableName	数据库表名
		 * @param projectRootPath	要生成到的项目代码根目录
		 * @param entityPackageName	entity所在包
		 * @param daoPackageName	dao所在包
		 */
		public Config(String tableName, String projectRootPath, String entityPackageName, String daoPackageName) {
			super();
			this.tableName = tableName;
			this.entityPackageName = entityPackageName;
			this.projectRootPath = projectRootPath;
			this.daoPackageName = daoPackageName;
		}

		public String getTableName() {
			return tableName;
		}

		public boolean isNeedComment() {
			return needComment;
		}

		public boolean isCreateDao() {
			return isCreateDao;
		}

		public boolean isForceCoverEntity() {
			return forceCoverEntity;
		}

		public boolean isForceCoverDao() {
			return forceCoverDao;
		}

		public boolean isOriginal() {
			return isOriginal;
		}

		public String getEntityPackageName() {
			return entityPackageName;
		}

		public String getDaoPackageName() {
			return daoPackageName;
		}

		public String getProjectRootPath() {
			return projectRootPath;
		}

		public void setNeedComment(boolean needComment) {
			this.needComment = needComment;
		}

		public void setCreateDao(boolean isCreateDao) {
			this.isCreateDao = isCreateDao;
		}

		public void setForceCoverEntity(boolean forceCoverEntity) {
			this.forceCoverEntity = forceCoverEntity;
		}

		public void setForceCoverDao(boolean forceCoverDao) {
			this.forceCoverDao = forceCoverDao;
		}
		
		

	}

	/**
	 * 首字母小写
	 * 
	 * @author John Li Email：jujubeframework@163.com
	 * @param source
	 * @return
	 */
	public static String initialToLowerCase(String source) {
		return source.substring(0, 1).toLowerCase() + source.substring(1);
	}

}
