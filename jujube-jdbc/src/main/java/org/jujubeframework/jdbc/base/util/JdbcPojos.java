package org.jujubeframework.jdbc.base.util;

import lombok.Data;
import org.jujubeframework.jdbc.support.annotation.Column;
import org.jujubeframework.jdbc.support.entity.BaseEntity;
import org.jujubeframework.util.CamelCase;
import org.jujubeframework.util.Pojos;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * Jdbc专用的Pojos，添加了Entity与数据库表的字段对照关系处理逻辑
 *
 * @author John Li
 */
public class JdbcPojos {

    private static final ConcurrentMap<Class<? extends BaseEntity>, Pojos.FieldMapping> FIELD_MAPPING_CACHE = new ConcurrentHashMap<>();

    /** Entity与数据库字段的对应数据 */
    private static final Map<Class<?>, List<FieldColumn>> CLASS_FIELDS_DATA = new HashMap<>(16);

    /** 获得Entity与数据库表的字段对照关系 */
    public static List<FieldColumn> getFieldColumns(Class<? extends BaseEntity> aClass) {
        return CLASS_FIELDS_DATA.computeIfAbsent(aClass, classGenericType -> {
            Field[] declaredFields = classGenericType.getDeclaredFields();
            List<FieldColumn> list = new ArrayList<>(declaredFields.length);
            for (Field declaredField : declaredFields) {
                FieldColumn fieldColumn = new FieldColumn();
                String fieldName = declaredField.getName();
                fieldColumn.setField(fieldName);
                Column annotation = declaredField.getAnnotation(Column.class);
                if (annotation != null) {
                    fieldColumn.setColumn(annotation.value());
                } else {
                    fieldColumn.setColumn(CamelCase.toUnderlineName(fieldName));
                }
                list.add(fieldColumn);
            }
            return list;
        });
    }

    /**
     * 把原始对象映射为对应类型的Pojo
     */
    public static <T extends BaseEntity> T mapping(Object sourceObj, Class<T> clazz) {
        return Pojos.mapping(sourceObj, clazz, getFieldMapping(clazz));
    }

    /** 获得FieldMapping */
    private static <T extends BaseEntity> Pojos.FieldMapping getFieldMapping(Class<T> cl) {
        return FIELD_MAPPING_CACHE.computeIfAbsent(cl, clazz -> {
            List<FieldColumn> fieldColumns = getFieldColumns(clazz);
            Pojos.FieldMapping fieldMapping = new Pojos.FieldMapping();
            for (FieldColumn fieldColumn : fieldColumns) {
                fieldMapping.field(fieldColumn.getColumn(), fieldColumn.getField());
            }
            return fieldMapping;
        });
    }

    /**
     * 把原始对象集合映射为对应类型的Pojo集合
     */
    public static <T extends BaseEntity> List<T> mappingArray(List<?> source, Class<T> clazz) {
        return Pojos.mappingArray(source, clazz, getFieldMapping(clazz));
    }

    /**
     * 字段与数据库字段的存储类
     */
    @Data
    public static class FieldColumn {
        /** Entity字段名 */
        private String field;
        /** 表列名 */
        private String column;
    }
}
