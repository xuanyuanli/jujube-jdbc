package org.jujubeframework.jdbc.support.entity;

import org.jujubeframework.lang.Record;

import java.util.Map;

/**
 * 此类即是Record也是BaseEntity,只限用于JpaQuery中，其他地方禁止使用
 *
 * @author John Li
 */
public class RecordEntity extends Record implements BaseEntity {

    public RecordEntity(Map<String, Object> map) {
        super(map);
    }

    private static final long serialVersionUID = -5995445431564439259L;

}
