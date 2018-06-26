# easy-jdbc
一款简洁的ORM框架，更接近原生SQL思维，融合了Mybatis、JPA的思维模型，简化了持久层的开发。

# 一、初衷
当我们出现邪恶的想法：自己造轮子，那么一定是遇到了什么困难导致了很多优秀的开源项目不足以满足你的胃口了。  
那么在Java持久层方面，主流的选择有那些？他们又有什么弊端呢？   
## 1、Spring JDBC
可谓是非常简单的一层JDBC封装了，简洁又方便，缺点在于手动写sql，带来的效率低下和不便于管理。
## 2、JFianl Model
和JFinal的整个理论非常契合，简洁到极致，问题是不太容易淡出抽取出来使用，而且也需要大量手写SQL
## 3、Hibernate
一直很优秀，从出生就金光闪闪，独特的对象-关系数据库映射让人眼前一亮，比较明显的缺点是联合查询的弱势。
## 4、Spring JPA
强大的函数名语法解析用起来让人欲罢不能，将提高效率进行到底，缺点和Hibernate类似。
## 5、Mybatis
解脱了Hibernate的繁重，ORM层变得很清新，缺点在于如果不愿舍弃JPA的巨大优势肿么办呢？
## 6、speedment
随着微服务的CQRS和函数式编程的兴起，speedment的春风也吹起来了，可以看成是Hibernate进阶版本，缺点是还是需要手动写查询逻辑。
## 6、思路和借鉴
上述所有的框架中，最让我舒服的就是Spring JPA了，只写方法名就完成了程序的逻辑编写，这样的特性简直是太抓心。    
不过JPA的联合查询实在让人不能忍啊，而且用`select *`这种方式效率确实不高。  
坐在苹果树下思考了一下，为什么不能把Spring JPA和Mybatis结合起来呢？这样不就能鱼和熊掌兼得么？  
说干就干，管他邪恶与否呢？没有亚当夏娃被邪恶引诱，也不会有无穷匮已的百姓和灿烂的文明啊！

# 二、示例
中间耕耘的过程已快进，先来看一下成果。    
示例项目在easy-jdbc-sample中，使用了Spring Boot+H2来启动，先看看测试代码：
```
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
```
再看我们的UserDao，还是比较简洁：
```
@Repository
public class UserDao extends BaseDao<User> {

    @Override
    protected String getTableName() {
        return "user";
    }
    
    @JpaQuery
    public String findNameById(long id) {
        return null;
    }
    
    @JpaQuery
    public List<String> findNameByAge(int age) {
        return null;
    }

    @JpaQuery
    public User findByName(String name) {
        return null;
    }
    
    @JpaQuery
    public List<User> findByNameLike(String name) {
        return null;
    }

    @JpaQuery
    public List<User> findByIdGtSortByAgeDesc(int i) {
        return null;
    }
    
    @JpaQuery
    public int getCountByNameLike(String name) {
        return 0;
    }

    public Pageable<Record> paginationByNameLength(PageableRequest request, int len) {
        String sql = "select u.*,d.name department_name from user u left join department d on d.id = u.department_id where length(u.name) >= ?";
        return paginationBySql(sql, request, len);
    }
}
```
同学们已经注意到@JpaQuery注解的方法都是没有主体的，这点就是继承了Spring JPA的神奇之处了，至于分页的要用到的联合查询则需要手动写SQL了。  

---

我写的方法都没有注释，其实是约定大于配置，如果了解了方法名构建的规则，一看方法名就知道这个原子操作是个什么意思了。  
# 三、Spring Jpa理念与扩展
先来看一下Spring JPA的理念：在查询时，通常需要同时根据多个属性进行查询，且查询的条件也格式各样（大于某个值、在某个范围等等），Spring Data JPA 为此提供了一些表达条件查询的关键字，大致如下：

- And --- 等价于 SQL 中的 and 关键字，比如 findByUsernameAndPassword(String user, Striang pwd)；

- Or --- 等价于 SQL 中的 or 关键字，比如 findByUsernameOrAddress(String user, String addr)；

- Between --- 等价于 SQL 中的 between 关键字，比如 findBySalaryBetween(int max, int min)；

- LessThan --- 等价于 SQL 中的 "<"，比如 findBySalaryLessThan(int max)；

