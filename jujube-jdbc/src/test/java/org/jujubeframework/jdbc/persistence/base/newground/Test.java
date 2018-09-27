package org.jujubeframework.jdbc.persistence.base.newground;

import org.jujubeframework.util.Beans;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Test {
    public static void main(String[] args) throws Throwable {
        System.out.println(Beans.invokeInterfaceDefault(Beans.getDeclaredMethod(ChildOneInterface.class,"getTableName")));
    }

    public  static  void  test()throws Throwable {
    }
}
