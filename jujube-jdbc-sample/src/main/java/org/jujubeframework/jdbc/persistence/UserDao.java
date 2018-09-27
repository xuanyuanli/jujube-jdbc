package org.jujubeframework.jdbc.persistence;

import org.jujubeframework.jdbc.entity.User;
import org.jujubeframework.jdbc.persistence.base.BaseDaoSupport;
import org.jujubeframework.jdbc.persistence.base.jpa.JpaQuery;
import org.jujubeframework.jdbc.persistence.base.spec.Spec;
import org.jujubeframework.jdbc.support.pagination.Pageable;
import org.jujubeframework.jdbc.support.pagination.PageableRequest;
import org.jujubeframework.lang.Record;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author John Li
 */
@Repository
public class UserDao extends BaseDaoSupport<User> {

    @Override
    protected String getTableName() {
        return "user";
    }

    @JpaQuery
    public String findNameById(long id) {
        return null;
    }

    @JpaQuery
    public List<String> findNameByAge(int age) {
        return null;
    }

    @JpaQuery
    public User findByName(String name) {
        return null;
    }

    @JpaQuery
    public List<User> findByNameLike(String name) {
        return null;
    }

    @JpaQuery
    public List<User> findByIdGtSortByAgeDesc(int i) {
        return null;
    }

    @JpaQuery
    public int getCountByNameLike(String name) {
        return 0;
    }

    public Pageable<Record> paginationByNameLength(PageableRequest request, int len) {
        String sql = "select u.*,d.name department_name from user u left join department d on d.id = u.department_id where length(u.name) >= ?";
        return paginationBySql(sql, request, len);
    }

    public Pageable<Record> paginationByUser(PageableRequest request, User user) {
        String sql = "select u.*,d.name department_name from user u left join department d on d.id = u.department_id where ";
        Spec spec = new Spec();
        spec.like("u.name", likeWrap(user.getName()));
        spec.gt("u.age",user.getAge());
        sql += spec.getFilterSql();
        return paginationBySql(sql, request, spec.getFilterParams());
    }
}
