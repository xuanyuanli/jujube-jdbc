package org.jujubeframework.jdbc.base.newground;

public interface ChildOneInterface extends OneInterface{
   default String getTableName(){
       return  "user";
   }
}
