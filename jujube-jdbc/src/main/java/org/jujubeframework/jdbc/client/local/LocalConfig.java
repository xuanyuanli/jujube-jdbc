package org.jujubeframework.jdbc.client.local;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jujubeframework.constant.Profiles;
import org.jujubeframework.util.Resources;

import java.util.Properties;

/**
 * 本地application.properties配置文件映射（只适用于Windows本地环境）
 *
 * @author John Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalConfig {

    /**
     * 全局配置文件
     */
    private static final Properties P = wrapApplicationConfig(Resources.getCurrentClasspathProperties("application.properties"));

    /**
     * ----------------------- JDBC 配置 -----------------------
     */
    public static final String JDBC_DRIVER_CLASS_NAME = P.getProperty(Name.JDBC_DRIVER_CLASS_NAME);
    public static final String JDBC_URL = P.getProperty(Name.JDBC_URL);
    public static final String JDBC_USERNAME = P.getProperty(Name.JDBC_USERNAME);
    public static final String JDBC_PASSWORD = P.getProperty(Name.JDBC_PASSWORD);

    private static Properties wrapApplicationConfig(Properties properties) {
        if (properties != null) {
            // 和spring-profiles处理一致
            if (Profiles.DEVELOPMENT.equals(Profiles.getSpringProfileFromSystemProperty())) {
                properties.putAll(Resources.getCurrentClasspathProperties("application." + Profiles.DEVELOPMENT + ".properties"));
            }

            if (StringUtils.isBlank(properties.getProperty(Name.JDBC_DRIVER_CLASS_NAME))) {
                properties.setProperty(Name.JDBC_DRIVER_CLASS_NAME, "com.mysql.cj.jdbc.Driver");
            }
        }
        return properties;
    }

    /**
     * 配置文件中的name值
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Name {

        public static final String JDBC_DRIVER_CLASS_NAME = "spring.datasource.driver-class-name";
        public static final String JDBC_URL = "spring.datasource.url";
        public static final String JDBC_USERNAME = "spring.datasource.username";
        public static final String JDBC_PASSWORD = "spring.datasource.password";

    }
}
