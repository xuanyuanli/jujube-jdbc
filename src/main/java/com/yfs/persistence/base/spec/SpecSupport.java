package com.yfs.persistence.base.spec;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

/**
 * 构建Spec的支持类
 * 
 * @author 李衡 Email：li15038043160@163.com
 */
public class SpecSupport {

    public String fieldName;
    public Object value;
    public Op operator;

    public SpecSupport(String fieldName, Op operator, Object value) {
        this.fieldName = fieldName;
        this.value = value;
        this.operator = operator;
    }

    public SpecSupport() {
    }

    /**
     * 把约定格式的key解析为QueryFilter
     * 
     * @param searchParams
     *            searchParams中key的格式为OPERATOR_FIELDNAME
     */
    public static Map<String, SpecSupport> parse(Map<String, Object> searchParams) {
        if (searchParams == null) {
            throw new IllegalArgumentException("param can not be null");
        }
        Map<String, SpecSupport> filters = Maps.newHashMap();
        for (Entry<String, Object> entry : searchParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 从缓存中取出QueryFilter，如果存在，则改为当前的value
            SpecSupport filter = KEY_CACHE.get(key);
            if (filter != null && validateValue(value, filter.operator)) {
                filter.value = value;
                filters.put(key, filter);
                continue;
            }

            // 拆分operator与filedAttribute
            String[] names = key.split(separator);
            if (names.length != 2) {
                throw new RuntimeException(key + "is not a valid filter name");
            }
            // names[0]是操作符；names[1]是字段名；value是字段值
            Op operator = Op.valueOf(names[0].toUpperCase());
            String filedName = names[1];

            // 如果value为空，则continue；除非他用的是判断中的三个操作符
            if (!validateValue(value, operator)) {
                continue;
            }

            // 创建searchFilter
            filter = new SpecSupport(filedName, operator, value);
            KEY_CACHE.put(key, filter);
            filters.put(key, filter);
        }
        return filters;
    }

    /**
     * 验证value正确性
     */
    private static boolean validateValue(Object value, Op operator) {
        boolean result = true;
        if (!operator.equals(Op.ISNOTEMPTY) && !operator.equals(Op.ISNOTNULL) && !operator.equals(Op.ISNULL) && !operator.equals(Op.ISEMPTY)) { // 如果是这四个操作符，则不需验证
            if (operator.equals(Op.BETWEEN)) {
                try {
                    Object[] arr = (Object[]) value;
                    if (arr.length != 2) {
                        result = false;
                    }
                    if (result && StringUtils.isBlank(ObjectUtils.toString(arr[0])) || StringUtils.isBlank(ObjectUtils.toString(arr[1]))) {
                        result = false;
                    }
                } catch (Exception e) { // 不能转换为数组，则不合法
                    result = false;
                }
            } else if (operator.equals(Op.OR)) {
                // do nothing
            } else {
                // do nothing
                // if (StringUtils.isBlank(ObjectUtils.toString(value))) {
                // result = false;
                // }
            }
        }
        return result;
    }

    static final String separator = "__";// 两个下划线

    // key缓存。可以省去一些解析为QueryFilter的时间
    private static final ConcurrentHashMap<String, SpecSupport> KEY_CACHE = new ConcurrentHashMap<String, SpecSupport>();

    /**
     * 构建规格的操作符
     */
    public enum Op {
        /** 等于 */
        EQ,
        /** like */
        LIKE,
        /** not like */
        NOTLIKE,
        /** 大于 */
        GT,
        /** 小于 */
        LT,
        /** 大于等于 */
        GTE,
        /** 小于等于 */
        LTE,
        /** not */
        NOT,
        /** between,他的值为一个长度为2的数组 */
        BETWEEN,
        /** 为null */
        ISNULL,
        /** 不为null */
        ISNOTNULL,
        /** in */
        IN,
        /** not in */
        NOTIN,
        /** 为空 */
        ISEMPTY,
        /** 不为空 */
        ISNOTEMPTY,
        /** or */
        OR,
        /** and */
        AND,;

        /**
         * 获得正确的查询形式
         * 
         * @since 2013年10月29日 上午10:30:13
         * @author 李衡 Email：li15038043160@163.com
         */
        public static String join(Op op, String field) {
            return op + SpecSupport.separator + field;
        }

    }

}
