package org.jujubeframework.jdbc.persistence.base.jpa;

import java.lang.annotation.*;

/**
 * Spring Data Jpa的接口方式很魔幻，这里参考实现一下 <br>
 * <br>
 * 使用方式（目前支持find*ById()、findBy*()、findOneBy*()、find(.+)By(.+)、getCountBy系列方法），也支持SortBy和Limit:
 *
 * <pre>
 * 1、在Dao的方法上使用此注解
 * &#64;JpaQuery
 * public String findNameById(long id) {
 * return null;
 * }
 *
 * 2、此方法返回值为null，这里通过aop动态的返回信息，省去了方法主体的编写
 * </pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface JpaQuery {
    /** 查询的字段是否是关键字 */
}
