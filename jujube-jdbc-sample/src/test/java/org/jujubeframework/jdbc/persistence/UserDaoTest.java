package org.jujubeframework.jdbc.persistence;

import com.google.common.collect.Lists;
import static org.assertj.core.api.Assertions.assertThat;
import org.jujubeframework.jdbc.JujubeJdbcApp;
import org.jujubeframework.jdbc.entity.User;
import org.jujubeframework.jdbc.support.pagination.Page;
import org.jujubeframework.jdbc.support.pagination.PageRequest;
import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Collections3;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = JujubeJdbcApp.class)
@RunWith(SpringRunner.class)
@ActiveProfiles({ "test" })
public class UserDaoTest {
	@Autowired
	private UserDao userDao;

	@Test
	public void findNameById() {
		String name = userDao.findNameById(1);
		assertThat(name).isEqualTo("百度");
	}

	@Test
	public void findNameByAge() {
		List<String> names = userDao.findNameByAge(5);
		assertThat(names).hasSize(2).contains("新浪", "人人网");
	}

	@Test
	public void findByName() {
		User user = userDao.findByName("宇宙");
		assertThat(user.getId()).isEqualTo(4);
	}

	@Test
	public void findByNameLike() {
		List<User> users = userDao.findByNameLike("%人%");
		assertThat(users).hasSize(2);
		List<String> names = Collections3.extractToListString(users, "name");
		assertThat(names).contains("女人", "人人网");
	}

	@Test
	public void findByIdGtSortByAgeDesc() {
		List<User> users = userDao.findByIdGtSortByAgeDesc(2);
		assertThat(users).hasSize(8);
		assertThat(users.get(0).getName()).isEqualTo("宇宙");
	}

	@Test
	public void getCountByNameLike() {
		int count = userDao.getCountByNameLike("%人%");
		assertThat(count).isEqualTo(2);
	}

    @Test
    public void pageForUserList() {
        Map<String,Object> map = new HashMap<>();
        map.put("name","人");
        map.put("age",1);
        map.put("ids", Lists.newArrayList(1,2,3,4,5,6));
        PageRequest request = new PageRequest(1,10);
        Page<Record> page = userDao.pageForUserList(map, request);
        for (Record record : page) {
            System.out.println(record);
        }
    }

    @Test
    public void pageForUserListOfOrder() {
    }

}
