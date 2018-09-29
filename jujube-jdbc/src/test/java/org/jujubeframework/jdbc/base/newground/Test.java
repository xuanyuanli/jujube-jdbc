package org.jujubeframework.jdbc.base.newground;

import org.jujubeframework.util.Texts;

import java.util.ArrayList;
import java.util.List;

public class Test {

    private static final String PREFIX = "[ (]([.\\w`]+?)";
    private static final String SUFFIX = "(\\((.*?)\\))*?(\\s*?)[ )]";
    private static final String L_BRACKET = "(";
    private static final String R_BRACKET = ")";

    public static void main(String[] args) throws Throwable {
        String function = "name.iter(',')";
        String reg = "\\(('|\")(.*?)\\1\\)";
        String[] groups = Texts.getGroups(reg, function);
        if (groups.length==3){

        }
        for (String group : groups) {
            System.out.println(group);
        }
    }


    public static void test() throws Throwable {
        List<String> ownText = getOwnText(".iter", "id in(ids.iter(',')) and name in names.iter(',') (a.iter(',') ) ( b.iter) c.iter");
        for (String text : ownText) {
            System.out.println(text);
        }
    }

    private static List<String> getOwnText(String function, String line) {
        line = " " + line + " ";
        String regFunction = function.replace(".", "\\.");
        String reg = PREFIX + regFunction + SUFFIX;
        List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery(reg, line);
        List<String> result = new ArrayList<>(4);
        for (Texts.RegexQueryInfo regexQueryInfo : regexQueryInfos) {
            String group = regexQueryInfo.getGroup().trim();
            if (group.startsWith(L_BRACKET)) {
                group = group.substring(1).trim();
            }
            if ((!group.contains(L_BRACKET) && group.endsWith(R_BRACKET)) || group.endsWith(R_BRACKET + R_BRACKET)) {
                group = group.substring(0, group.length() - 1).trim();
            }
            result.add(group);
        }
        return result;
    }
}
