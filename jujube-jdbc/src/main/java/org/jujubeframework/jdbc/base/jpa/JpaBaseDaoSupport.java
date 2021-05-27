package org.jujubeframework.jdbc.base.jpa;

import org.jujubeframework.jdbc.base.BaseDaoSupport;
import org.jujubeframework.jdbc.support.entity.BaseEntity;
import org.jujubeframework.jdbc.base.jpa.entity.RecordEntity;

import java.io.Serializable;

/**
 * JPA Dao支持
 *
 * @author John Li
 */
public class JpaBaseDaoSupport extends BaseDaoSupport<RecordEntity, Serializable> {

    /**
     * 真实的Entity类型
     */
    private final Class<? extends BaseEntity> originalRealGenericType;

    public JpaBaseDaoSupport(Class<? extends BaseEntity> originalRealGenericType, Class<? extends Serializable> realPrimayKeyType, String tableName) {
        super(RecordEntity.class, (Class<Serializable>) realPrimayKeyType, tableName);
        this.originalRealGenericType = originalRealGenericType;
    }

    public Class<? extends BaseEntity> getOriginalRealGenericType() {
        return originalRealGenericType;
    }

    public JpaBaseDaoSupport cloneSele() {
        JpaBaseDaoSupport jpaBaseDaoSupport = new JpaBaseDaoSupport(this.getOriginalRealGenericType(), this.getRealPrimayKeyType(), this.getTableName());
        jpaBaseDaoSupport.setPrimaryKeyName(this.getPrimayKeyName());
        jpaBaseDaoSupport.setJdbcTemplate(this.getJdbcTemplate());
        return jpaBaseDaoSupport;
    }
}
