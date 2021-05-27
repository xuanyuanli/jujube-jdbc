package org.jujubeframework.jdbc.base.util;

import com.google.common.collect.Lists;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlsTest {

    @Test
    public void getSecurityFieldName() {
        Assertions.assertThat(Sqls.getSecurityFieldName("name")).isEqualTo("`name`");
        Assertions.assertThat(Sqls.getSecurityFieldName("u.name")).isEqualTo("u.`name`");
    }

    @Test
    public void getCountSql() {
        // 普通
        String sql = "select * from user u";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM user u");

        // order by
        sql = "select * from user u order by u.age";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM user u");

        // 子查询+order by 1
        sql = "select * from (select * from a where a.id = 5) order by age";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM a WHERE a.id = 5)");

        // 子查询+order by 2
        sql = "select a.*,(select * from b where b.id = a.id limit 1) 't' from a order by a.age";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM a");

        // 子查询+order by 3
        sql = "select a.*,(select * from b where b.id = a.id group by b.age limit 1) 't' from (select * from u group by u.id) a order by a.age";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM u GROUP BY u.id) a");
    }

    @Test
    public void getCountSql2() {
        // 普通
        String sql = "select * from (select * from user u) t5";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM user u) t5");

        // order by
        sql = "select * from (select * from user u order by u.age) t5";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM user u ORDER BY u.age) t5");

        // 子查询+order by 1
        sql = "select * from (select * from (select * from a where a.id = 5) order by age) t5";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM (SELECT * FROM a WHERE a.id = 5) ORDER BY age) t5");

        // 子查询+order by 2
        sql = "select * from (select a.*,(select * from b where b.id = a.id limit 1) 't' from a order by a.age) t5";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT a.*, (SELECT * FROM b WHERE b.id = a.id LIMIT 1) 't' FROM a ORDER BY a.age) t5");

        // 子查询+order by 3
        sql = "select * from (select a.*,(select * from b where b.id = a.id group by b.age limit 1) 't' from (select * from u group by u.id) a order by a.age) t5";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo(
                "SELECT COUNT(*) FROM (SELECT a.*, (SELECT * FROM b WHERE b.id = a.id GROUP BY b.age LIMIT 1) 't' FROM (SELECT * FROM u GROUP BY u.id) a ORDER BY a.age) t5");

        // 子查询+order by 4
        sql = "select * from (select a.*,(select * from b where b.id = a.id limit 1) 't' from (select * from user) t2 group by a.age) t5";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql)
                .isEqualTo("SELECT COUNT(*) FROM (SELECT a.*, (SELECT * FROM b WHERE b.id = a.id LIMIT 1) 't' FROM (SELECT * FROM user) t2 GROUP BY a.age) t5");
    }

    @Test
    public void getCountSqlOfGroupBy() {
        String sql = "select * from user group by id order by age";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT 1 FROM user GROUP BY id ) getcountsql_t_t");

        sql = "select * from (select *,(select age from stu where name = '123') u from (select * from stu s) t1 group by id order by age) t2";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql)
                .isEqualTo("SELECT COUNT(*) FROM (SELECT *, (SELECT age FROM stu WHERE name = '123') u FROM (SELECT * FROM stu s) t1 GROUP BY id ORDER BY age) t2");
    }

    @Test
    public void getCountSqlOfUnion() {
        String sql = "select * from x group by id order by age union select * from stu s where s.type >5";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM x GROUP BY id ORDER BY age UNION SELECT * FROM stu s WHERE s.type > 5) getcountsql_t_t");
    }

    @Test
    public void getCountSqlOfUnion2() {
        String sql = "select * from (select * from x group by id order by age union select * from stu s where s.type >5) t";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM x GROUP BY id ORDER BY age UNION SELECT * FROM stu s WHERE s.type > 5) t");
    }

    @Test
    public void getCountSqlOfUnion3() {
        String sql = "select *,(select count(*) from user) num from (select * from x group by id order by age union select * from stu s where s.type >5) getcountsql_t_t";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM x GROUP BY id ORDER BY age UNION SELECT * FROM stu s WHERE s.type > 5) getcountsql_t_t");
    }

    @Test
    public void getCount4() {
        // 普通
        String sql = "select count(*) from user u group by id";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT 1 FROM user u GROUP BY id ) getcountsql_t_t");
    }

    @Test
    public void getCount5() {
        // 普通
        String sql = "select u.id from user u where (select count(*) from bill b where b.uid=u.id) > 0 group by id";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql)
                .isEqualTo("SELECT COUNT(*) FROM (SELECT 1 FROM user u WHERE (SELECT count(*) FROM bill b WHERE b.uid = u.id) > 0 GROUP BY id ) getcountsql_t_t");
    }

    @Test
    public void getCountOfJoin() {
        String sql = "select * from a left join b on b.id = a.id where a.age = 4 and b.id > 6";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM a LEFT JOIN b ON b.id = a.id  WHERE a.age = 4 AND b.id > 6");

        sql = "select * from a left join b on b.id = a.id left join c on c.id = b.bid where a.age = 4 and b.id > 6";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.bid  WHERE a.age = 4 AND b.id > 6");
    }

    @Test
    public void singleQuotes() {
        String sql = Sqls.inJoin(Lists.newArrayList("name", "17世纪 铜鎏金自在观音'd", "\\d'"));
        Assertions.assertThat(sql).isEqualTo("'name','17世纪 铜鎏金自在观音\\'d','\\\\d\\''");
    }

}
