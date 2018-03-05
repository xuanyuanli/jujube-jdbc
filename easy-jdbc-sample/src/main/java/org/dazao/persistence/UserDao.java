package org.dazao.persistence;

import org.dazao.entity.User;
import org.dazao.persistence.base.BaseDao;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends BaseDao<User> {

    @Override
    protected String getTableName() {
        return "user";
    }

}
