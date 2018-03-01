package com.yfs.lang;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;

import com.yfs.util.Beans;
import com.yfs.util.CamelCase;
import com.yfs.util.DataTypeConvertor;

/**
 * 一个进阶版的Map，一般作为数据库表的一行数据存在，也可以作为其他数据载体
 */
public class Record extends HashMap<String, Object> {

    private static final long serialVersionUID = -5745367570456272792L;

    public Record() {
    }

    public Record(Map<String, Object> map) {
        if (map != null) {
            putAll(map);
        }
    }

    /**
     * Get column of mysql type: varchar, char, enum, set, text, tinytext,
     * mediumtext, longtext
     */
    public String getStr(String column) {
        Object val = get(column);
        if (val == null) {
            return null;
        }
        return String.valueOf(val);
    }

    public String getStr(String column, String defaultStr) {
        Object val = get(column);
        if (val == null) {
            return defaultStr;
        }
        return String.valueOf(val);
    }

    /**
     * Get column of mysql type: int, integer, tinyint(n) n > 1, smallint,
     * mediumint
     */
    public Integer getInt(String column) {
        return NumberUtils.toInt(String.valueOf(get(column)));
    }

    public Integer getInt(String column, int def) {
        return NumberUtils.toInt(String.valueOf(get(column)), def);
    }

    /**
     * Get column of mysql type: bigint
     */
    public Long getLong(String column) {
        return NumberUtils.toLong(String.valueOf(get(column)));
    }

    /**
     * Get column of mysql type: unsigned bigint
     */
    public java.math.BigInteger getBigInteger(String column) {
        return (java.math.BigInteger) get(column);
    }

    /**
     * Get column of mysql type: date, year
     */
    public java.util.Date getDate(String column) {
        Object val = get(column);
        if (val instanceof Date) {
            Date date = (Date) val;
            return new java.util.Date(date.getTime());
        } else if (val instanceof java.util.Date) {
            return (java.util.Date) val;
        }
        return null;
    }

    /**
     * Get column of mysql type: real, double
     */
    public Double getDouble(String column) {
        return NumberUtils.toDouble(String.valueOf(get(column)));
    }

    /**
     * Get column of mysql type: float
     */
    public Float getFloat(String column) {
        return NumberUtils.toFloat(String.valueOf(get(column)));
    }

    /**
     * Get column of mysql type: bit, tinyint(1)
     */
    public Boolean getBoolean(String column) {
        String value = String.valueOf(get(column));
        return !(value.equals("0") || value.equals("false"));
    }

    /**
     * Get column of mysql type: decimal, numeric
     */
    public java.math.BigDecimal getBigDecimal(String column) {
        return new BigDecimal(String.valueOf(get(column)));
    }

    /**
     * Get column of mysql type: binary, varbinary, tinyblob, blob, mediumblob,
     * longblob I have not finished the test.
     */
    public byte[] getBytes(String column) {
        return (byte[]) get(column);
    }

    /**
     * Get column of any type that extends from Number
     */
    public Number getNumber(String column) {
        return (Number) get(column);
    }

    public Long getId() {
        return getLong("id");
    }

    public Record set(String key, Object value) {
        put(key, value);
        return this;
    }

    public <T> T toBean(Class<T> clazz) {
        return DataTypeConvertor.convertRecordToBean(clazz, this);
    }

    public static Record valueOf(Object obj) {
        return new Record(convertBeanToMap(obj, false));
    }

    public Record getRecord(String key) {
        return valueOf(get(key));
    }

    public List<Record> getList(String key) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) get(key);
        return DataTypeConvertor.convertListMapToListRecord(list);
    }

    /**
     * Bean对象转换为Map,所有字段名都由驼峰转为下划线格式
     * 
     * @param allowNull
     *            是否允许null值
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertBeanToMap(Object javaBean, boolean allowNull) {
        if (javaBean == null) {
            return null;
        }
        if (javaBean instanceof Map) {
            return (Map<String, Object>) javaBean;
        }
        Map<String, Object> result = new HashMap<>();
        Field[] fields = javaBean.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object value = null;
            try {
                value = Beans.getProperty(javaBean, field.getName());
            } catch (Exception e) {
                continue;
            }
            if (!allowNull && value == null) {
                continue;
            }
            String dbField = CamelCase.toUnderlineName(field.getName());
            result.put(dbField, value);
        }
        return result;
    }
}
