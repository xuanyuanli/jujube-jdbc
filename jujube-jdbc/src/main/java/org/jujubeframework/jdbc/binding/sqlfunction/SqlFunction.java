package org.jujubeframework.jdbc.binding.sqlfunction;

import org.jujubeframework.util.Texts;

/**
 * 自定义模板中的函数接口
 *
 * @author John Li
 */
public interface SqlFunction {
    /**
     * 转换为Freemarker模板
     *
     * @param expression 原自定义函数
     * @return Freemarker模板
     */
    String convertToFreemarkerTemplate(String expression);

    /**
     * 获得方法参数
     */
    default String getFunctionParam(String expression) {
        String reg = "\\(('|\")(.*?)\\1\\)";
        String group = null;
        String[] groups = Texts.getGroups(reg, expression);
        int three = 3;
        if (groups.length == three) {
            group = groups[2];
        }
        return group;
    }

    /**
     * 获得方法调用者
     */
    default String getFunctionCaller(String expression) {
        return expression.substring(0, expression.indexOf("."));
    }
}
