package org.jujubeframework.jdbc.persistence;

import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.entity.User;
import org.jujubeframework.jdbc.support.pagination.Pageable;
import org.jujubeframework.jdbc.support.pagination.PageableRequest;
import org.jujubeframework.lang.Record;

import java.util.List;
import java.util.Map;

/**
 * @author John Li
 */
public interface UserDao extends BaseDao<User, Long> {

    @Override
    default String getTableName() {
        return "user";
    }

    public String findNameById(long id);

    public List<String> findNameByAge(int age);

    public User findByName(String name);

    public List<User> findByNameLike(String name);

    public List<User> findByIdGtSortByAgeDesc(int i);

    public int getCountByNameLike(String name);

    public Pageable<Record> pageForUserList(Map<String, Object> queryMap, PageableRequest request);

    public Pageable<Record> pageForUserList2(Map<String, Object> queryMap, PageableRequest request);

}
