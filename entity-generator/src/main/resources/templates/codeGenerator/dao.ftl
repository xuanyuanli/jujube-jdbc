package ${basePackage};

import org.springframework.stereotype.Repository;

import org.jujubeframework.jdbc.persistence.base.BaseDao;
import ${entityPackage}.${className};

/**
 * @author generator
 */
@Repository
public class ${className}Dao extends BaseDao<${className}> {

    @Override
    protected String getTableName() {
        return "${tableName}";
    }
}