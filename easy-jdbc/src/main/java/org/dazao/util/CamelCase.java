package org.dazao.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

/**
 * 驼峰命名法转换工具
 * 
 * <pre>
 *  CamelCaseUtils.toCamelCase("orderId") == "orderId"
 *  CamelCaseUtils.toCamelCase("hello_world") == "helloWorld" 
 *  CamelCaseUtils.toCapitalizeCamelCase("hello_world") == "HelloWorld"
 *  CamelCaseUtils.toUnderScoreCase("helloWorld") = "hello_world"
 * </pre>
 * 
 * @author 李衡 Email：li15038043160@163.com
 */
public class CamelCase {

    private CamelCase() {
    }

    /** 字段转换的缓存 */
    private static Map<String, String> underLine_fieldCache = new ConcurrentHashMap<String, String>();
    private static Map<String, String> camelCase_fieldCache = new ConcurrentHashMap<String, String>();

    private static final char SEPARATOR = '_';

    /**
     * 遇大写，则转换为下划线形式+小写
     */
    public static String toUnderlineName(String input) {
        if (input == null) {
            return "";
        }

        // 先从cache中取值
        String cache = underLine_fieldCache.get(input);
        if (cache != null) {
            return cache;
        }

        StringBuilder resultSb = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            boolean nextUpperCase = true;

            if (i < (input.length() - 1)) {
                nextUpperCase = Character.isUpperCase(input.charAt(i + 1));
            }

            if ((i >= 0) && Character.isUpperCase(c)) {
                if (!upperCase || !nextUpperCase) {
                    if (i > 0) {
                        resultSb.append(SEPARATOR);
                    }
                }
                upperCase = true;
            } else {
                upperCase = false;
            }

            resultSb.append(Character.toLowerCase(c));
        }

        String result = resultSb.toString();
        underLine_fieldCache.put(input, result);
        return result;
    }

    /**
     * 下划线写法转换为驼峰写法
     */
    public static String toCamelCase(String input) {
        if (input == null) {
            return "";
        }
        if (!input.contains(String.valueOf(SEPARATOR))) {
            return input;
        }

        // 先从cache中取值
        String cache = camelCase_fieldCache.get(input);
        if (cache != null) {
            return cache;
        }

        String curInput = input;
        StringBuilder resultSb = new StringBuilder(curInput.length());
        boolean upperCase = false;
        for (int i = 0; i < curInput.length(); i++) {
            char c = curInput.charAt(i);
            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                resultSb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                resultSb.append(c);
            }
        }

        String result = resultSb.toString();
        camelCase_fieldCache.put(input, result);
        return result;
    }

    /**
     * 特殊的转换：前两个字母不能大写
     */
    public static String toSpecilCamelCase(String input) {
        // 先从cache中取值
        String cache = camelCase_fieldCache.get(input);
        if (cache != null) {
            return cache;
        }

        String temp = toCamelCase(input);
        StringBuilder resultSb = new StringBuilder(temp.length());
        resultSb.append(temp.substring(0, 2).toLowerCase());
        resultSb.append(temp.substring(2));

        String result = resultSb.toString();
        camelCase_fieldCache.put(input, result);
        return result;
    }

    /**
     * 下划线写法转换为驼峰写法,并首字母大写
     */
    public static String toCapitalizeCamelCase(String s) {
        String str = toCamelCase(s);
        return StringUtils.capitalize(str);
    }

}
