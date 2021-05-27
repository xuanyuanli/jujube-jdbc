package org.jujubeframework.jdbc.persistence;

import java.util.List;
import java.util.Map;

import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.entity.Department;
import org.jujubeframework.jdbc.entity.User;
import org.jujubeframework.jdbc.support.pagination.Pageable;
import org.jujubeframework.jdbc.support.pagination.PageableRequest;
import org.jujubeframework.lang.Record;

/**
 * @author John Li
 */
public interface DepartmentDao extends BaseDao<Department, Integer> {

    @Override
    default String getTableName() {
        return "department";
    }

}
