package org.dazao.persistence.base;

import java.util.List;

import org.dazao.persistence.base.spec.Spec;
import org.dazao.support.entity.RecordEntity;
import org.springframework.jdbc.core.JdbcTemplate;

/** 本地BaseDao */
public class LocalBaseDao extends BaseDao<RecordEntity> {

    private String tableName;

    protected String getTableName() {
        return tableName;
    }

    public LocalBaseDao(JdbcTemplate jdbcTemplate, String tableName) {
        super();
        setJdbcTemplate(jdbcTemplate);
        this.tableName = tableName;
    }

    @Override
    public List<RecordEntity> find(String sql, Object[] params) {
        return super.find(sql, params);
    }

    @Override
    public List<RecordEntity> find(Spec spec) {
        return super.find(spec);
    }

    @Override
    public List<RecordEntity> find(String fields, Spec spec) {
        return super.find(fields, spec);
    }

    @Override
    public RecordEntity findOne(Spec spec) {
        return super.findOne(spec);
    }

}
