package com.yfs.util;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 简单Java对象 转换工具类，主要用于把源对象Bean转换为Pojo <br>
 * <li>针对字段名，驼峰命名和下划线命名可以完成自动转换并赋值。例如A对象到B对象，A中有user_type字段，B中有userType，可以完成user_type->userType的字段赋值，如果类型不一致，也会自动转换</li>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Pojos {

    private static final ConcurrentMap<String, FieldMapping> CLASS_MAP = new ConcurrentHashMap<>();

    /**
     * 把原始对象映射为对应类型的Pojo
     */
    public static <T> T mapping(Object sourceObj, Class<T> clazz) {
        return mapping(sourceObj, clazz, null);
    }

    /** 获得缓存的FieldMapping */
    public static FieldMapping getCacheFieldMapping(Object sourceObj, Class<?> clazz, FieldMapping fieldMapping) {
        if (fieldMapping == null) {
            fieldMapping = new FieldMapping();
        }
        String cacheKey = sourceObj.getClass().getName() + ":" + clazz.getName();
        if (sourceObj instanceof Map) {
            List<String> fieldNames = getFieldNameList(sourceObj);
            fieldNames.addAll(fieldMapping.getFieldMapping().keySet());
            fieldNames.addAll(fieldMapping.getFieldMapping().values());
            // 没必要用排序来判定Map的Key是否一致，因为同一个Map的keySet和values取出来的顺序是不变的
            // fieldNames.sort(null);
            cacheKey = fieldNames + ":" + clazz.getName();
        }
        if (CLASS_MAP.containsKey(cacheKey)) {
            return CLASS_MAP.get(cacheKey);
        } else {
            List<String> fieldNames = getFieldNameList(sourceObj);
            Map<String, String> mapping = fieldMapping.getFieldMapping();
            for (String fieldName : fieldNames) {
                if (!mapping.containsKey(fieldName)) {
                    PropertyDescriptor field = Beans.getPropertyDescriptor(clazz, fieldName);
                    if (field == null) {
                        String camelCase = CamelCase.toCamelCase(fieldName);
                        field = Beans.getPropertyDescriptor(clazz, camelCase);
                        if (field == null) {
                            field = Beans.getPropertyDescriptor(clazz, CamelCase.toUnderlineName(fieldName));
                        }
                    }
                    if (field != null) {
                        fieldMapping.field(fieldName, field.getName());
                    }
                }
            }

            CLASS_MAP.put(cacheKey, fieldMapping);
        }
        return fieldMapping;
    }

    /** 获得字段名称集合 */
    private static List<String> getFieldNameList(Object sourceObj) {
        List<String> fieldNames = null;
        if (sourceObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) sourceObj;
            fieldNames = new ArrayList<>(map.keySet());
        } else {
            fieldNames = Beans.getAllDeclaredFieldNames(sourceObj.getClass());
        }
        return fieldNames;
    }

    /**
     * 把原始对象映射为对应类型的Pojo
     * 
     * @param fieldMapping
     *            字段映射
     */
    public static <T> T mapping(Object sourceObj, Class<T> destClass, FieldMapping fieldMapping) {
        if (destClass == null || sourceObj == null) {
            return null;
        }
        if (Map.class.isAssignableFrom(destClass)) {
            throw new IllegalArgumentException("destClass不能为Map");
        }
        Map<String, String> mapping = getCacheFieldMapping(sourceObj, destClass, fieldMapping).getFieldMapping();
        // 获得对象实例
        T destObj = Beans.getInstance(destClass);
        for (Entry<String, String> entry : mapping.entrySet()) {
            String fieldName = entry.getKey();
            String destFieldName = entry.getValue();
            Object value = Beans.getProperty(sourceObj, fieldName);
            if (value != null) {
                // value = valueFilter(value, destClass, destFieldName);
                Beans.setProperty2(destObj, destFieldName, value);
            }
        }
        return destObj;
    }

    /** 对值进行过滤 */
    static Object valueFilter(Object sourceValue, Class<?> destClass, String destFieldName) {
        // double转换为string时，有可能值是科学计数法。现在暂时没有这个问题，以后出现了，这里需要添加逻辑
        Class<?> sourceFieldClass = sourceValue.getClass();
        PropertyDescriptor destField = Beans.getPropertyDescriptor(destClass, destFieldName);
        Class<?> destFieldClass = destField.getPropertyType();
        if ((Double.class.equals(sourceFieldClass) || double.class.equals(sourceFieldClass) || Float.class.equals(sourceFieldClass) || float.class.equals(sourceFieldClass))
                && String.class.equals(destFieldClass)) {
            Number number = (Number) sourceValue;
            return Utils.numberToString(number.doubleValue());
        }
        return sourceValue;
    }

    /** 字段对应类(key-value: sourceFieldName-destFieldName) */
    public static class FieldMapping {
        private Map<String, String> mapping = new HashMap<String, String>();

        public FieldMapping field(String sourceField, String destField) {
            mapping.put(sourceField, destField);
            return this;
        }

        public Map<String, String> getFieldMapping() {
            return mapping;
        }

    }

    public static <T> List<T> mappingArray(List<?> source, Class<T> class1) {
        List<T> list = new ArrayList<>();
        for (Object obj : source) {
            list.add(mapping(obj, class1));
        }
        return list;
    }
}
