package ${basePackage};

import org.jujubeframework.jdbc.base.BaseDao;
import ${entityPackage}.${className};

/**
 * @author generator
 */
public interface ${className}Dao extends BaseDao<${className},${pk.type}> {

    @Override
    default String getTableName() {
        return "${tableName}";
    }

    <#if pk.colName??>
    @Override
    default String getPrimayKeyName() {
        return "${pk.colName}";
    }
    </#if>
}