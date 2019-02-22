package org.jujubeframework.jdbc.binding;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jujubeframework.jdbc.binding.fmtmethod.JoinMethod;
import org.jujubeframework.jdbc.binding.fmtmethod.NotBlankMethod;
import org.jujubeframework.jdbc.binding.fmtmethod.NotNullMethod;
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

    /**
     * 找到【${...}】这样的值
     */
    private static final String NOT_DIRECT_VAR_REGEX = "\\$\\{(.+?)\\}";
    private static final String BRACE_VAR_REGEX = "\\{(.+?)\\}";
    private static final String QUESTION_MARK = "?";

    private final List<String> originSql;
    private String sql;

    public SqlBuilder(List<String> originSql) {
        this.originSql = originSql;
        this.sql = StringUtils.join(originSql, " ");
    }


    /**
     * 构建最终的查询sql
     *
     * @param queryMap 入参
     * @return SqlResult包含了查询sql和查询参数集合
     */
    public SqlResult builder(Map queryMap) {
        SqlResult result = new SqlResult();
        List<Object> filterParam = new ArrayList<>();
        fullFreemarkerRoot(queryMap);
        StringBuilder tsql = new StringBuilder();
        String $ = "$";
        if (sql.contains($)){
            //替换${...}为@{${...}}
            String[] arr = sql.split("\\$");
            for (int i = 0; i < arr.length; i++) {
                String line = arr[i];
                if (i>0){
                    line = $ + line;
                }
                List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery(NOT_DIRECT_VAR_REGEX, line);
                for (Texts.RegexQueryInfo regexQueryInfo : regexQueryInfos) {
                    String group0 = regexQueryInfo.getGroups().get(0);
                    String text = "@{${" + group0 + "}}";
                    //对于join的特殊处理
                    if (group0.trim().startsWith("join(")){
                        text = "@{${" + group0 + "}}*";
                    }
                    line = StringUtils.join(line.substring(0, regexQueryInfo.getStart()), text, line.substring(regexQueryInfo.getEnd()));
                }
                tsql.append(line);
            }
            sql = Ftls.processStringTemplateToString(tsql.toString(), queryMap).replace("\n", " ");
            //根据@{...}获取值,值替换为sql中的问号，并填充filterParam
            arr = sql.split("@");
            tsql = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                String line = arr[i];
                List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery(BRACE_VAR_REGEX, line);
                for (Texts.RegexQueryInfo regexQueryInfo : regexQueryInfos) {
                    String group0 = regexQueryInfo.getGroups().get(0);
                    if (regexQueryInfo.getEnd() < line.length()-1){
                        char ch = line.charAt(regexQueryInfo.getEnd());
                        //对于in的特殊处理
                        if (ch == '*'){
                            line = StringUtils.join(line.substring(0, regexQueryInfo.getStart()), group0, line.substring(regexQueryInfo.getEnd()+1));
                            continue;
                        }
                        //对于like的特殊处理
                        if (ch == '\'' || ch == '%' || ch == '"'){
                            line = StringUtils.join(line.substring(0, regexQueryInfo.getStart()), group0.replace("'","\\'"), line.substring(regexQueryInfo.getEnd()));
                            continue;
                        }
                    }
                    filterParam.add(group0);
                    line = StringUtils.join(line.substring(0, regexQueryInfo.getStart()), QUESTION_MARK, line.substring(regexQueryInfo.getEnd()));
                }
                tsql.append(line);
            }
        }else {
            tsql.append(Ftls.processStringTemplateToString(sql.toString(), queryMap).replace("\n", " "));
        }
        result.setSql(tsql.toString());
        result.setFilterParams(filterParam.toArray());
        return result;
    }

    private void fullFreemarkerRoot(Map queryMap) {
        queryMap.put("join", new JoinMethod());
        queryMap.put("notBlank", new NotBlankMethod());
        queryMap.put("notNull", new NotNullMethod());
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
