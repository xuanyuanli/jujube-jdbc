package org.jujubeframework.jdbc.persistence;

import org.assertj.core.api.Assertions;
import org.jujubeframework.jdbc.JujubeJdbcApp;
import org.jujubeframework.jdbc.entity.Department;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = JujubeJdbcApp.class)
@ActiveProfiles({ "test" })
public class DepartmentDaoTest {
    @Autowired
    private DepartmentDao departmentDao;

    @Test
    public void saveOrUpdate() {
        Department department = new Department();
        department.setName("abc");
        Integer id = departmentDao.saveOrUpdate(department);
        Assertions.assertThat(id).isEqualTo(4);
    }

}
