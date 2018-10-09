package org.jujubeframework.jdbc.binding;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlBuilderTest {

    @Test
    public void builder() {
        List<String> originSql = Lists.newArrayList("@if name.notBlank", "and u.name like '%${name}%' and age = ${age}", "@if ids.notNull", "and id in (ids.iter(','))");
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("name", "abc");
        map.put("age", 10);
        map.put("ids", Lists.newArrayList(1, 2, 3));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        System.out.println(result.getSql());
        System.out.println(StringUtils.join(result.getFilterParams(), "\t"));

    }
}