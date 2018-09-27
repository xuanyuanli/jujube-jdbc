package org.jujubeframework.jdbc.persistence.base;

import org.jujubeframework.jdbc.persistence.base.spec.Spec;
import org.jujubeframework.jdbc.support.entity.RecordEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * 本地BaseDao
 *
 * @author John Li
 */
public class LocalBaseDao extends BaseDaoSupport<RecordEntity> {

    private String tableName;

    @Override
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
