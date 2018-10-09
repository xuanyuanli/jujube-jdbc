package org.jujubeframework.jdbc.binding.sqlfunction;

/**
 * @author John Li
 */
public class NullIfSqlFunction implements BooleanSqlFunction {
    @Override
    public String convertToFreemarkerTemplate(String expression) {
        String functionCaller = getFunctionCaller(expression);
        return "!" + functionCaller + "??";
    }
}
