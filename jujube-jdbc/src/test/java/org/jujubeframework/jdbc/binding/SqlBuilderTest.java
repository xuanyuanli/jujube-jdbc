package org.jujubeframework.jdbc.binding;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlBuilderTest {

    @Test
    public void builder() {
        List<String> originSql = Lists.newArrayList("<#if notBlank(name)>\n" + "  and u.name like '%${name}%' ${name}\n" + "</#if>",
                "<#if notNull(ids)>\n" + "  and u.id in (${join(ids,',')})\n" + "</#if>",
                "<#if age gt 0>\n" + "  and u.age > ${age}\n" + "</#if>");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("name", "abc");
        map.put("age", 10);
        map.put("ids", Lists.newArrayList(1, 2, 3));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        Assertions.assertThat(result.getSql()).isEqualTo("  and u.name like '%abc%' ?     and u.id in (1,2,3)     and u.age > ? ");
        Assertions.assertThat(result.getFilterParams()).hasSize(2).containsSequence("abc", "10");
    }

    @Test
    public void  builder2(){
        List<String> originSql = Lists.newArrayList("select * from user");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        Assertions.assertThat(result.getSql()).isEqualTo("select * from user");
    }

    @Test
    public void  builder3(){
        List<String> originSql = Lists.newArrayList("select * from user<#if notBlank(nameDesc)>10</#if>");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        Assertions.assertThat(result.getSql()).isEqualTo("select * from user");
    }
}