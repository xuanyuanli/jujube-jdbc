package org.jujubeframework.jdbc.base.util;

import org.jujubeframework.util.Texts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 带有缓存的字符串处理方法集合
 *
 * @author John Li
 */
public class Strings {
    private final static ConcurrentMap<String, String[]> SPLIT_BY_AND_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Boolean> REG_FIND_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, String[]> REG_GETGROUPS_CACHE = new ConcurrentHashMap<>();
    private final static String AND = "And";

    /** 根据And关键字分割字符串 */
    public static String[] splitByAnd(String text) {
        return SPLIT_BY_AND_CACHE.computeIfAbsent(text, k -> {
            String mname = text;
            List<String> list = new ArrayList<>();
            int index;
            int andLength = AND.length();
            while ((index = mname.indexOf(AND)) > -1) {
                if (mname.startsWith(AND)) {
                    int secondAndIndex = mname.substring(andLength).indexOf(AND);
                    if (secondAndIndex > -1) {
                        int beginIndex = secondAndIndex + andLength;
                        list.add(mname.substring(0, beginIndex));
                        mname = mname.substring(beginIndex + andLength);
                    } else {
                        break;
                    }
                } else {
                    boolean beforeLowerCase = Character.isLowerCase(mname.substring(index - 1, index).charAt(0));
                    boolean afterUpperCase = Character.isUpperCase(mname.substring(index, index + 1).charAt(0));
                    if (beforeLowerCase && afterUpperCase) {
                        list.add(mname.substring(0, index));
                        mname = mname.substring(index + andLength);
                    }
                }
            }
            list.add(mname);
            return list.toArray(new String[] {});
        });
    }

    /** 用正则匹配，查找字符串中有没有相应字符 */
    public static boolean find(String source, String regEx) {
        return REG_FIND_CACHE.computeIfAbsent(source + ":" + regEx, k -> Texts.find(source, regEx));
    }

    /**
     * @see Texts#getGroups(String, String,boolean)
     */
    public static String[] getGroups(String regex, String source) {
        return REG_GETGROUPS_CACHE.computeIfAbsent(source + ":" + regex, k -> Texts.getGroups(regex, source));
    }
}