- GreaterThan --- 等价于 SQL 中的">"，比如 findBySalaryGreaterThan(int min)；

- IsNull --- 等价于 SQL 中的 "is null"，比如 findByUsernameIsNull()；

- IsNotNull --- 等价于 SQL 中的 "is not null"，比如 findByUsernameIsNotNull()；

- NotNull --- 与 IsNotNull 等价；

- Like --- 等价于 SQL 中的 "like"，比如 findByUsernameLike(String user)；

- NotLike --- 等价于 SQL 中的 "not like"，比如 findByUsernameNotLike(String user)；

- OrderBy --- 等价于 SQL 中的 "order by"，比如 findByUsernameOrderBySalaryAsc(String user)；

- Not --- 等价于 SQL 中的 "！ ="，比如 findByUsernameNot(String user)；

- In --- 等价于 SQL 中的 "in"，比如 findByUsernameIn(Collection<String> userList) ，方法的参数可以是 Collection 类型，也可以是数组或者不定长参数；

- NotIn --- 等价于 SQL 中的 "not in"，比如 findByUsernameNotIn(Collection<String> userList) ，方法的参数可以是 Collection 类型，也可以是数组或者不定长参数；

除了上述规则，框架还可以实现排序，用到SortBy关键字，如：
```
    @JpaQuery
    public List<User> findByIdGtSortByAgeDesc(int i) {
        return null;
    }
```
这里是根据年龄进行了倒序查询，Desc后缀表示倒序，Asc表示正序（也是默认值）。  

上节的代码中还出现了getCountBy系列方法，规则和Spring JPA一致，是用来查询总数的。  

## 1、特殊情况
还有一个问题，加入我定义了这么一个方法`List<User> findByNameLike(String name)`，但有时候我只希望findByNameLike只返回一条数据，原则上我们修改返回值为User即可，框架会智能分析出你是需要一条数据。如果不幸的时候一个Dao中想要同时定义`List<User> findByNameLike(String name)`和`User findByNameLike(String name)`那肯定是不符合规则的，我们可以这么写`User findOneByNameLike(String name)`也是可以被正确解析的

# 四、分页
BaseDao里面有几个分页的方法，最常用的是paginationBySql。  
这里涉及到两个类：

- PageableRequest 分页请求，主要内容有：当前是第几页，每页显示多少条等
- Pageable 分页信息，主要内容有：分页数据集合，总的元素数等

# 五、代码生成工具
在entity-generator项目中打开EntityGeneratorDemo：
```
	public static void main(String[] args) {
		Config config = new Config("user", "D:\\workspace\\easy-jdbc\\entity-generator\\src\\main\\java", "org.demo.entity", "org.demo.persistence");
		config.setForceCoverDao(false);
		config.setForceCoverEntity(true);
		config.setCreateDao(true);
		EntityGenerator.generateEntity(config);
	}
```
条件：
- classpath下需要有application.properties文件，必须的字段如下：
```
spring.datasource.url=jdbc:mysql://192.168.99.100:3306/demo?characterEncoding=utf8&useSSL=false
spring.datasource.username=root
spring.datasource.password=Aa123456
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
```
- Config配置中需要输入数据库表名、要生成到的项目代码根目录、entity所在包、dao所在包等信息

运行这个类即可在响应的路径中生成Entity和Dao类了。

# 六、使用
大致的步骤为：
- git clone下代码，安装easy-jdbc项目到本地仓库或发布到远程仓库（后续我会把项目发布到maven中央仓库，这一步就可以省略了）
- 在需要的项目中引入依赖：
```
        <dependency>
            <groupId>org.dazao</groupId>
            <artifactId>easy-jdbc</artifactId>
            <version>1.0.0</version>
        </dependency>
```
- 在项目的Spring组件的扫描路径添加上`org.dazao.persistence`。如果用的是applicationContext.xml的形式，如：
```
<context:component-scan base-package="com.yourproject;org.dazao.persistence"></context:component-scan>
```
如果是用的是Spring Boot，如：
```
@ComponentScan({ "com.yourproject", "org.dazao.persistence" })
```
- 打开entity-generator项目的EntityGeneratorDemo来生成需要的entity和dao

