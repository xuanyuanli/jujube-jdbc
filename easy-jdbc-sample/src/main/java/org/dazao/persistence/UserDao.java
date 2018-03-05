package org.dazao.persistence;

import java.util.List;

import org.dazao.entity.User;
import org.dazao.persistence.base.BaseDao;
import org.dazao.persistence.base.jpa.JpaQuery;
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

}
