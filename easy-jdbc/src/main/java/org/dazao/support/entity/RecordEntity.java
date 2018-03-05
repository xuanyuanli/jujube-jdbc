package org.dazao.support.entity;

import java.util.Map;

import org.dazao.lang.Record;

/** 此类即是Record也是BaseEntity,只限用于JpaQuery中，其他地方禁止使用 */
public class RecordEntity extends Record implements BaseEntity {

    public RecordEntity(Map<String, Object> map) {
        super(map);
    }

    private static final long serialVersionUID = -5995445431564439259L;

}
