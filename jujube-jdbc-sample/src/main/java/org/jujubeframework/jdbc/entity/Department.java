package org.jujubeframework.jdbc.entity;

import org.jujubeframework.jdbc.support.annotation.Column;
import org.jujubeframework.jdbc.support.entity.BaseEntity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author John Li
 */
@Data
@Accessors(chain = true)
public class Department implements BaseEntity {
    private Integer id;
    private String name;
}
