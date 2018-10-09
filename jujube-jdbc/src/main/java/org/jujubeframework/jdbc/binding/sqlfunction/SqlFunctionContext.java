package org.jujubeframework.jdbc.binding.sqlfunction;

import org.jujubeframework.util.Texts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sql中的函数处理
 *
 * @author John Li
 */
public class SqlFunctionContext {
    /**
     * 空格或左括号开头，中间可以有字母、小数点和`符号
     */
    private static final String PREFIX = "[ (]([.\\w`]+?)";
    /**
     * 第一段是函数的()部分，这部分可以没有；第二部分是允许空格；第三部分是结尾
     */
    private static final String SUFFIX = "(\\((.*?)\\))*?(\\s*?)[ )]";

    private static final String L_BRACKET = "(";
    private static final String R_BRACKET = ")";
    private static final String SPACE = " ";
    private static final String DOT_MARK = ".";
    private static final String ESCAPE_DOT_MARK = "\\.";

    private static Map<String, LineSqlFunction> LINE_SQL_FUNCTION_DATA = new HashMap<>();
    private static Map<String, BooleanSqlFunction> BOOLEAN_SQL_FUNCTION_DATA = new HashMap<>();
    private static IfSqlKeyword IF_KEYWORK = new IfSqlKeyword();

    static {
        LINE_SQL_FUNCTION_DATA.put(".iter", new IterSqlFunction());

        BOOLEAN_SQL_FUNCTION_DATA.put(".notBlank", new NotBlankIfSqlFunction());
        BOOLEAN_SQL_FUNCTION_DATA.put(".notNull", new NotNullIfSqlFunction());
        BOOLEAN_SQL_FUNCTION_DATA.put(".null", new NullIfSqlFunction());
        BOOLEAN_SQL_FUNCTION_DATA.put(".blank", new BlankIfSqlFunction());
    }

    public static boolean containsLineSqlFunction(String line) {
        return LINE_SQL_FUNCTION_DATA.keySet().stream().anyMatch(f -> line.contains(f));
    }

    public static String lineSqlFunctionExecute(final String line) {
        String result = line;
        for (String functionName : LINE_SQL_FUNCTION_DATA.keySet()) {
            LineSqlFunction lineSqlFunction = LINE_SQL_FUNCTION_DATA.get(functionName);
            List<String> ownTexts = getExpression(functionName, line);
            for (String ownText : ownTexts) {
                String fmText = lineSqlFunction.convertToFreemarkerTemplate(ownText);
                result = result.replace(ownText, fmText);
            }
        }
        return result;
    }

    /**
     * boolean函数表达式处理
     *
     * @param pexpression boolean表达式
     * @return
     */
    public static String booleanSqlFunctionExecute(final String pexpression) {
        String result = pexpression;
        for (String functionName : BOOLEAN_SQL_FUNCTION_DATA.keySet()) {
            BooleanSqlFunction sqlFunction = BOOLEAN_SQL_FUNCTION_DATA.get(functionName);
            List<String> expressions = getExpression(functionName, pexpression);
            for (String expression : expressions) {
                String fmText = sqlFunction.convertToFreemarkerTemplate(expression);
                result = result.replace(expression, fmText);
            }
        }
        result = booleanExpressionPostProcess(result);
        return result;
    }

    /**
     * boolean表达式后置处理，替换一些特殊符号
     */
    private static String booleanExpressionPostProcess(String expression) {
        return expression.replace(">", " gt ").replace("<", " lt ").replace(">=", " gte ").replace("<=", " lte ");
    }

    public static String ifKeyworkBeforeProcess(String line) {
        return IF_KEYWORK.beforeConvert(line);
    }

    public static String ifKeyworkPostProcess() {
        return IF_KEYWORK.postConvert();
    }

    /**
     * 从line中获得独立的(变量.函数)
     *
     * @param function
     * @param line
     * @return
     */
    private static List<String> getExpression(String function, String line) {
        line = SPACE + line + SPACE;
        List<String> result = new ArrayList<>();
        String regFunction = function.replace(DOT_MARK, ESCAPE_DOT_MARK);
        String reg = PREFIX + regFunction + SUFFIX;
        List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery(reg, line);
        for (Texts.RegexQueryInfo regexQueryInfo : regexQueryInfos) {
            String group = regexQueryInfo.getGroup().trim();
            if (group.startsWith(L_BRACKET)) {
                group = group.substring(1).trim();
            }
            boolean bool = (!group.contains(L_BRACKET) && group.endsWith(R_BRACKET)) || group.endsWith(R_BRACKET + R_BRACKET);
            if (bool) {
                group = group.substring(0, group.length() - 1).trim();
            }
            result.add(group);
        }
        return result;
    }
}
