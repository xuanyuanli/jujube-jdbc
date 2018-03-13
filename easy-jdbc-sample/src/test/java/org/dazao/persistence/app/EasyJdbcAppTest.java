package org.dazao.persistence.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.dazao.entity.User;
import org.dazao.lang.Record;
import org.dazao.persistence.UserDao;
import org.dazao.persistentce.app.EasyJdbcApp;
import org.dazao.support.pagination.Pageable;
import org.dazao.support.pagination.PageableRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = EasyJdbcApp.class)
@RunWith(SpringRunner.class)
@ActiveProfiles({ "test" })
public class EasyJdbcAppTest {
    @Autowired
    private UserDao userDao;

    @Test
    public void findById() {
        User user = userDao.findById(1L);
        assertThat(user.getName()).isEqualTo("百度");
    }

    @Test
    public void findByNameLike() {
        List<User> users = userDao.findByNameLike("百度%");
        for (User user : users) {
            System.out.println(user);
        }
        assertThat(users.size()).isEqualTo(2);
        assertThat(users.get(0).getName()).isEqualTo("百度");
        assertThat(users.get(1).getName()).isEqualTo("百度爱奇艺");
    }

    @Test
    public void findByIdGtSortByAgeDesc() {
        List<User> users = userDao.findByIdGtSortByAgeDesc(1);
        assertThat(users.get(0).getAge()).isEqualTo(999);
    }

    @Test
    public void paginationByNameLength() {
        PageableRequest request = new PageableRequest();
        Pageable<Record> result = userDao.paginationByNameLength(request, 4);
        assertThat(result.getSize()).isGreaterThan(2);
    }
}
