package org.dazao.persistence.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.dazao.entity.User;
import org.dazao.persistence.UserDao;
import org.dazao.persistentce.app.EasyJdbcApp;
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
    public void test() {
        User user = userDao.findById(1L);
        assertThat(user.getName()).isEqualTo("百度");
    }
}
