package org.jujubeframework.jdbc.binding.sqlfunction;

import org.jujubeframework.util.Texts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author John Li
 */
public class SqlFunctionContext {
    /**空格或左括号开头，中间可以有字母、小数点和`符号*/
    private static final String PREFIX = "[ (]([.\\w`]+?)";
    /**第一段是函数的()部分，这部分可以没有；第二部分是允许空格；第三部分是结尾*/
    private static final String SUFFIX = "(\\((.*?)\\))*?(\\s*?)[ )]";
    private static final String L_BRACKET = "(";
    private static final String R_BRACKET = ")";

    private static Map<String,LineSqlFunction> LINE_SQL_FUNCTION_DATA = new HashMap<>();
    private static Map<String,IfSqlFunction> IF_SQL_FUNCTION_DATA = new HashMap<>();
    static {
        LINE_SQL_FUNCTION_DATA.put(".iter",new IterSqlFunction());

        IF_SQL_FUNCTION_DATA.put(".null",new NullIfSqlFunction());
        IF_SQL_FUNCTION_DATA.put(".notNull",new NotNullIfSqlFunction());
        IF_SQL_FUNCTION_DATA.put(".empty",new EmptyIfSqlFunction());
        IF_SQL_FUNCTION_DATA.put(".notEmpty",new NotEmptyIfSqlFunction());
        IF_SQL_FUNCTION_DATA.put(".blank",new BlankIfSqlFunction());
        IF_SQL_FUNCTION_DATA.put(".notBlank",new NotBlankIfSqlFunction());
    }

    public static  boolean containsLineSqlFunction(String line){
        return  LINE_SQL_FUNCTION_DATA.keySet().stream().anyMatch(f ->line.contains(f));
    }

    public static String lineSqlFunctionExecute(final String line) {
        String result = line;
        for (String functionName : LINE_SQL_FUNCTION_DATA.keySet()) {
            LineSqlFunction lineSqlFunction = LINE_SQL_FUNCTION_DATA.get(functionName);
            List<String> ownTexts = getOwnText(functionName, line);
            for (String ownText : ownTexts) {
                String fmText = lineSqlFunction.toFreemarker(ownText);
                result =  result.replace(ownText,fmText);
            }
        }
        return  result;
    }

    /**
     * 从line中获得独立的变量.函数
     *
     * @param function
     * @param line
     * @return
     */
    private static List<String> getOwnText(String function, String line){
        String regFunction = function.replace(".", "\\.");
        String reg = PREFIX + regFunction + SUFFIX;
        List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery(reg, line);
        List<String> result = new ArrayList<>(4);
        for (Texts.RegexQueryInfo regexQueryInfo : regexQueryInfos) {
            String group = regexQueryInfo.getGroup().trim();
            if (group.startsWith(L_BRACKET)){
                group = group.substring(1).trim();
            }
            boolean bool = (!group.contains(L_BRACKET) && group.endsWith(R_BRACKET)) || group.endsWith(R_BRACKET + R_BRACKET);
            if (bool){
                group = group.substring(0,group.length()-1).trim();
            }
            result.add(group);
        }
        return  result;
    }
}
