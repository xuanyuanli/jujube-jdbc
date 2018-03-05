package org.dazao.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dazao.support.entity.BaseEntity;

/**
 * 文本字符相关工具类
 * 
 * @author 李衡 Email：li15038043160@163.com
 */
public class Texts {

    private Texts() {
    }

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     */
    public static String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    /**
     * 过滤掉超过3个字节的UTF8字符
     */
    public static String filterOffUtf8Mb4(String text) {
        byte[] bytes = null;
        try {
            bytes = text.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            return text;
        }
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        int i = 0;
        while (i < bytes.length) {
            short b = bytes[i];
            if (b > 0) {
                buffer.put(bytes[i++]);
                continue;
            }

            b += 256; // 去掉符号位

            if (((b >> 5) ^ 0x6) == 0) {
                buffer.put(bytes, i, 2);
                i += 2;
            } else if (((b >> 4) ^ 0xE) == 0) {
                buffer.put(bytes, i, 3);
                i += 3;
            } else if (((b >> 3) ^ 0x1E) == 0) {
                i += 4;
            } else if (((b >> 2) ^ 0x3E) == 0) {
                i += 5;
            } else if (((b >> 1) ^ 0x7E) == 0) {
                i += 6;
            } else {
                buffer.put(bytes[i++]);
            }
        }
        buffer.flip();
        try {
            return new String(buffer.array(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            return text;
        }
    }

    /**
     * 替换所有空白字符
     */
    public static String replaceBlank(String str) {
        String regex = "\\s*|\t|\r|\n";
        Pattern compile = Pattern.compile(regex);
        return compile.matcher(str).replaceAll("");
    }

    /** 替换utf-8中的空格，以免造成编码转换出现？的情况 */
    public static String replaceUtf8Blank(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        return text.replace(" ", " "); // utf-8空格替换;
    }

    /** 获取一段html的纯文本 */
    public static String getHtmlText(String html) {
        String txtcontent = html.replaceAll("</?[^>]+>", ""); // 剔出<html>的标签
        txtcontent = txtcontent.replaceAll("<a>\\s*|\t|\r|\n</a>", "");// 去除字符串中的空格,回车,换行符,制表符
        return txtcontent;
    }

    /**
     * 正则替换封装（忽略大小写）
     * 
     * @param reg
     *            正则表达式
     * @param repstr
     *            要替换为的字符
     * @param instr
     *            原始字符串
     * @return 完成替换的字符串
     * @example regReplace("@+","","@@@123@") = 123<br />
     *          需要注意的是，正则表达式中如果出现特殊字符，需要进行转义。比如：*.$等。"\\$"进行转义
     */
    public static String regReplace(String reg, String repstr, String instr) {
        return regReplace(reg, repstr, instr, true);
    }

    /**
     * @see #regQuery(String, String, boolean)
     */
    public static List<RegexQueryInfo> regQuery(String reg, String instr) {
        return regQuery(reg, instr, true);
    }

    /**
     * 正则查询
     * 
     * @param reg
     *            正则表达式
     * @param instr
     *            原始字符串
     * @param ignoreCase
     *            是否忽略大小写
     * @return 返回多个匹配的信息
     */
    public static List<RegexQueryInfo> regQuery(String reg, String instr, boolean ignoreCase) {
        List<RegexQueryInfo> list = new ArrayList<Texts.RegexQueryInfo>();
        String regex = reg;
        Pattern pattern = null;
        if (ignoreCase) {
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } else {
            pattern = Pattern.compile(regex);
        }
        Matcher matcher = pattern.matcher(instr);
        while (matcher.find()) {
            RegexQueryInfo info = new RegexQueryInfo();
            info.setEnd(matcher.end());
            info.setStart(matcher.start());
            info.setGroup(matcher.group());
            List<String> groups = new ArrayList<>(matcher.groupCount());
            for (int i = 1; i <= matcher.groupCount(); i++) {
                groups.add(matcher.group(i));
            }
            info.setGroups(groups);
            list.add(info);
        }
        return list;
    }

    /**
     * 正则替换封装
     * 
     * @param reg
     *            正则表达式
     * @param repstr
     *            要替换为的字符
     * @param instr
     *            原始字符串
     * @param ignoreCase
     *            是否忽略大小写
     * @return
     */
    public static String regReplace(String reg, String repstr, String instr, boolean ignoreCase) {
        String regex = reg;
        Pattern pattern = null;
        if (ignoreCase) {
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } else {
            pattern = Pattern.compile(regex);
        }
        Matcher matcher = pattern.matcher(instr);
        return matcher.replaceAll(repstr);
    }

    /**
     * 替换字符串str中的中文为str2
     */
    public static String replaceChinese(String str, String str2) {
        StringBuffer bf = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            if (String.valueOf(str.charAt(i)).matches("[^x00-xff]*")) {
                bf.append(str2);
            } else {
                bf.append(str.charAt(i));
            }
        }
        return bf.toString();
    }

    /**
     * 全角转半角
     */
    public static String toDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);
            }
        }
        String returnString = new String(c);
        return returnString;
    }

    /**
     * 适用于大型字符串分割，可以设置多个分隔符
     * 
     * @param srcString
     *            要分割的字符串
     * @param tokenizerString
     *            分隔符
     * @return 分割后的数组
     * @example String[] s = stringTokenizer("wo; are, student", " ,;");
     *          //s={wo,are,student}
     */
    public static String[] stringTokenizer(String srcString, String tokenizerString) {
        StringTokenizer fenxi = new StringTokenizer(srcString, tokenizerString);
        int length = fenxi.countTokens();
        String[] s = new String[length];
        int i = 0;
        while (fenxi.hasMoreElements()) {
            String str = fenxi.nextToken();
            s[i] = str.trim();
            i++;
        }
        return s;
    }

    /**
     * 用正则匹配，查找字符串中有没有相应字符
     * 
     * @param source
     *            原字符串
     * @param regEx
     *            正则表达式
     * @return 是否找到
     * @example find("zfa_999_ic", "zfa_\\d+_ic") = true
     */
    public static boolean find(String source, String regEx) {
        if (StringUtils.isBlank(source)) {
            return false;
        }
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(source);
        boolean rs = mat.find();
        return rs;
    }

    /**
     * 高亮显示关键字(所有匹配的字符都替换)
     * 
     * @param source
     *            原文本
     * @param keyWord
     *            关键字
     * @param styleBefore
     *            样式前，例如<font class='red'>
     * @param styleAfter
     *            样式后,例如</font>
     * @return
     */
    public static String highlight(String source, String keyWord, String styleBefore, String styleAfter) {
        int begin = 0;
        int len = styleAfter.length() + styleBefore.length() + keyWord.length(); // 加上样式之后的关键字长度
        StringBuilder sb = new StringBuilder(source.length() + len * 5);
        String tag = source;
        while (true) {
            begin = tag.toUpperCase().indexOf(keyWord.toUpperCase()); // 不区分大小写，找到关键字
            // 如果找到关键字，则关键字替换为高亮样式
            if (begin != -1) {
                int end = begin + keyWord.length();
                String red = tag.substring(begin, end);// 原文本中的关键字（保持其大小写状态）
                String result = tag.substring(0, end); // 此次查找的字符串
                result = result.replace(red, (styleBefore + red + styleAfter)); // 对文本中关键字进行高亮替换
                sb.append(result); // 保存已经替换完成的那一段
                tag = tag.substring(end); // 截取字符串，在后面继续寻找关键字，进行高亮替换
            } else {
                sb.append(tag); // 如果没有找到关键字，把文本遗落的一段放入结果中
                break;
            }
        }
        return sb.toString();
    }

    public static boolean isEn(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public static boolean isNumeric(char c) {
        return Character.isDigit(c);
    }

    /** 是否是科学计数法 */
    public static boolean isScientificNotation(String str) {
        try {
            BigDecimal bd = new BigDecimal(str);
            bd.toPlainString();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 和JavaScript中RegExp对象的exec()方法一样<br>
     * 只返回第一个匹配的结果，数组中第一个元素包含正则表达式匹配的字符串，余下的元素是与圆括号内的子表达式相匹配的子串
     */
    public static String[] getGroups(String regex, String source) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        String[] groups = new String[0];
        if (matcher.find()) {
            int count = matcher.groupCount();
            groups = new String[count + 1];
            for (int i = 0; i <= count; i++) {
                groups[i] = matcher.group(i);
            }
        }
        return groups;
    }

    /** 获取匹配到的文本 */
    public static String getGroup(String regex, String source) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * 判断是否包含特殊符号,注此特殊字符不能包含&,因为搜索的内容可能有这个符号
     */
    public static boolean isFormal(String content) {
        String regEx = "[`~!@#$%^*()+=|{}':;',//[//].<>/?~！@#￥%……*（）——+|{}【】‘；：”“’。，、？]";
        return find(content, regEx);
    }

    /**
     * 格式化字符串，仿C#
     * 
     * <pre>
     * str = Hello {0} 
     * result = format(str,"World!") 
     * #result = Hello World!
     * </pre>
     * 
     * @param pattern
     *            待匹配字符串
     * @param params
     *            参数数组
     */
    public static String format(String pattern, String... params) {
        String regex = "\\{\\d+\\}";
        int count = regQuery(regex, pattern).size();
        // 类似{0}这种形式
        if (count > 0) {
            if (params.length != count) {
                throw new IllegalArgumentException("模式匹配跟参数个数不对应");
            }
            String result = pattern;
            for (int i = 0; i < count; i++) {
                String replacement = params[i];
                if (replacement == null) {
                    replacement = "";
                }
                result = result.replace("{" + i + "}", replacement);
            }
            return result;
        }
        // 或者直接{}这种形式
        else if (pattern.contains("{}")) {
            String result = "";
            pattern += " ";// 防止{}出现在最后一行
            String[] arr = pattern.split("\\{\\}");
            for (int i = 0; i < arr.length - 1; i++) {
                result += arr[i] + (params.length <= i ? "" : params[i]);
                if (i == arr.length - 2) {
                    result += arr[i + 1];
                }
            }
            return result.substring(0, result.length() - 1);
        } else {
            return pattern;
        }
    }

    public static String toA(String href, String title, boolean blank) {
        return format("<a href=\"{0}\" target=\"{1}\">{2}</a>", href, blank ? "_blank" : "", title);
    }

    /**
     * 字符截断。如果超出trancationNum，则后跟‘...’
     * 
     * @param source
     *            原始字符
     * @param trancationNum
     *            截断的字符数
     */
    public static String truncate(String source, int trancationNum) {
        if (StringUtils.isBlank(source)) {
            return "";
        }
        if (source.length() > trancationNum) {
            return source.substring(0, trancationNum) + "...";
        } else {
            return source;
        }
    }

    public static long toLong(String source) {
        return NumberUtils.toLong(source);
    }

    public static double toDouble(String source) {
        return NumberUtils.toDouble(source);
    }

    public static int toInt(String source) {
        return NumberUtils.toInt(source);
    }

    /** 只取字符串中的数字 */
    public static int parseInt(String source) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(source)) {
            for (int i = 0; i < source.length(); i++) {
                char ch = source.charAt(i);
                if (Character.isDigit(ch)) {
                    builder.append(ch);
                }
            }
        }
        return NumberUtils.toInt(builder.toString());
    }

    public static String capitalize(String str) {
        return StringUtils.capitalize(str);
    }

    /**
     * 正则查询的信息
     * <p>
     * 类中三个字段参考Matcher类
     * 
     * @author 李衡 Email：li15038043160@163.com
     * @since 2014年10月10日 上午10:43:15
     */
    public static class RegexQueryInfo implements BaseEntity {
        private String group;
        private int start;
        private int end;
        private List<String> groups;

        public String getGroup() {

            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public List<String> getGroups() {
            return groups;
        }

        public void setGroups(List<String> groups) {
            this.groups = groups;
        }

    }

}
