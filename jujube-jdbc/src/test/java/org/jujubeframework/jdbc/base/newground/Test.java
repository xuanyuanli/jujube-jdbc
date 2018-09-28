package org.jujubeframework.jdbc.base.newground;

import org.jujubeframework.util.Beans;

public class Test {
    public static void main(String[] args) throws Throwable {
        System.out.println(Beans.invokeInterfaceDefault(Beans.getDeclaredMethod(ChildOneInterface.class,"getTableName")));
    }

    public  static  void  test()throws Throwable {
    }
}
