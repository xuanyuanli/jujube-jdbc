package org.jujubeframework.jdbc.binding.sqlfunction;

import com.google.common.collect.Maps;
import org.jujubeframework.util.Collections3;
import org.jujubeframework.util.Ftls;
import org.jujubeframework.util.Texts;

/**
 * @author John Li
 */
public class IterSqlFunction implements LineSqlFunction {
    private  static  final  String FREEMARKER_PATTERN="<#list {} as x>${x}<#if x_has_next>{}</#if></#list>";
    @Override
    public String convertToFreemarkerTemplate(String expression) {
        String functionParam = getFunctionParam(expression);
        if (functionParam == null) {
            functionParam = ",";
        }
        String functionCaller = getFunctionCaller(expression);
        return Texts.format(FREEMARKER_PATTERN,functionCaller,functionParam);
    }

}
