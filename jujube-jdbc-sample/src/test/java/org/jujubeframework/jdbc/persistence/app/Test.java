package org.jujubeframework.jdbc.persistence.app;

import org.jujubeframework.jdbc.JujubeJdbcApp;
import org.jujubeframework.jdbc.persistence.UserDao;
import org.jujubeframework.util.Beans;

public class Test {
    public static void main(String[] args) {
        Class<?> daoInterface = UserDao.class;
        Class<?> realGenericType = Beans.getClassGenericType(daoInterface, 0);
        Class<?> realPrimayKeyType = Beans.getClassGenericType(daoInterface, 1);
    }
}
