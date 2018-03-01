package com.yfs.constant;

import com.yfs.util.Beans;

/** Spring Profiles */
public class Profiles {

    private static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";

    private Profiles() {
    }

    public static void setSpringProfileAsSystemProperty(String profile) {
        System.setProperty(SPRING_PROFILES_ACTIVE, profile);
    }

    public static String getSpringProfileAsSystemProperty() {
        return System.getProperty(SPRING_PROFILES_ACTIVE);
    }

    /** 是否是测试环境 */
    public static boolean isTestProfile() {
        try {
            Beans.forName(Constants.H2_DRIVER_CLASS_NAME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static final String ACTIVE_PROFILE = SPRING_PROFILES_ACTIVE;
    public static final String DEFAULT_PROFILE = "spring.profiles.default";
    public static final String PRODUCTION = "prod";
    public static final String DEVELOPMENT = "dev";
    public static final String UNIT_TEST = "test";
    public static final String FUNCTIONAL_TEST = "func";
}