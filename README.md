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
## 7、QueryDSL
强类型查询，和speedment相似
## 8、思路和借鉴
上述所有的框架中，最让我舒服的就是Spring JPA了，只写方法名就完成了程序的逻辑编写，这样的特性实在太抓心。    
不过JPA的联合查询实在让人不能忍啊，而且用`select *`这种方式效率确实不高。  

为了解决这个痛点，我决定基于Mybatis和Spring Data Jpa的思路，用Spring-JDBC为基础自己进行二次开发！

# 二、示例
先来看一下成果，示例项目在easy-jdbc-sample中，使用了Spring Boot+H2来启动，先看看测试代码：
```
@SpringBootTest(classes = JujubeJdbcApp.class)
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
    public void findByIdGtOrderByAgeDesc() {
        List<User> users = userDao.findByIdGtOrderByAgeDesc(2);
        assertThat(users).hasSize(9);
        assertThat(users.get(0).getName()).isEqualTo("长白山");
    }
    
    @Test
    public void findAllGroupById(){
        List<User> list = userDao.findAllGroupById();
        assertThat(list.size()).isEqualTo(11);
    }

    @Test
    public void findAllGroupByIdLimit1(){
        User user = userDao.findAllGroupByIdLimit1();
        assertThat(user.getId()).isEqualTo(1);
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

    public List<User> findByIdGtOrderByAgeDesc(int i);

    public int getCountByNameLike(String name);

    public List<User> findAllGroupById();

    public User findAllGroupByIdLimit1();
    
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

- Lt --- 等价于 SQL 中的 "<"，比如 findBySalaryLt(int max)；

- Lte --- 等价于 SQL 中的 "<="，比如 findBySalaryLte(int max)；

- Gt --- 等价于 SQL 中的">"，比如 findBySalaryGt(int min)；

- Gte --- 等价于 SQL 中的">="，比如 findBySalaryGte(int min)；

- IsNull --- 等价于 SQL 中的 "is null"，比如 findByUsernameIsNull()；

- IsNotNull --- 等价于 SQL 中的 "is not null"，比如 findByUsernameIsNotNull()；

- NotNull --- 与 IsNotNull 等价；

- Like --- 等价于 SQL 中的 "like"，比如 findByUsernameLike(String user)；

- NotLike --- 等价于 SQL 中的 "not like"，比如 findByUsernameNotLike(String user)；

- OrderBy --- 等价于 SQL 中的 "order by"，比如 findByUsernameOrderBySalaryAsc(String user)；

- Not --- 等价于 SQL 中的 "！ ="，比如 findByUsernameNot(String user)；

- In --- 等价于 SQL 中的 "in"，比如 findByUsernameIn(Collection<String> userList) ，方法的参数可以是 Collection 类型，也可以是数组或者不定长参数；

- NotIn --- 等价于 SQL 中的 "not in"，比如 findByUsernameNotIn(Collection<String> userList) ，方法的参数可以是 Collection 类型，也可以是数组或者不定长参数；

## 1、扩展1：支持order by、group by和limit
除了上述规则，框架还可以实现排序，用到OrderBy关键字，如：
```
    public List<User> findByIdGtOrderByAgeDesc(int id);
```
这里是根据年龄进行了倒序查询，Desc后缀表示倒序，Asc表示正序（也是默认值），多个字段排序时使用“And"连接，例：findByProductIdOrderByTimeLienDescAndIdDesc()。  

支持group by，用到GroupBy关键字，例如findAllGroupByAgeAndType()
支持limit，如findAllLimit3()或者findAllLimit(3)。

---

这三种关键字的支持，前后顺序为order by -> group by -> limit，不能颠倒顺序，否则报错。
正确写法：findAllOrderByIdGroupByAgeLimit5()


## 2、扩展2：支持getCount和getSumOf
上节的代码中还出现了getCountBy系列方法，规则和Spring JPA一致，是用来查询总数的。

还有getSumOf方法，例如getSumOfAgeByCreateTimeBetween(long begin, long end)   

## 3、扩展3：支持findAll
支持查询表中所有数据，但findAll只支持order by、group by和limit

## 4、智能判断返回类型
对于`List<User> findByNameLike(String name)`来说，将会自动去查询集合；对于`User findByNameLike(String name)`来说，将会自动取得第一个元素。

# 四、分页与SQL查询
上面说到分页需要写Sql，这个Sql定义在哪儿呢？  

定义的UserDao.sql如下：

```
##pageForUserList
select u.* from `user` u left join  `department` d on u.department_id=d.id
where 1=1
<#if notBlank(name)>
  and u.name like '%${name}%'
</#if>
<#if age gt 0>
  and u.age > ${age}
</#if>
<#if notNull(ids)>
  and u.id in (${join(ids,',')})
</#if>
<#if notBlank(nameDesc)>
  order by u.id asc
</#if>


