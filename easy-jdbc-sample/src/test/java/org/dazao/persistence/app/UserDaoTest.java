package org.dazao.persistence.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.dazao.entity.User;
import org.dazao.lang.Record;
import org.dazao.persistence.UserDao;
import org.dazao.persistentce.app.EasyJdbcApp;
import org.dazao.support.pagination.Pageable;
import org.dazao.support.pagination.PageableRequest;
import org.dazao.util.Collections3;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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
		assertThat(users.get(0).getName()).isEqualTo("女人");
	}

	@Test
	public void testGetCountByNameLike() {
		int count = userDao.getCountByNameLike("%人%");
		assertThat(count).isEqualTo(2);
	}

	@Test
	public void testPaginationByNameLength() {
		PageableRequest request = PageableRequest.buildPageRequest(null);
		Pageable<Record> pageable = userDao.paginationByNameLength(request, 3);
		assertThat(pageable.getTotalElements()).isEqualTo(3);
	}
}
