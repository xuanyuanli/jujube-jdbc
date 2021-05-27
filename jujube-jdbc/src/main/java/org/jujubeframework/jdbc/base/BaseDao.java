package org.jujubeframework.jdbc.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jujubeframework.jdbc.support.entity.BaseEntity;
import org.jujubeframework.lang.Record;

/**
 * @author John Li
 *
 * @param <T>
 *            对应的实体类型
 * @param <PK>
 *            对应的主键类型
 */
public interface BaseDao<T extends BaseEntity, PK extends Serializable> {
    /**
     * 保存
     *
     * @param t
     *            t
     * @return id
     */
    PK save(T t);

    /**
     * 更新
     *
     * @param t
     *            t
     * @return 是否成功
     */
    boolean update(T t);

    /**
     * 存在则更新，不存在则保存(注意：如果id是自增的，请放心使用此方法；否则，则慎用)
     *
     * @param t
     *            t
     * @return 是否成功
     */
    PK saveOrUpdate(T t);

    /**
     * 根据id删除数据
     *
     * @return 是否成功
     */
    boolean deleteById(PK id);

    /**
     * 批量更新数据
     *
     * @param list
     *            数据
     */
    void batchUpdate(List<T> list);

    /**
     * 根据id获得数据
     *
     * @param id
     *            id
     * @return 是否成功
     */
    T findById(PK id);

    /**
     * 根据id查询是否存在此数据
     *
     * @param id
     *            id
     * @return 是否成功
     */
    boolean exists(PK id);

    /**
     * 根据id获得对应fields数据
     *
     * @return T类型的对象
     */
    T findById(String fields, PK id);

    /**
     * 查询所有
     *
     * @return T类型的对象集合
     */
    List<T> findAll();

    /**
     * 获得所有id
     *
     * @return 所有id
     */
    List<PK> findIds();

    /**
     * 获得表名。在子接口中此方法必须用default覆写
     *
     * @return 表名
     */
    String getTableName();

    /**
     * 获得主键名，默认为"id"。如果不是默认值，请在子接口中用default覆写
     *
     * @return 主键名
     */
    String getPrimayKeyName();

    /**
     * List<Long>转换为List<Record>
     */
    default List<Record> idsToRecords(List<Long> ids) {
        List<Record> records = new ArrayList<>(ids.size());
        for (Long id : ids) {
            records.add(new Record().set("id", id));
        }
        return records;
    }
}