##pageForUserListOfOrder
select u.* from `user` u left join  `department` d on u.department_id=d.id
order by u.id desc
```

他的规则非常简单，以`##`开头后跟Dao中的方法名，对应的就是Dao中同名的方法查询Sql。
需要注意的是分页的方法参数形式必须包含这以下两个类：
```
Map<String, Object> queryMap, PageableRequest request
```
queryMap作为Freemarker的root入参，request是分页请求

## 1、Freemarker模板

复杂SQL一般是有逻辑的，这里选用了Freemarker模板引擎来做条件判断和筛选。

如果你不熟悉Freemarker也没关系，掌握常用的if判断和取值即可。

`if`判断类似`<#if age gt 0></#if>`这种形式。下面说一下新手使用Freemarker需要注意的问题：

- Freemarker的判断式中大于不能用`>`符号，而要用`gt`；小于用`lt`
- Freemarker的取值使用`$`符号，如` ${name}`
- 我这里扩展了Freemarker的一些函数，如notBlank、notNull、join等，他们分别表示：不能为空白字符、不能为null、把集合用特定符号连接起来

## 2、分页之外
用sql查询，同时还支持以下形式：
```
    long queryAgeCount(long age, long departmentId);

    int queryAgeCount2(long age);

    String queryUserName(long id);

    Record queryUserAge(long age);

    List<Record> queryUserDepartment(long departmentId);
```
方法的形参会作为Freemarker的root入参，返回值支持String、Long、Integer、Double、Record、List<Record>这六种，会智能进行处理。  
注意：对于List只支持泛型为Record的返回值

## 3、特殊union的使用
有时候需要sql1+sql2才能得出结果，例如先查询出直播的场次，再查询出其他场次。最麻烦的是他们的结合还需要分页的支持，框架中已经支持了这个功能，只要在sql中用`#jujube-union`分割两个sql即可实现以上逻辑

## 4、特别注意in查询的使用
in查询条件可以传进来一个集合，然后用join函数进行处理：
```
<#if notNull(ids)>
  and u.id in (${join(ids,',')})
</#if>
```
也可以传进来一个字符串：
```
<#if notNull(ids)>
  and u.id in (${ids})
</#if>
```

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
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```
- Config配置中需要输入数据库表名、要生成到的项目代码根目录、entity所在包、dao所在包等信息

运行这个类即可在相应的路径中生成Entity和Dao类了。

# 六、使用
## 1、开发工具配置
因为涉及到读取接口方法的形参，所以IDE编译代码的时候要加上`-parameters`参数。  
IDEA在Build--Compiler--Java Compiler--Java Options--Additional command line parameters框中配置；  
Eclipse在Preferences->java->Compiler下勾选Store information about method parameters选项即可

另外项目编译方面需要注意，在Maven中配置如：
```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.6.1</version>
    <configuration>
        <compilerArgs>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```
## 2、项目配置
如果你用的是spring-boot的话，maven中加上依赖：

```
        <dependency>
            <groupId>io.github.jujube-framework</groupId>
            <artifactId>spring-boot-starter-jujube-jdbc</artifactId>
            <version>1.6</version>
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
    		<version>2.1</version>
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



---



关于sqlBasePackage的路径一般都放在resources下，赋值的时候按照package的形式进行赋值。

如果你想把sql放到src/main/java的classpath下，就必须在Maven的pom.xml中配置：

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
            <resource>
                <filtering>false</filtering>
                <directory>src/main/resources</directory>
                <includes>
                    <include>**/*.*</include>
                </includes>
            </resource>
        </resources>
    </build>
```

- 打开entity-generator项目的EntityGeneratorDemo来生成需要的entity和dao





# 实现原理介绍

入口在JujubeJdbcConfiguration，他实现了Spring注册器后置处理接口BeanDefinitionRegistryPostProcessor，主要是覆写了postProcessBeanDefinitionRegistry()方法。这个方法中有两步操作：1、用DaoSqlRegistry注册dao和dao sql的对应信息；2、通过ClassPathDaoScanner进行扫描，代理所有Dao接口。  

ClassPathDaoScanner中有关代理的逻辑是关键，主要在doScan()方法中，将扫描到的类的Class类型都设置为DaoFactoryBean，DaoFactoryBean是一个FactoryBean，在其getObject()中获得真实用到的Bean。

DaoProxyFactory是一个代理类的生产工厂，根据Dao的类型获得DaoProxy。DaoProxy中有查询的真正逻辑，先通过SqlBuilder通过Method对象获得sql模板，然后用BaseDaoSupport或JpaBaseDaoSupport进行真正的查询。

# 性能优化
主要是充分利用缓存和正则表达式相关的优化

## 1、充分利用缓存
凡是有比较重的运算且符合缓存条件的地方，都要用缓存，大到SQL的解析，小到正则分割字符串

## 2、正则表达式优化
Pattern.compile()是一个比较重的操作，建议把所有用到正则解析的地方都缓存起来，提供了PatternHolder类  
一个比较难发现的地方在于String中的split和replace方法，内部也使用了正则表达式，但没有缓存Pattern。建议用StringUtils.splitByWholeSeparator()和StringUtils.replace()替代，以达到更好的性能
