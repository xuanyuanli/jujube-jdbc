# jujube-jdbc
一款简洁的ORM框架，融合了Mybatis和JPA的优势，简化了持久层的开发。

# 一、初衷
当我们出现邪恶的想法：自己造轮子，那么一定是现有软件的使用上遇到了不舒服的地方，不能满足你的胃口了。  
那么在Java持久层方面，主流的选择有那些？他们又有什么弊端呢？   
## 1、Spring JDBC
可谓是非常简单的一层JDBC封装了，简洁又方便，缺点在于手动写sql，带来的效率低下和不便于管理。
## 2、JFianl Model
和JFinal的整个理论非常契合，简洁到极致，问题是不太容易单独抽取出来使用，而且也需要大量手写SQL
## 3、Hibernate
一直很优秀，从出生就金光闪闪，独特的对象-关系数据库映射让人眼前一亮，比较明显的缺点是联合查询的弱势。
## 4、Spring JPA
强大的函数名语法解析用起来让人欲罢不能，将提高效率进行到底，缺点和Hibernate类似。
## 5、Mybatis
解脱了Hibernate的繁重，ORM层变得很清新，缺点在于如果不愿舍弃JPA的巨大优势肿么办呢？
## 6、speedment
随着微服务的CQRS和函数式编程的兴起，speedment的春风也吹起来了，可以看成是Hibernate进阶版本，缺点是还是需要手动写查询逻辑。
## 6、思路和借鉴
上述所有的框架中，最让我舒服的就是Spring JPA了，只写方法名就完成了程序的逻辑编写，这样的特性实在太抓心。    
不过JPA的联合查询实在让人不能忍啊，而且用`select *`这种方式效率确实不高。  

为了解决这个痛点，我决定基于Mybatis和Spring Data Jpa的思路，用Spring-JDBC为基础自己进行二次开发！

# 二、示例
先来看一下成果，示例项目在easy-jdbc-sample中，使用了Spring Boot+H2来启动，先看看测试代码：
```
@SpringBootTest(classes = JujubeJdbcApp.class)
@RunWith(SpringRunner.class)
@ActiveProfiles({"test"})
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
        User user = userDao.findByName("宇宙女人");
        assertThat(user.getId()).isEqualTo(4);
    }

    @Test
    public void findByNameLike() {
        List<User> users = userDao.findByNameLike("%人%");
        assertThat(users).hasSize(3);
        List<String> names = Collections3.extractToListString(users, "name");
        assertThat(names).contains("女人宇宙", "宇宙女人", "人人网");
    }

    @Test
    public void findByIdGtSortByAgeDesc() {
        List<User> users = userDao.findByIdGtSortByAgeDesc(2);
        assertThat(users).hasSize(9);
        assertThat(users.get(0).getName()).isEqualTo("长白山");
    }

    @Test
    public void getCountByNameLike() {
        int count = userDao.getCountByNameLike("%人%");
        assertThat(count).isEqualTo(3);
    }

    @Test
    public void pageForUserList() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "人");
        map.put("age", 1);
        map.put("ids", Lists.newArrayList(1, 2, 3, 4, 5, 6));
        PageRequest request = new PageRequest(1, 10);
        Page<Record> page = userDao.pageForUserList(map, request);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getData().get(0).getId()).isEqualTo(3L);
        assertThat(page.getData().get(1).getId()).isEqualTo(4L);
    }

    @Test
    public void pageForUserListOfOrder() {
        PageRequest request = new PageRequest(1, 10);
        Page<Record> page = userDao.pageForUserListOfOrder(new HashMap<>(), request);
        assertThat(page.getTotalElements()).isEqualTo(11L);
        assertThat(page.getData().get(0).getId()).isEqualTo(11L);
    }

}
```
再看我们的UserDao，非常简洁：
```
public interface UserDao extends BaseDao<User, Long> {

    @Override
    default String getTableName() {
        return "user";
    }

    public String findNameById(long id);

    public List<String> findNameByAge(int age);

    public User findByName(String name);

    public List<User> findByNameLike(String name);

    public List<User> findByIdGtSortByAgeDesc(int i);

    public int getCountByNameLike(String name);

    public Page<Record> pageForUserList(Map<String, Object> queryMap, PageRequest request);

    public Page<Record> pageForUserListOfOrder(Map<String, Object> queryMap, PageRequest request);

}
```
Dao中的find和getCount系列方法继承了Spring JPA的神奇之处：根据方法名动态的生成查询语句。至于分页的话，则需要手动写Sql了，后面会讲到如何配置。

---

