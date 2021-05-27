package org.jujubeframework.jdbc.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jujubeframework.jdbc.support.annotation.Column;
import org.jujubeframework.jdbc.support.entity.BaseEntity;

/**
 * @author John Li
 */
@Data
@Accessors(chain = true)
public class User implements BaseEntity {
    private Long id;
    private String name;
    private Integer age;
    private Long departmentId;
    @Column("f_info_id_")
    private Integer fInfoId;
}
