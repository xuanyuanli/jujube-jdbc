package org.jujubeframework.jdbc.persistence;

import com.google.common.collect.Lists;
import org.jujubeframework.jdbc.JujubeJdbcApp;
import org.jujubeframework.jdbc.entity.User;
import org.jujubeframework.jdbc.support.entity.BaseEntity;
import org.jujubeframework.jdbc.support.pagination.Pageable;
import org.jujubeframework.jdbc.support.pagination.PageableRequest;
import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Collections3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JujubeJdbcApp.class)
@ActiveProfiles({ "test" })
public class UserDaoTest {
    @Autowired
    private UserDao userDao;

    @Test
    public void save() {
        long id = 9999L;
        int fInfoId = 12345555;
        userDao.save(new User().setFInfoId(fInfoId).setId(id));
        User user = userDao.findById(id);
        assertThat(user.getFInfoId()).isEqualTo(fInfoId);
        userDao.update(user.setFInfoId(10));
        user = userDao.findById(id);
        assertThat(user.getFInfoId()).isEqualTo(10);
        userDao.deleteById(id);
        user = userDao.findById(id);
        assertThat(user).isNull();
    }

    @Test
    public void updateNull() {
        long id = 9998L;
        userDao.save(new User().setName("name").setId(id));
        User user = userDao.findById(id);
        assertThat(user.getName()).isEqualTo("name");
        userDao.update(new User().setName(BaseEntity.STRING_NULL).setId(id));
        user = userDao.findById(id);
        assertThat(user.getName()).isNull();
        userDao.deleteById(id);
    }

    @Test
    public void batchUpdate() {
        List<User> users = new ArrayList<>();
        users.add(new User().setId(1001L).setName("1"));
        users.add(new User().setId(1002L).setName("2"));
        users.add(new User().setId(1003L).setName("3"));
        users.add(new User().setId(1004L).setName("4"));
        for (User user : users) {
            userDao.save(user);
        }
        users.clear();
        users.add(new User().setId(1001L).setName("112345678911234567891123456789").setAge(344444));
        users.add(new User().setId(1002L).setName("2"));
        users.add(new User().setId(1003L).setName("3"));
        users.add(new User().setId(1004L).setName("4"));
        userDao.batchUpdate(users);

        assertThat(userDao.findById(1001L).getAge()).isEqualTo(344444);
        assertThat(userDao.findNameById(1001L)).isEqualTo("112345678911234567891123456789");
        userDao.deleteById(1001L);
        userDao.deleteById(1002L);
        userDao.deleteById(1003L);
        userDao.deleteById(1004L);
    }

    @Test
    public void update() {
        long id = 9998L;
        int fInfoId = 12345555;
        userDao.save(new User().setFInfoId(fInfoId).setId(id).setAge(10).setName("John"));
        User user = userDao.findById(id);
        assertThat(user.getFInfoId()).isEqualTo(fInfoId);
        assertThat(user.getAge()).isEqualTo(10);
        assertThat(user.getName()).isEqualTo("John");
        assertThat(user.getId()).isEqualTo(id);

        userDao.update(new User().setId(id).setAge(18));
        user = userDao.findById(id);
        assertThat(user.getFInfoId()).isEqualTo(fInfoId);
        assertThat(user.getAge()).isEqualTo(18);
        assertThat(user.getName()).isEqualTo("John");
        assertThat(user.getId()).isEqualTo(id);
        userDao.deleteById(id);
        user = userDao.findById(id);
        assertThat(user).isNull();
    }

    @Test
    public void getName() {
        String name = userDao.getName(1);
        assertThat(name).isEqualTo("百度");
    }

    @Test
    public void findNameById() {
        String name = userDao.findNameById(1);
        assertThat(name).isEqualTo("百度");
    }

    @Test
    public void getNameById() {
        String name = userDao.getNameById(1);
        assertThat(name).isEqualTo("百度");
    }

    @Test
    public void findNameByAge() {
        List<String> names = userDao.findNameByAge(5);
        assertThat(names).hasSize(2).contains("新浪", "人人网");
    }

    @Test
    public void findByName() {
        User user = userDao.findByName("宇宙女人");
        assertThat(user.getId()).isEqualTo(4);
    }

    @Test
    public void findByNameAndAge() {
        User user = userDao.findByNameAndAge("宇宙女人", 999);
        assertThat(user.getId()).isEqualTo(4);
    }

    @Test
    public void findByNameAndAgeEx() {
        Assertions.assertThrows(UndeclaredThrowableException.class, () -> {
            User user = userDao.findByNameAndAge("", 999);
            assertThat(user.getId()).isEqualTo(4);
        });
    }

    @Test
    public void findByNameLike() {
        List<User> users = userDao.findByNameLike("%人%");
        assertThat(users).hasSize(3);
        List<String> names = Collections3.extractToListString(users, "name");
        assertThat(names).contains("女人宇宙", "宇宙女人", "人人网");
    }

    @Test
    public void findByNameNotLike() {
        List<User> users = userDao.findByNameNotLike("%人%");
        assertThat(users).hasSize(8);
        List<String> names = Collections3.extractToListString(users, "name");
        assertThat(names).contains("百度", "阿里");
    }

    @Test
    public void findByDepartmentIdIn() {
        List<User> users = userDao.findByDepartmentIdIn(Lists.newArrayList(2L, 3L));
        assertThat(users).hasSize(7);
    }

