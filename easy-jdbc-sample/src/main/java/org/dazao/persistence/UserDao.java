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
    public List<User> findByNameLike(String name) {
        return null;
    }

    @JpaQuery
    public List<User> findByIdGtSortByAgeDesc(int i) {
        return null;
    }

    public Pageable<Record> paginationByNameLength(PageableRequest request, int len) {
        String sql = "select * from user u where length(u.name) >= ?";
        return paginationBySql(sql, request, len);
    }
}
