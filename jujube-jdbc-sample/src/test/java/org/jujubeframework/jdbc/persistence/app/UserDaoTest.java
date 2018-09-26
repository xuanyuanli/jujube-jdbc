package org.jujubeframework.jdbc.persistence.app;

import static org.assertj.core.api.Assertions.assertThat;
import org.jujubeframework.jdbc.EasyJdbcApp;
import org.jujubeframework.jdbc.entity.User;
import org.jujubeframework.jdbc.persistence.UserDao;
import org.jujubeframework.jdbc.support.pagination.Pageable;
import org.jujubeframework.jdbc.support.pagination.PageableRequest;
import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Collections3;
import org.jujubeframework.util.Jsons;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest(classes = EasyJdbcApp.class)
@RunWith(SpringRunner.class)
@ActiveProfiles({ "test" })
public class UserDaoTest {
	@Autowired
	private UserDao userDao;

	@Test
	public void testFindNameById() {
		String name = userDao.findNameById(1);
		assertThat(name).isEqualTo("百度");
	}

	@Test
	public void testFindNameByAge() {
		List<String> names = userDao.findNameByAge(5);
		assertThat(names).hasSize(2).contains("新浪", "人人网");
	}

	@Test
	public void testFindByName() {
		User user = userDao.findByName("宇宙");
		assertThat(user.getId()).isEqualTo(4);
	}

	@Test
	public void testFindByNameLike() {
		List<User> users = userDao.findByNameLike("%人%");
		assertThat(users).hasSize(2);
		List<String> names = Collections3.extractToListString(users, "name");
		assertThat(names).contains("女人", "人人网");
	}

	@Test
	public void testFindByIdGtSortByAgeDesc() {
		List<User> users = userDao.findByIdGtSortByAgeDesc(2);
		assertThat(users).hasSize(8);
		assertThat(users.get(0).getName()).isEqualTo("宇宙");
	}

	@Test
	public void testGetCountByNameLike() {
		int count = userDao.getCountByNameLike("%人%");
		assertThat(count).isEqualTo(2);
	}

	@Test
	public void testPaginationByNameLength() {
		PageableRequest request = PageableRequest.buildPageRequest();
		Pageable<Record> pageable = userDao.paginationByNameLength(request, 3);
		assertThat(pageable.getTotalElements()).isEqualTo(3);
	}

    @Test
    public void testPaginationByUser() {
        PageableRequest request = PageableRequest.buildPageRequest();
        User user = new User();
        user.setName("人");
        Pageable<Record> pageable = userDao.paginationByUser(request, user);
        System.out.println(Jsons.toJson(pageable));
        assertThat(pageable.getTotalElements()).isEqualTo(2);
    }
}
