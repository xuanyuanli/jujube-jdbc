package org.dazao.support.entity;

import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Jsons;
import org.jujubeframework.util.Pojos;
import org.apache.commons.beanutils.BeanUtils;

/**
 * entity的基类，所有entity都要继承这个类<br>
 * 此类没有实现序列化，如果子类需要，自行添加
 * 
 * @author John Li Email：jujubeframework@163.com
 *
 */
public interface BaseEntity {

    /** clone自己 */
    @SuppressWarnings("unchecked")
    default <T> T cloneSelf() {
        try {
            return (T) BeanUtils.cloneBean(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default String toJson() {
        return Jsons.toJson(this);
    }

    /** 将Bean转换为Record，会把Bean中驼峰命名的字段转为下划线命名 */
    default Record toRecord() {
        return Record.valueOf(this);
    }

    /** 将Bean赋值给对象类型的BO */
    default <T> T toBO(Class<T> cl) {
        return Pojos.mapping(this, cl);
    }
}
