package org.jujubeframework.jdbc.binding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.jujubeframework.jdbc.base.util.Strings;
import org.jujubeframework.jdbc.binding.fmtmethod.JoinMethod;
import org.jujubeframework.jdbc.binding.fmtmethod.NotBlankMethod;
import org.jujubeframework.jdbc.binding.fmtmethod.NotNullMethod;
import org.jujubeframework.jdbc.binding.fmtmethod.TypeOfMethod;
import org.jujubeframework.util.Beans;
import org.jujubeframework.util.Ftls;
import org.jujubeframework.util.Texts;

import java.util.*;

/**
 * Sql构建器
 *
 * @author John Li
 */
public class SqlBuilder {
    /**
     * union查询的段落分割标志
     */
    public static final String JUJUBE_UNION = "#jujube-union";
    public static final String SEPARATOR_CHARS = "@@__@@__@@";

    private final String unionBefore;
    private final String[] unionAfterArr;

    public SqlBuilder(List<String> originSql) {
        String sql = String.join(" ", originSql);
        String[] arr = StringUtils.splitByWholeSeparator(sql, JUJUBE_UNION);
        unionBefore = arr[0];
        if (arr.length == 1) {
            unionAfterArr = null;
        } else {
            unionAfterArr = Arrays.copyOfRange(arr, 1, arr.length);
        }
    }

    /**
     * 构建最终的查询sql
     *
     * @param queryMap
     *            入参
     * @return SqlResult包含了查询sql和查询参数集合
     */
    public SqlResult builder(Map queryMap) {
        SqlResult result = new SqlResult();
        SqlResult result1 = builderSqlResult(unionBefore, queryMap);
        result.setSql(result1.getSql());
        result.setFilterParams(result1.getFilterParams());
        if (unionAfterArr != null) {
            result.setUnion(true);
            List<UnionSqlInfo> unionSqlInfos = new ArrayList<>();
            for (String unionSql : unionAfterArr) {
                SqlResult sqlResult = builderSqlResult(unionSql.trim(), queryMap);
                UnionSqlInfo unionSqlInfo = new UnionSqlInfo().setSql(sqlResult.getSql()).setFilterParams(sqlResult.getFilterParams());
                unionSqlInfos.add(unionSqlInfo);
            }
            result.setUnionAfterSqlInfo(unionSqlInfos);
        }
        return result;
    }

    /**
     * 构建最终的查询sql
     *
     * @param queryMap
     *            入参
     * @return SqlResult包含了查询sql和查询参数集合
     */
    public static SqlResult builderSqlResult(String sourceSql, Map queryMap) {
        fullFreemarkerRoot(queryMap);
        SqlResult result = new SqlResult();
        // 这里为了获得param的真实类型，所以绕了一个弯：先把${..}给摘出来，然后用queryMap.get($1)即可获得真实类型
        FreemarkerSqlResult freemarkerSqlResult = getFreemarkerSqlResult(sourceSql, queryMap);
        result.setSql(freemarkerSqlResult.getSql());
        result.setFilterParams(freemarkerSqlResult.getFreemarkerParams().stream().map(e -> {
            if (e.startsWith("${")) {
                String key = e.substring(2, e.length() - 1);
                if (key.contains(".")) {
                    String[] arr = StringUtils.splitByWholeSeparator(key, ".");
                    return Beans.getProperty(queryMap.get(arr[0]), arr[1]);
                } else {
                    return queryMap.get(key);
                }
            } else {
                String[] groups = Strings.getGroups("\\$\\{(.*?)\\}", e);
                return StringUtils.replace(e, groups[0], "" + queryMap.get(groups[1]));
            }
        }).toArray());
        return result;
    }

    /** 获得摘除${..}之后的sql和param */
    private static FreemarkerSqlResult getFreemarkerSqlResult(String sourceSql, Map queryMap) {
        sourceSql = getAfterHandleLogicSql(sourceSql, queryMap);
        StringBuilder rsql = new StringBuilder();
        List<String> freemarkerParams = new ArrayList<>();
        List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery("('?)(\\%?)\\$\\{(.*?)\\}(\\%?)('?)", sourceSql);
        int start = 0;
        for (Texts.RegexQueryInfo queryInfo : regexQueryInfos) {
            // like的处理
            if (queryInfo.getGroup().startsWith("'")) {
                freemarkerParams.add(queryInfo.getGroups().get(1) + "${" + queryInfo.getGroups().get(2) + "}" + queryInfo.getGroups().get(3));
                rsql.append(sourceSql, start, queryInfo.getStart()).append("?");
            }
            // in的处理，不做${..}的摘除，直接freemarker去执行
            else if (queryInfo.getGroup().contains("join(")) {
                rsql.append(sourceSql, start, queryInfo.getEnd());
            }
            // 其他处理
            else {
                freemarkerParams.add(queryInfo.getGroup());
                rsql.append(sourceSql, start, queryInfo.getStart()).append("?");
            }
            start = queryInfo.getEnd();
        }
        rsql.append(sourceSql.substring(start));
        String finalSql = Ftls.processStringTemplateToString(rsql.toString(), queryMap);
        return new FreemarkerSqlResult(finalSql, freemarkerParams);
    }

    /** 执行除了${..}取值之外的所有逻辑（包含if、for等） */
    private static String getAfterHandleLogicSql(String sourceSql, Map queryMap) {
        StringBuilder rsql = new StringBuilder();
        List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery("\\$\\{(.*?)\\}", sourceSql);
        int start = 0;
        for (Texts.RegexQueryInfo queryInfo : regexQueryInfos) {
            rsql.append(sourceSql, start, queryInfo.getStart()).append("${\"$\"}{").append(queryInfo.getGroups().get(0)).append("}");
            start = queryInfo.getEnd();
        }
        rsql.append(sourceSql.substring(start));
        return Ftls.processStringTemplateToString(rsql.toString(), queryMap);
    }

    /** 填充Freemarker Root */
    private static void fullFreemarkerRoot(Map queryMap) {
        if (queryMap == null) {
            queryMap = new HashMap<>(4);
        }
        queryMap.put("join", new JoinMethod());
        queryMap.put("notBlank", new NotBlankMethod());
        queryMap.put("notNull", new NotNullMethod());
        queryMap.put("typeOf", new TypeOfMethod());
    }

    /**
     * 包含了查询sql和查询参数集合
     */
    @Data
    @Accessors(chain = true)
    public static class SqlResult {
        private boolean union;
        private String sql;
        private Object[] filterParams;
        /** 联合查询的SQL及参数信息集合 */
        List<UnionSqlInfo> unionAfterSqlInfo;
    }

    /** 联合查询的SQL及参数信息 */
    @Data
    @Accessors(chain = true)
    public static class UnionSqlInfo {
        private String sql;
        private Object[] filterParams;
        private long sqlCount;
    }

    /** sql中的?和freemarkerParams的${..}一一对应 */
    @Data
    @AllArgsConstructor
    private static class FreemarkerSqlResult {
        private String sql;
        /** 形如${..}的组合 */
        private List<String> freemarkerParams;
    }
}
