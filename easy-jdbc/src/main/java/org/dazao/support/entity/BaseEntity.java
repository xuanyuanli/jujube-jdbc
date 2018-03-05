package org.dazao.support.entity;

import org.apache.commons.beanutils.BeanUtils;
import org.dazao.lang.Record;
import org.dazao.util.Jsons;
import org.dazao.util.Pojos;

/**
 * entity的基类，所有entity都要继承这个类<br>
 * 此类没有实现序列化，如果子类需要，自行添加
 * 
 * @author 李衡 Email：li15038043160@163.com
 * @since 2013-5-10 下午3:22:19
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
