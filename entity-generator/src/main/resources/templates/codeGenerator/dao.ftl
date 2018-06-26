package ${basePackage};

import org.springframework.stereotype.Repository;

import org.dazao.persistence.base.BaseDao;
import ${entityPackage}.${className};

@Repository
public class ${className}Dao extends BaseDao<${className}> {

    @Override
    protected String getTableName() {
        return "${tableName}";
    }
}