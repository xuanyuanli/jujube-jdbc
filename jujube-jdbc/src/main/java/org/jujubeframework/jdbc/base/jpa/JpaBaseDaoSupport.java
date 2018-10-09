package org.jujubeframework.jdbc.base.jpa;

import org.jujubeframework.jdbc.base.BaseDaoSupport;
import org.jujubeframework.jdbc.support.entity.RecordEntity;

import java.io.Serializable;

/**
 * @author John Li
 */
public class JpaBaseDaoSupport extends BaseDaoSupport<RecordEntity, Serializable> {

    /**
     * 真实的类型
     */
    private final Class<?> originalRealGenericType;

    public JpaBaseDaoSupport(Class<?> originalRealGenericType, Class<? extends Serializable> realPrimayKeyType, String tableName) {
        super(RecordEntity.class, (Class<Serializable>) realPrimayKeyType, tableName);
        this.originalRealGenericType = originalRealGenericType;
    }

    public Class<?> getOriginalRealGenericType() {
        return originalRealGenericType;
    }
}
