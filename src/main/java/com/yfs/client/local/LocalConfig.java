package com.yfs.client.local;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.yfs.constant.Profiles;
import com.yfs.util.Utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 本地application.properties配置文件映射（只适用于Windows本地环境） */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalConfig {

    /** 全局配置文件 */
    private static final Properties P = wrapApplicationConfig(Utils.getCurrentClasspathProperties("application.properties"));

    // ----------------------- JDBC 配置 -----------------------
    public static final String JDBC_DRIVER_CLASS_NAME = P.getProperty(Name.JDBC_DRIVER_CLASS_NAME);
    public static final String JDBC_URL = P.getProperty(Name.JDBC_URL);
    public static final String JDBC_USERNAME = P.getProperty(Name.JDBC_USERNAME);
    public static final String JDBC_PASSWORD = P.getProperty(Name.JDBC_PASSWORD);

    private static Properties wrapApplicationConfig(Properties properties) {
        if (properties != null) {
            if (Profiles.DEVELOPMENT.equals(Profiles.getSpringProfileAsSystemProperty())) { // 和spring-profiles处理一致
                properties.putAll(Utils.getCurrentClasspathProperties("application." + Profiles.DEVELOPMENT + ".properties"));
            }

            if (StringUtils.isBlank(properties.getProperty(Name.JDBC_DRIVER_CLASS_NAME))) {
                properties.setProperty(Name.JDBC_DRIVER_CLASS_NAME, "com.mysql.jdbc.Driver");
            }
        }
        return properties;
    }

    /** 配置文件中的name值 */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Name {

        public static final String JDBC_DRIVER_CLASS_NAME = "spring.datasource.driver-class-name";
        public static final String JDBC_URL = "spring.datasource.url";
        public static final String JDBC_USERNAME = "spring.datasource.username";
        public static final String JDBC_PASSWORD = "spring.datasource.password";

    }
}
