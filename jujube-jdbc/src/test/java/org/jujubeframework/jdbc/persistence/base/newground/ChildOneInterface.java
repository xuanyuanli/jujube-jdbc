package org.jujubeframework.jdbc.persistence.base.newground;

public interface ChildOneInterface extends OneInterface{
   default String getTableName(){
       return  "user";
   }
}
