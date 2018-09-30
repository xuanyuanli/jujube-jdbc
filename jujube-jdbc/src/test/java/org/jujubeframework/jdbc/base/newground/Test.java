package org.jujubeframework.jdbc.base.newground;

import org.jujubeframework.util.Texts;

import java.util.ArrayList;
import java.util.List;

public class Test {

    /**空格或左括号开头，中间可以有字母、小数点和`符号*/
    private static final String PREFIX = "[ (]([.\\w`]+?)";
    /**第一段是函数的()部分，这部分可以没有；第二部分是允许空格；第三部分是结尾*/
    private static final String SUFFIX = "(\\((.*?)\\))*?(\\s*?)[ )]";
    private static final String L_BRACKET = "(";
    private static final String R_BRACKET = ")";

    public static void main(String[] args) throws Throwable {
        String freemarkerSql="like '%#{name}%', '#{age}' , '%#{part}' ,'#{uid}%'";
        List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery("(['%])#\\{(\\w+?)\\}(['%])", freemarkerSql);
        for (Texts.RegexQueryInfo regexQueryInfo : regexQueryInfos) {
            List<String> groups = regexQueryInfo.getGroups();
            freemarkerSql= freemarkerSql.replace(regexQueryInfo.getGroup(), groups.get(0)+"${"+groups.get(1)+"}"+groups.get(2));
            System.out.println(regexQueryInfo.getGroups().get(0)+"\t"+regexQueryInfo.getGroups().get(1)+"\t"+regexQueryInfo.getGroups().get(2));
        }
        System.out.println(freemarkerSql);
    }


    public static void test() throws Throwable {
        List<String> ownText = getExpression(".iter", "id in(ids.iter(',')) and name in names.iter(',') (a.iter(',') ) ( b.iter) c.iter");
        for (String text : ownText) {
            System.out.println(text);
        }
    }

    /**
     * 从line中获得独立的(变量.函数)
     *
     * @param function
     * @param line
     * @return
     */
    private static List<String> getExpression(String function, String line){
        line = " "+line+" ";
        List<String> result = new ArrayList<>();
        String regFunction = function.replace(".", "\\.");
        String reg = PREFIX + regFunction + SUFFIX;
        List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery(reg, line);
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
