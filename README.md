# easy-jdbc
一款简洁的ORM框架，希望可以提供不同的思路。更接近SQL思维，融入了Record、JPA的思维模式，简化了SQL的开发。

# 思路与借鉴
吸取了很多营养，才有了这个开源项目。主要包括了：Hibernate、Spring Jpa和JFinal。  
Hibernate是ORM流行起来的标榜，简化了JDBC开发，后来越来越重；  
Spring JPA的出现让人惊喜，大大提高了效率，学习成本较高；  
JFinal的Model模式简洁高效，却免不了写SQL和稍显麻烦的对象取值；  

MyBatis可以定制化SQL，渐渐成为了大公司主流。不过对于中小公司来说，阿里的MyBatis规范如：  
> 不要用 resultClass 当返回参数，即使所有类属性名与数据库字段一一对应，也需要定义 ； 反过来，每一个表也必然有一个与之对应。  
> 在表查询中，一律不要使用 * 作为查询的字段列表，需要哪些字段必须明确写明。  
> 不允许直接拿 HashMap 与 Hashtable 作为查询结果集的输出。  

有时候遵守起来不会那么严谨。

考虑到中小公司业务迭代快，前期效率优先，如果有一款上手快、效率高的持久层ORM，那么无疑会很大程度的提高生产力。

# 示例
所有底层框架已完成，先看一下示例。  
示例项目在easy-jdbc-sample中，使用了Spring Boot+H2来启动，直接看测试代码：
```
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
}
```
再看我们的UserDao，其实非常简单：
```
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
```
有兴趣，可以看一下BaseDao的代码，里面有大量的预定义方法。  
至于方法没有主体，只用了@JpaQuery注解，则是借鉴了Spring JPA的神奇之处。

# 待续
关于分页；  
关于设计理念；
