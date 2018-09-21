package org.dazao.persistence.base.spec;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 构建Spec的支持类
 * 
 * @author John Li Email：jujubeframework@163.com
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
            if (filter != null) {
                filter.value = value;
                filters.put(key, filter);
                continue;
            }

            // 拆分operator与filedAttribute
            String[] names = key.split(SEPARATOR);
            if (names.length != 2) {
                throw new RuntimeException(key + "is not a valid filter name");
            }
            // names[0]是操作符；names[1]是字段名；value是字段值
            Op operator = Op.valueOf(names[0].toUpperCase());
            String filedName = names[1];

            // 创建searchFilter
            filter = new SpecSupport(filedName, operator, value);
            KEY_CACHE.put(key, filter);
            filters.put(key, filter);
        }
        return filters;
    }

    static final String SEPARATOR = "__";// 两个下划线

    /** key缓存。可以省去一些解析为QueryFilter的时间 */
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
         * @author John Li Email：jujubeframework@163.com
         */
        public static String join(Op op, String field) {
            return op + SpecSupport.SEPARATOR + field;
        }

    }

}
