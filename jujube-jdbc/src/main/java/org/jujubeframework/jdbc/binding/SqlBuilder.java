package org.jujubeframework.jdbc.binding;

import lombok.Data;
import org.jujubeframework.jdbc.binding.sqlfunction.SqlFunctionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author John Li
 */
public class SqlBuilder {
    private final List<String> originSql;

    public SqlBuilder(List<String> originSql) {
        this.originSql = originSql;
        toFreemarkerSql();
    }

    private void toFreemarkerSql() {
        List<String> sqlLines = new ArrayList<>(originSql.size());
        for (String line : originSql) {
            if (line.startsWith("@if")) {

            } else if (SqlFunctionContext.containsLineSqlFunction(line)) {
                sqlLines.add(SqlFunctionContext.lineSqlFunctionExecute(line));
            } else {
                sqlLines.add(line);
            }
        }
    }

    public SqlResult builder(Map queryMap) {
        SqlResult result = new SqlResult();

        return result;
    }

    @Data
    public static class SqlResult {
        private String sql;
        private Object[] filterParams;
    }
}
