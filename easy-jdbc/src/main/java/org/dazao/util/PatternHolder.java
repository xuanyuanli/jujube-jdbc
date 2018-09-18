package org.dazao.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * 因为Pattern会在初始化的时候进行编译，所以最好缓存一下
 */
public class PatternHolder {
    private static ConcurrentMap<String, Pattern> PATTERNS = new ConcurrentHashMap<>();

    public static Pattern getPattern(String regex) {
        return getPattern(regex, false);
    }

    public static Pattern getPattern(String regex, boolean ignoreCase) {
        String key = regex + ignoreCase;
        Pattern pattern = PATTERNS.get(key);
        if (pattern == null) {
            if (ignoreCase) {
                pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            } else {
                pattern = Pattern.compile(regex);
            }
        }
        return pattern;
    }


}
