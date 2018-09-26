package org.jujubeframework.jdbc.entity;

import lombok.Data;
import org.jujubeframework.jdbc.support.entity.BaseEntity;

@Data
public class User implements BaseEntity {
    private Long id;
    private String name;
    private Integer age;
}
