package ${basePackage};

import org.jujubeframework.jdbc.base.BaseDao;
import ${entityPackage}.${className};

/**
 * @author generator
 */
public interface ${className}Dao extends BaseDao<${className},Long> {

    @Override
    default String getTableName() {
        return "${tableName}";
    }
}