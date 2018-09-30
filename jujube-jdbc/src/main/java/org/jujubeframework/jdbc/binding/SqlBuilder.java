package org.jujubeframework.jdbc.binding;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jujubeframework.jdbc.binding.sqlfunction.SqlFunctionContext;
import org.jujubeframework.util.Ftls;
import org.jujubeframework.util.Texts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sql构建器
 *
 * @author John Li
 */
public class SqlBuilder {
    private static final String DIRECT_VAR_REGEX = "(['%])#\\{(\\w+?)\\}(['%])";
    private static final String NOT_DIRECT_VAR_REGEX = "#\\{(\\w+?)\\}";
    private static final String QUESTION_MARK = "?";
    private final List<String> originSql;
    private String freemarkerSql;

    public SqlBuilder(List<String> originSql) {
        this.originSql = originSql;
        toFreemarkerSql();
    }

    /**原始sql转换为freemarker模板sql*/
    private void toFreemarkerSql() {
        List<String> sqlLines = new ArrayList<>(originSql.size());
        boolean meetIf = false;
        for (String line : originSql) {
            line = line.replace("${","#{");
            if (line.startsWith("@if")) {
                line = line.substring(3).trim();
                line = SqlFunctionContext.booleanSqlFunctionExecute(line);
                line = SqlFunctionContext.ifKeyworkBeforeProcess(line);
                sqlLines.add(line);
                meetIf = true;
            } else {
                if (SqlFunctionContext.containsLineSqlFunction(line)) {
                    line = SqlFunctionContext.lineSqlFunctionExecute(line);
                }
                sqlLines.add(line);
                if (meetIf) {
                    sqlLines.add(SqlFunctionContext.ifKeyworkPostProcess());
                    meetIf = false;
                }
            }
        }
        freemarkerSql = StringUtils.join(sqlLines, "\n");
        //先处理直接查询（包含like），逻辑是：如果是直接查询，则替换为freemarker模块
        List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery(DIRECT_VAR_REGEX, freemarkerSql);
        for (Texts.RegexQueryInfo regexQueryInfo : regexQueryInfos) {
            List<String> groups = regexQueryInfo.getGroups();
            freemarkerSql= freemarkerSql.replace(regexQueryInfo.getGroup(), groups.get(0)+"${"+groups.get(1)+"}"+groups.get(2));
        }
    }

    /**
     * 构建最终的查询sql
     * @param queryMap 入参
     * @return SqlResult包含了查询sql和查询参数集合
     */
    public SqlResult builder(Map queryMap) {
        SqlResult result = new SqlResult();
                List<Object> filterParam = new ArrayList<>();
        //处理非直接查询，统一替换为?符号
        List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery(NOT_DIRECT_VAR_REGEX, freemarkerSql);
        for (Texts.RegexQueryInfo regexQueryInfo : regexQueryInfos) {
            freemarkerSql = freemarkerSql.replace(regexQueryInfo.getGroup(), QUESTION_MARK);
            String var = regexQueryInfo.getGroups().get(0);
            filterParam.add(queryMap.get(var));
        }
        result.setSql(Ftls.processStringTemplateToString(freemarkerSql,queryMap).replace("\n"," "));
        result.setFilterParams(filterParam.toArray());
        return result;
    }

    /**
     * 包含了查询sql和查询参数集合
     */
    @Data
    public static class SqlResult {
        private String sql;
        private Object[] filterParams;
    }
}
