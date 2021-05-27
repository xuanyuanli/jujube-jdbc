package org.jujubeframework.jdbc.binding;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlBuilderTest {

    @Test
    public void builderBase() {
        List<String> originSql = Lists.newArrayList("<#if notBlank(name)>" , "  and (u.name like '%${name}%' or u.name = ${name})" + "</#if>",
                "<#if notNull(ids)>" , "  and u.id in (${join(ids,',')})" , "</#if>");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("ids", Lists.newArrayList("1", "2", "3"));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("    and u.id in ('1','2','3') ");

        originSql = Lists.newArrayList("<#if isFilter>abc</#if>");
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("isFilter", true);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("abc");
    }

    @Test
    public void builderBase2() {
        List<String> originSql = Lists.newArrayList("<#if notBlank(name)>" , "  and (u.name like '%${name}%' or u.name = ${name})" , "</#if>",
                "<#if notNull(ids)>" , "  and u.id in (${join(ids,',')})" , "</#if>", "<#if age?? && age gt 0>" , "  and u.age > ${age}" , "</#if>");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("name", "abc'");
        map.put("age", 10);
        map.put("ids", Lists.newArrayList(1, 2, 3));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("   and (u.name like ? or u.name = ?)     and u.id in (1,2,3)     and u.age > ? ");
        assertThat(result.getFilterParams()).hasSize(3).containsSequence("%abc'%", "abc'", 10);

        map = new HashMap<>();
        map.put("age", 10);
        map.put("ids", Lists.newArrayList(1, 2, 3));
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("    and u.id in (1,2,3)     and u.age > ? ");
        assertThat(result.getFilterParams()).hasSize(1).containsSequence(10);

        map = new HashMap<>();
        map.put("name", "abc'");
        map.put("ids", Lists.newArrayList(1, 2, 3));
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("   and (u.name like ? or u.name = ?)     and u.id in (1,2,3)  ");
        assertThat(result.getFilterParams()).hasSize(2).containsSequence("%abc'%", "abc'");

        map = new HashMap<>();
        map.put("name", "abc'");
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("   and (u.name like ? or u.name = ?)   ");
        assertThat(result.getFilterParams()).hasSize(2).containsSequence("%abc'%", "abc'");

        map = new HashMap<>();
        map.put("ids", Lists.newArrayList(1, 2, 3));
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("    and u.id in (1,2,3)  ");
        assertThat(result.getFilterParams()).hasSize(0);
    }

    @Test
    public void builderSimple() {
        List<String> originSql = Lists.newArrayList("select * from user");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user");
    }

    @Test
    public void builderLimit() {
        List<String> originSql = Lists.newArrayList("select * from user limit 10");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user limit 10");

        originSql = Lists.newArrayList("select * from user limit ${t}");
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("t", 10);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user limit ?");

        originSql = Lists.newArrayList("select * from user limit ${b},${e}");
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("b", 10);
        map.put("e", 5);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user limit ?,?");

        originSql = Lists.newArrayList("select * from user limit ${b},5");
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("b", 10);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user limit ?,5");

        originSql = Lists.newArrayList("select * from user limit ${b} , ${e}");
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("b", 10);
        map.put("e", 5);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user limit ? , ?");

        originSql = Lists.newArrayList("select * from user limit ${b} , 5");
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("b", 10);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user limit ? , 5");

        originSql = Lists.newArrayList("select * from (select * from user limit 1) t");
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from (select * from user limit 1) t");
    }

    @Test
    public void builderLimit2() {
        List<String> originSql = Lists.newArrayList("select u.* from user u,(select id form user limit ${a}) t where t.id = u.id");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 10);
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select u.* from user u,(select id form user limit ?) t where t.id = u.id");

        originSql = Lists.newArrayList("select u.* from user u,(select id form user limit ${a},${e}) t where t.id = u.id");
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("a", 10);
        map.put("e", 5);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select u.* from user u,(select id form user limit ?,?) t where t.id = u.id");
    }

    @Test
    public void builderFunction() {
        List<String> originSql = Lists.newArrayList("select * from user<#if notBlank(nameDesc)>10</#if>");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user");
    }

    @Test
    public void builderInner() {
        List<String> originSql = Lists.newArrayList("select * from user where id = ${cmd.id}");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("cmd", new User(12L));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user where id = ?");
        assertThat(result.getFilterParams()).hasSize(1).containsSequence(12L);
    }

    @Test
    public void builderInner2() {
        List<String> originSql = Lists.newArrayList("select * from user where id = ${cmd.id}");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        Map<Object, Object> root = new HashMap<>();
        root.put("id", 12L);
        map.put("cmd", root);
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user where id = ?");
        assertThat(result.getFilterParams()).hasSize(1).containsSequence(12L);
    }

    @Data
    @AllArgsConstructor
    private static class User {
        private Long id;
    }

    @Test
    public void builderUnion() {
        List<String> originSql = Lists.newArrayList("select * from user where id > ${id}", "#jujube-union ", "select * from user where id > ${id} and age = ${age}",
                "#jujube-union", "select * from auction_product where name like '%${productName}%' and status = ${status}");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("id", 10);
        map.put("age", 5);
        map.put("productName", "大明");
        map.put("status", 3);
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.isUnion()).isTrue();
        assertThat(result.getSql()).isEqualTo("select * from user where id > ? ");
        assertThat(result.getFilterParams()).containsSequence(10);

        assertThat(result.getUnionAfterSqlInfo().get(0).getSql()).isEqualTo("select * from user where id > ? and age = ?");
        assertThat(result.getUnionAfterSqlInfo().get(0).getFilterParams()).containsSequence(10, 5);

        assertThat(result.getUnionAfterSqlInfo().get(1).getSql()).isEqualTo("select * from auction_product where name like ? and status = ?");
        assertThat(result.getUnionAfterSqlInfo().get(1).getFilterParams()).containsSequence(3);
    }

}
