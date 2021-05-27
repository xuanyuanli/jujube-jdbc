package org.jujubeframework.jdbc;

import org.jujubeframework.jdbc.base.SqlQueryPostHandler;
import org.jujubeframework.util.Texts;

import java.util.Arrays;

/**
 * @author John Li
 */
public class SqlQueryPostHandlerDemo implements SqlQueryPostHandler {
    @Override
    public SqlQuery postHandle(String sql, Object[] params) {
        System.out.println(Texts.format("sql: {}, params: {}", sql, Arrays.asList(params)));
        return new SqlQuery(sql, params);
    }
}
