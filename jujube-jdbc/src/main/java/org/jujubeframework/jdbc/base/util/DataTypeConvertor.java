package org.jujubeframework.jdbc.base.util;

import org.jujubeframework.jdbc.support.entity.BaseEntity;
import org.jujubeframework.jdbc.support.pagination.Pageable;
import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Pojos;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jdbc专用之数据类型的转换工具类
 *
 * @deprecated Bean和Record的转换，请使用JdbcPojos或Pojos
 * @author John Li Email：jujubeframework@163.com
 */
@Deprecated
public class DataTypeConvertor {

    private DataTypeConvertor() {
    }

    /**
     * 把List<Record>转换为List<Map<String, Object>>
     */
    public static List<Record> convertListBeanToListRecord(List<? extends BaseEntity> list) {
        return list.stream().map(Record::valueOf).collect(Collectors.toList());
    }

    /**
     * 转换Pageable的泛型为指定类型
     */
    public static <T extends Serializable> Pageable<T> convertPageableGenericType(Pageable<Record> pageable, Class<T> clazz) {
        Pageable<T> result = new Pageable<>();
        result.setTotalElements(pageable.getTotalElements());
        result.setSize(pageable.getSize());
        result.setIndex(pageable.getIndex());
        result.setData(Pojos.mappingArray(pageable.getData(), clazz));
        return result;
    }
}
