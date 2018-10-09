package org.jujubeframework.jdbc.support.entity;

import org.apache.commons.beanutils.BeanUtils;
import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Jsons;
import org.jujubeframework.util.Pojos;

/**
 * entity的基类，所有entity都要继承这个类<br>
 * 此类没有实现序列化，如果子类需要，自行添加
 *
 * @author John Li Email：jujubeframework@163.com
 */
public interface BaseEntity {

    /**
     * clone自己
     *
     * @return self type object
     */
    @SuppressWarnings("unchecked")
    default <T> T cloneSelf() {
        try {
            return (T) BeanUtils.cloneBean(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * to json
     *
     * @return json
     */
    default String toJson() {
        return Jsons.toJson(this);
    }

    /**
     * 将Bean转换为Record，会把Bean中驼峰命名的字段转为下划线命名
     *
     * @return Record
     */
    default Record toRecord() {
        return Record.valueOf(this);
    }

    /**
     * 将Bean赋值给对象类型的BO
     *
     * @param cl  class
     * @param <T> 要转换的类型
     * @return bo
     */
    default <T> T toBO(Class<T> cl) {
        return Pojos.mapping(this, cl);
    }
}
