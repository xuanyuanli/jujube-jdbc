package org.jujubeframework.jdbc.persistence;

import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.entity.User;
import org.jujubeframework.jdbc.base.BaseDaoSupport;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.support.pagination.Pageable;
import org.jujubeframework.jdbc.support.pagination.PageableRequest;
import org.jujubeframework.lang.Record;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author John Li
 */
public interface UserDao extends BaseDao<User,Long> {

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

//    public Pageable<Record> paginationByNameLength(PageableRequest request, int len) {
//        String sql = "select u.*,d.name department_name from user u left join department d on d.id = u.department_id where length(u.name) >= ?";
//        return paginationBySql(sql, request, len);
//    }
//
//    public Pageable<Record> paginationByUser(PageableRequest request, User user) {
//        String sql = "select u.*,d.name department_name from user u left join department d on d.id = u.department_id where ";
//        Spec spec = new Spec();
//        spec.like("u.name", likeWrap(user.getName()));
//        spec.gt("u.age",user.getAge());
//        sql += spec.getFilterSql();
//        return paginationBySql(sql, request, spec.getFilterParams());
//    }
}
