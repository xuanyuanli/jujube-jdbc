package com.yfs.persistence.base.batchupdate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用此注解完成批量更新。没有到size之前，所有的更新数据都在缓存中；当到达size后，进行批量更新
 * 
 * <pre>
 * 1、此注解只适用于Dao层;2、用此注解的方法返回类型必须与Dao的泛型一致
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BatchUpdate {
    /** 缓存key */
    String value() default "";

    /** 多少条一更新 */
    int size() default 0;
}
