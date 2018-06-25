package org.dazao.persistence;

import java.util.List;

import org.dazao.entity.User;
import org.dazao.lang.Record;
import org.dazao.persistence.base.BaseDao;
import org.dazao.persistence.base.jpa.JpaQuery;
import org.dazao.support.pagination.Pageable;
import org.dazao.support.pagination.PageableRequest;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends BaseDao<User> {

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
}
