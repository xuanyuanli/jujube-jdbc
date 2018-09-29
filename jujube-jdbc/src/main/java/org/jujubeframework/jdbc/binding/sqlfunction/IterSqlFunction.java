package org.jujubeframework.jdbc.binding.sqlfunction;

import org.jujubeframework.util.Texts;

/**
 * @author John Li
 */
public class IterSqlFunction implements LineSqlFunction {
    private  static  final  String FREEMARKER_PATTERN="<#list {} as x>${x}<#if x_has_next>{}</#if></#list>";
    @Override
    public String toFreemarker(String originText) {

        return null;
    }

}