UserDao的方法没什么注释，其实是约定大于配置，当你了解了方法名构建的规则，就会知道这些原子操作是什么含义了。
# 三、Spring Jpa理念与扩展
先来看一下Spring JPA的理念：在查询时，通常需要同时根据多个属性进行查询，且查询的条件也各式各样（大于某个值、在某个范围等等），Spring Data JPA 为此提供了一些表达条件查询的关键字，大致如下：

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
    public List<User> findByIdGtSortByAgeDesc(int id);
```
这里是根据年龄进行了倒序查询，Desc后缀表示倒序，Asc表示正序（也是默认值）。  

上节的代码中还出现了getCountBy系列方法，规则和Spring JPA一致，是用来查询总数的。  

## 1、智能判断返回类型
对于`List<User> findByNameLike(String name)`来说，将会自动去查询集合；对于`User findByNameLike(String name)`来说，将会自动取得top元素。

# 四、分页
上面说到分页需要些Sql，这个Sql定义在哪儿呢？  

定义的UserDao.sql如下：

```
##pageForUserList
select u.* from `user` u left join  `department` d on u.department_id=d.id
where 1=1
@if name.notBlank
  and u.name like '%${name}%'
@if age > 0
  and u.age > ${age}
@if ids.notNull
  and u.id in (ids.iter(','))
@if nameDesc.notBlank
  order by u.id asc


##pageForUserListOfOrder
select u.* from `user` u left join  `department` d on u.department_id=d.id
order by u.id desc
```

他的规则非常简单，以`##`开头后跟Dao中的方法名，对应的就是Dao中同名的方法查询Sql。

## 1、判断式

看到`@if name.notBlank`这样的写法，其实是非常符合Java的语法习惯的。`@if`就是`if`判断，`name.notBlank`就是变量`name`不为null且不为空。

框架内置的有4个boolean判断函数，分别为：`notBlank`,`blank`,`notNull`,`null`

## 2、取值

从分页方法入参的`map`，要从中取值，使用`$`符号，如`${age}`。

## 3、iter函数

上面有看到`and u.id in (ids.iter(','))`的语句，其中`ids.iter(',')`其实用到了内置函数`iter`。

`iter`就是循环，`ids.iter(',')`的意思是：以逗号为分隔符，循环输出ids中的元素。比如`ids=[1,2,3]`，那么`in (ids.iter(','))`的结果就是`in (1,2,3)`

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

运行这个类即可在相应的路径中生成Entity和Dao类了。

# 六、使用
如果你用的是spring-boot的话，maven中加上依赖：

```
        <dependency>
            <groupId>io.github.jujube-framework</groupId>
            <artifactId>spring-boot-starter-jujube-jdbc</artifactId>
            <version>1.1</version>
        </dependency>
```

在spring的配置文件中有两个配置项：

```
jujube.jdbc.base-package=org.jujubeframework.jdbc.persistence
jujube.jdbc.sql-base-package=dao-sql
```

basePackage是要扫描的Dao所在的包，sqlBasePackage是sql所在的包。注意sql的名称与Dao的名称要一致。



---



如果是非spring-boot环境，步骤为：

- 在需要的项目中引入依赖：
```
        <dependency>
    		<groupId>org.jujubeframework</groupId>
    		<artifactId>jujube-jdbc</artifactId>
    		<version>1.0.1</version>
        </dependency>
```
- 因为这个框架是基于Spring JDBC的，所以你需要先配置一下DataSource和JdbcTemplate。之后加上如下配置：
```
    @Bean
	public JujubeJdbcConfiguration jujubeJdbcFactoryBean(){
        JujubeJdbcConfiguration jujubeJdbcFactoryBean = new JujubeJdbcConfiguration();
        jujubeJdbcFactoryBean.setBasePackage("org.jujubeframework.jdbc.persistence");
        jujubeJdbcFactoryBean.setSqlBasePackage("dao-sql");
        return jujubeJdbcFactoryBean;
    }
```
如果用的是xml配置，则如：
```
<bean name="jujubeJdbcFactoryBean" class="org.jujubeframework.jdbc.spring.JujubeJdbcConfiguration">
	<property name="basePackage" ref="org.jujubeframework.jdbc.persistence"/>
	<property name="sqlBasePackage" ref="dao-sql"/>
</bean>
```
basePackage是要扫描的Dao所在的包，sqlBasePackage是sql所在的包。注意sql的名称与Dao的名称要一致。

关于sqlBasePackage的路径一般都放在resources下，赋值的时候按照package的形式进行赋值。

如果你想把sql放到main的classpath下，就必须在Maven的pom.xml中配置：

```
    <build>
        <resources>
            <resource>
                <filtering>false</filtering>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*Dao.sql</include>
                </includes>
            </resource>
        </resources>
    </build>
```

- 打开entity-generator项目的EntityGeneratorDemo来生成需要的entity和dao

