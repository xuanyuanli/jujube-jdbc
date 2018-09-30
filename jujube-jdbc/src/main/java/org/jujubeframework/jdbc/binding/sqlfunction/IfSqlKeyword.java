package org.jujubeframework.jdbc.binding.sqlfunction;

/**
 * Sql中的关键字@if处理
 * @author John Li
 */
public class IfSqlKeyword{

    public String beforeConvert(String line) {
        return "<#if "+line+">";
    }

    public String postConvert() {
        return "</#if>";
    }
}
