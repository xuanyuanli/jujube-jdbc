package ${basePackage}.persistence;

import org.springframework.stereotype.Repository;

import ${basePackage}.support.persistence.BaseDao;

@Repository
public class ${className}Dao extends BaseDao {

    @Override
    protected String getTableName() {
        return "${tableName}";
    }
}