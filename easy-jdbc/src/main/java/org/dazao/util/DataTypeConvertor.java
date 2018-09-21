package org.dazao.util;

import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Pojos;
import org.dazao.support.entity.BaseEntity;
import org.dazao.support.entity.RecordEntity;
import org.dazao.support.pagination.Pageable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据类型的转换工具类
 * 
 * @author John Li Email：jujubeframework@163.com
 */
public class DataTypeConvertor {

    private DataTypeConvertor() {
    }

    public static List<Record> convertListMapToListRecord(List<Map<String, Object>> listMap) {
        if (listMap == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        List<Record> list = new ArrayList<Record>();
        // 把一个map转换为一个对象
        for (Map<String, Object> map : listMap) {
            list.add(new Record(map));
        }
        return list;
    }

    /**
     * 把List<Record>转换为List<T>,T是实体类
     * 
     * @param clazz
     *            实体类的类型
     */
    public static <T> List<T> convertListRecordToListBean(Class<T> clazz, List<Record> records) {
        if (clazz == null || records == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        List<T> list = new ArrayList<T>();
        // 把一个map转换为一个对象
        for (Record record : records) {
            T t = convertRecordToBean(clazz, record);
            if (t != null) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 把List<Record>转换为List<Map<String, Object>>
     */
    public static List<Record> convertListBeanToListRecord(List<? extends BaseEntity> list) {
        List<Record> result = new ArrayList<>(list.size());
        for (Object obj : list) {
            result.add(Record.valueOf(obj));
        }
        return result;
    }

    /**
     * 用于把 jdbc查询的Map<String, Object>数据转换为对应Class的实例
     * 
     * @param cl
     *            要转换为的类型
     * @param record
     *            jdbc查询的数据格式
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertRecordToBean(Class<T> cl, Record record) {
        if (cl == null || record == null) {
            return null;
        }
        if (RecordEntity.class.isAssignableFrom(cl)) {
            return (T) new RecordEntity(record);
        }
        return Pojos.mapping(record, cl);
    }

    /** 转换Pageable的泛型为指定类型 */
    public static <T extends Serializable> Pageable<T> convertPageableGenericType(Pageable<Record> pageable, Class<T> clazz) {
        Pageable<T> result = new Pageable<T>();
        result.setTotalElements(pageable.getTotalElements());
        result.setSize(pageable.getSize());
        result.setIndex(pageable.getIndex());
        result.setData(convertListRecordToListBean(clazz, pageable.getData()));
        return result;
    }

}
