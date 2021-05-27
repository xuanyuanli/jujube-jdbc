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

    default String getName(long id) {
        return findNameById(id);
    }

    default String getNameById(long id) {
        return findNameById(id);
    }

    String findNameById(long id);

    int findAgeById(long id);

    Long findDepartmentIdById(long id);

    List<String> findNameByAge(int age);

    User findByName(String name);

    User findByNameAndAge(String name, int age);

    List<User> findByNameLike(String name);

    List<User> findByNameNotLike(String name);

    List<User> findByDepartmentIdIn(List<Long> dids);

    List<User> findByIdGtOrderByAgeDesc(int i);

    List<User> findByIdGtOrderByAgeAsc(int i);

    List<User> findByIdGtOrderById(int i);

    List<User> findByIdGtGroupById(int i);

    List<User> findByIdIn(long[] arr);

    List<User> findByAgeIn(List<Integer> arr);

    List<User> findAllGroupById();

    User findAllGroupByIdLimit1();

    List<User> findAllGroupByIdLimit(int limit);

    int getCountByNameLike(String name);

    double getSumOfAgeByNameLike(String name);

    int getCountByNameLikeGroupById(String name);

    long queryAgeCount(long age, long departmentId);

    int queryAgeCount2(long age);

    String queryUserName(long id);

    Record queryUserAge(long age);

    List<Record> queryUserByDepartmentId(long departmentId);

    List<Long> queryIdByDepartmentId(long departmentId);

    List<Record> queryUserByIds(List<Long> ids);

    Pageable<Record> pageForUserList(Map<String, Object> queryMap, PageableRequest request);

    Pageable<Record> pageForUserListOfOrder(Map<String, Object> queryMap, PageableRequest request);

    Pageable<Record> pageForUserUnionQuery2(Map<String, Object> queryMap, PageableRequest request);

    Pageable<Record> pageForUserUnionQuery3(Map<String, Object> queryMap, PageableRequest request);

}
