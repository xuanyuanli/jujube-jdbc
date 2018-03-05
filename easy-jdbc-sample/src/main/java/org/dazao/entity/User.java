package org.dazao.entity;

import org.dazao.support.entity.BaseEntity;

import lombok.Data;

@Data
public class User implements BaseEntity {
    private Long id;
    private String name;
    private Integer age;
}
