package org.jujubeframework.jdbc.base;

import org.jujubeframework.jdbc.support.entity.BaseEntity;

import java.io.Serializable;
import java.util.List;

/**
 * @author John Li
 */
public interface BaseDao<T extends BaseEntity, PK extends Serializable> {
    /**
     * 保存
     * @param t
     * @return
     */
    long save(T t);

    /**
     * 更新
     * @param t
     * @return
     */
    boolean update(T t);

    /**
     * 存在则更新，不存在则保存(注意：如果id是自增的，请放心使用此方法；否则，则慎用)
     * @param t
     * @return
     */
    long saveOrUpdate(T t);

    /**
     * 根据id删除数据
     * @param id
     * @return
     */
    boolean deleteById(PK id);

    /**
     * 批量更新数据
     * @param list
     */
    void batchUpdate(List<T> list);

    /**
     * 根据id获得数据
     * @param id
     * @return
     */
    T findById(PK id);

    /**
     * 根据id查询是否存在此数据
     * @param id
     * @return
     */
    boolean exists(PK id);

    /**
     * 根据id获得对应fields数据
     * @param fields
     * @param id
     * @return
     */
    T findById(String fields, PK id);

    /**
     * 查询所有
     * @return
     */
    List<T> findAll();

    /**
     * 获得所有id
     * @return
     */
    List<Long> findIds();

    String getTableName();
}
