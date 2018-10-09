package org.jujubeframework.jdbc.binding.sqlfunction;

/**
 * @author John Li
 */
public class NotBlankIfSqlFunction implements BooleanSqlFunction {
    @Override
    public String convertToFreemarkerTemplate(String expression) {
        String functionCaller = getFunctionCaller(expression);
        return "(" + functionCaller + "?? && " + functionCaller + "!='')";
    }
}