    @Test
    public void findByIdGtOrderByAgeDesc() {
        List<User> users = userDao.findByIdGtOrderByAgeDesc(2);
        assertThat(users).hasSize(9);
        assertThat(users.get(0).getName()).isEqualTo("长白山");
    }

    @Test
    public void findByIdGtOrderByAgeAsc() {
        List<User> users = userDao.findByIdGtOrderByAgeAsc(2);
        assertThat(users).hasSize(9);
        assertThat(users.get(0).getName()).isEqualTo("日本");
    }

    @Test
    public void findByIdGtOrderById() {
        List<User> users = userDao.findByIdGtOrderById(2);
        assertThat(users).hasSize(9);
        assertThat(users.get(0).getName()).isEqualTo("女人宇宙");
    }

    @Test
    public void findByIdGtGroupById() {
        List<User> users = userDao.findByIdGtGroupById(2);
        assertThat(users).hasSize(9);
        assertThat(users.get(0).getName()).isEqualTo("女人宇宙");
    }

    @Test
    public void findAllGroupById() {
        List<User> list = userDao.findAllGroupById();
        assertThat(list.size()).isEqualTo(11);
    }

    @Test
    public void findAllGroupByIdLimit1() {
        User user = userDao.findAllGroupByIdLimit1();
        assertThat(user.getId()).isEqualTo(1);
    }

    @Test
    public void findAllGroupByIdLimit() {
        List<User> users = userDao.findAllGroupByIdLimit(2);
        assertThat(users.size()).isEqualTo(2);
    }

    @Test
    public void getCountByNameLike() {
        int count = userDao.getCountByNameLike("%人%");
        assertThat(count).isEqualTo(3);
    }

    @Test
    public void getCountByNameLikeGroupById() {
        int count = userDao.getCountByNameLikeGroupById("%人%");
        assertThat(count).isEqualTo(3);
    }

    @Test
    public void getSumOfAgeByNameLike(){
        double sumOfAge = userDao.getSumOfAgeByNameLike("%人%");
        assertThat(sumOfAge).isEqualTo(1013);
    }

    @Test
    public void queryAgeCount() {
        long count = userDao.queryAgeCount(10, 1);
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void queryAgeCount2() {
        int count2 = userDao.queryAgeCount2(10);
        assertThat(count2).isEqualTo(3);
    }

    @Test
    public void queryUserName() {
        String name = userDao.queryUserName(1);
        assertThat(name).isEqualTo("百度");
    }

    @Test
    public void queryUserAge() {
        Record record = userDao.queryUserAge(10);
        assertThat(record.getId()).isEqualTo(1L);
    }

    @Test
    public void queryUserDepartment() {
        List<Record> records = userDao.queryUserByDepartmentId(1);
        assertThat(records.size()).isEqualTo(4);
    }

    @Test
    public void queryIdDepartment(){
        List<Long> records = userDao.queryIdByDepartmentId(1);
        assertThat(records.size()).isEqualTo(4);
    }

    @Test
    public void pageForUserList() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "人");
        map.put("age", 1);
        map.put("ids", Lists.newArrayList(1, 2, 3, 4, 5, 6));
        PageableRequest request = new PageableRequest(1, 10);
        Pageable<Record> pageable = userDao.pageForUserList(map, request);
        assertThat(pageable.getTotalElements()).isEqualTo(2);
        assertThat(pageable.getData().get(0).getId()).isEqualTo(3L);
        assertThat(pageable.getData().get(1).getId()).isEqualTo(4L);
    }

    @Test
    public void pageForUserListOfOrder() {
        PageableRequest request = new PageableRequest(1, 10);
        Pageable<Record> pageable = userDao.pageForUserListOfOrder(new HashMap<>(), request);
        assertThat(pageable.getTotalElements()).isEqualTo(11L);
        assertThat(pageable.getData().get(0).getId()).isEqualTo(11L);
    }

    @Test
    public void findAgeById() {
        int age = userDao.findAgeById(1);
        assertThat(age).isEqualTo(10);

        age = userDao.findAgeById(11111111);
        assertThat(age).isEqualTo(0);
    }

    @Test
    public void findDepartmentIdById() {
        Long departmentId = userDao.findDepartmentIdById(1);
        assertThat(departmentId).isEqualTo(1);

        departmentId = userDao.findDepartmentIdById(1111111111);
        assertThat(departmentId).isNull();
    }

    @Test
    public void findByIdIn() {
        List<User> list = userDao.findByIdIn(new long[] { 1, 2, 3 });
        assertThat(list).isNotNull();
    }

    @Test
    public void findByAgeIn() {
        List<User> list = userDao.findByAgeIn(Lists.newArrayList(10, 20, 9));
        assertThat(list).isNotNull();
    }

    @Test
    public void queryUserByIds() {
        List<Record> list = userDao.queryUserByIds(Lists.newArrayList(10L));
        assertThat(list.get(0).getStr("name")).isEqualTo("美国");
    }

    @Test
    public void pageForUserUnionQuery2() {
        PageableRequest request = new PageableRequest(1, 10);
        HashMap<String, Object> map = new HashMap<>();
        map.put("age", 20);
        map.put("departmentId", 1);
        Pageable<Record> page = userDao.pageForUserUnionQuery2(map, request);
        assertThat(page.getData().size()).isEqualTo(6);
    }

    @Test
    public void pageForUserUnionQuery3() {
        PageableRequest request = new PageableRequest(1, 10);
        Pageable<Record> records = userDao.pageForUserUnionQuery3(new HashMap<>(), request);
        assertThat(records.getData().size()).isEqualTo(9);
    }

}
