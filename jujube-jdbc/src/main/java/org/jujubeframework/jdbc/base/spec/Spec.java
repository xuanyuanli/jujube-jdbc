package org.jujubeframework.jdbc.base.spec;

import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jujubeframework.exception.DaoQueryException;
import org.jujubeframework.jdbc.base.util.Sqls;
import org.jujubeframework.util.Texts;

import java.util.*;

/**
 * 构建查询规格（Specification）
 *
 * @author John Li Email：jujubeframework@163.com
 */
@EqualsAndHashCode
public final class Spec implements Cloneable {
    private Map<String, Object> specMap = Maps.newLinkedHashMap();

    private final List<Object> namedParam = new ArrayList<>();
    private Sort sort = new Sort(this);
    private String groupBy;
    private String having;

    private int limit;
    private int limitBegin;

    Map<String, Object> getSpecMap() {
        return specMap;
    }

    /**
     * 等于
     */
    public Spec eq(String fieldName, Object value) {
        // 不为null；如果为String类型，不为空
        if (isNotBlank(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.EQ, fieldName), value);
        } else {
            throw new DaoQueryException("eq查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    private boolean isNotBlank(Object value) {
        if (value == null) {
            return false;
        }
        return (!(value instanceof String) || ((String) value).length() != 0);
    }

    /**
     * like
     */
    public Spec like(String fieldName, Object value) {
        if (verifyLikeValue(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.LIKE, fieldName), value);
        } else {
            throw new DaoQueryException("like查询的值不符合规则。字段：" + fieldName + "，值为空或仅有%符号");
        }
        return this;
    }

    private boolean verifyLikeValue(Object value) {
        String symbol1 = "%";
        String symbol2 = "%%";
        return isNotBlank(value) && !symbol1.equals(value) && !symbol2.equals(value);
    }

    /**
     * not like
     */
    public Spec notlike(String fieldName, Object value) {
        if (verifyLikeValue(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.NOTLIKE, fieldName), value);
        } else {
            throw new DaoQueryException("not like查询的值不符合规则。字段：" + fieldName + "，值为空或仅有%符号");
        }
        return this;
    }

    /**
     * 大于
     */
    public Spec gt(String fieldName, Object value) {
        if (isNotBlank(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.GT, fieldName), value);
        } else {
            throw new DaoQueryException("gt查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * 小于
     */
    public Spec lt(String fieldName, Object value) {
        if (isNotBlank(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.LT, fieldName), value);
        } else {
            throw new DaoQueryException("lt查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * 大于等于
     */
    public Spec gte(String fieldName, Object value) {
        if (isNotBlank(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.GTE, fieldName), value);
        } else {
            throw new DaoQueryException("gte查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * 小于等于
     */
    public Spec lte(String fieldName, Object value) {
        if (isNotBlank(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.LTE, fieldName), value);
        } else {
            throw new DaoQueryException("lte查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * 不等于<>
     */
    public Spec not(String fieldName, Object value) {
        if (isNotBlank(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.NOT, fieldName), value);
        } else {
            throw new DaoQueryException("<>查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * is null
     */
    public Spec isNull(String fieldName) {
        specMap.put(SpecSupport.Op.join(SpecSupport.Op.ISNULL, fieldName), null);
        return this;
    }

    /**
     * is not null
     */
    public Spec isNotNull(String fieldName) {
        specMap.put(SpecSupport.Op.join(SpecSupport.Op.ISNOTNULL, fieldName), null);
        return this;
    }

    /**
     * <> ''
     */
    public Spec isNotEmpty(String fieldName) {
        specMap.put(SpecSupport.Op.join(SpecSupport.Op.ISNOTEMPTY, fieldName), null);
        return this;
    }

    /**
     * in
     */
    public Spec in(String fieldName, Iterable<?> value) {
        if (value != null && value.iterator().hasNext()) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.IN, fieldName), value);
        } else {
            throw new DaoQueryException("in查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * not in
     */
    public Spec notin(String fieldName, Iterable<?> value) {
        if (value != null && value.iterator().hasNext()) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.NOTIN, fieldName), value);
        } else {
            throw new DaoQueryException("not in查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * or
     *
     * @param rule
     *            最少为两个入参
     */
    public Spec or(Spec... rule) {
        Validate.isTrue(rule != null && rule.length >= 2);
        // 此处的字段名spec是无意义的,因为解析的时候用不到它，可以为任何非空值
        specMap.put(SpecSupport.Op.join(SpecSupport.Op.OR, "spec"), rule);
        return this;
    }

    /**
     * and
     */
    public Spec and(Spec... rule) {
        Validate.isTrue(rule != null && rule.length != 0);
        // 此处的字段名spec是无意义的,因为解析的时候用不到它，可以为任何非空值
        specMap.put(SpecSupport.Op.join(SpecSupport.Op.AND, "spec"), rule);
        return this;
    }

    /**
     * between
     */
    public Spec between(String fieldName, Object valuePrev, Object valueNext) {
        if (valuePrev != null && valueNext != null && StringUtils.isNotBlank(Objects.toString(valuePrev)) && StringUtils.isNotBlank(Objects.toString(valueNext))) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.BETWEEN, fieldName), new Object[] { valuePrev, valueNext });
        } else {
            throw new DaoQueryException("between查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * 返回排序对象
     */
    public Sort sort() {
        return sort;
    }

    public void groupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public Spec having(String having) {
        this.having = having;
        return this;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public String getHaving() {
        return having;
    }

    /**
     * 查询条数限制
     */
    public Spec limit(int size) {
        limit = size;
        return this;
    }

    /**
     * 从第几条开始查询
     */
    public Spec limitBegin(int end) {
        limitBegin = end;
        return this;
    }

    public int getLimitBegin() {
        return limitBegin;
    }

    public int getLimit() {
        return limit;
    }

    public Spec setAll(Map<String, Object> map) {
        Validate.isTrue(map != null);
        specMap.putAll(map);
        return this;
    }

    public int size() {
        return specMap.size();
    }

    public boolean isEmpty() {
        return specMap.isEmpty();
    }

    /**
     * 获得过滤条件的sql
     */
    public String getFilterSql() {
        namedParam.clear();
        String result = buildQuerySpecification(this, null);
        if (StringUtils.isBlank(result)) {
            result = "1=1";
        }
        return result;
    }

    /**
     * 获得过滤条件的sql
     *
     * @param alias
     *            表别名
     */
    public String getFilterSql(String alias) {
        namedParam.clear();
        String result = buildQuerySpecification(this, alias);
        if (StringUtils.isBlank(result)) {
            result = "1=1";
        }
        return result;
    }

    /**
     * 获得过滤条件的params(必须先执行getFilterSql方法，namedParam才会有值)
     */
    public Object[] getFilterParams() {
        return namedParam.toArray();
    }

    /**
     * 构建数据规格说明
     *
     * @param spec
     *            数据规格
     * @param alias
     *            表别名
     */
    private String buildQuerySpecification(Spec spec, String alias) {
        if (getSpecMap().isEmpty()) {
            return "";
        }
        StringBuilder specSql = new StringBuilder();
        Iterator<SpecSupport> filterIterator = SpecSupport.parse(spec.getSpecMap()).values().iterator();
        while (filterIterator.hasNext()) {
            SpecSupport filter = filterIterator.next();
            switch (filter.operator) {
            case EQ:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append("= ?");
                namedParam.add(filter.value);
                break;
            case LIKE:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" like ").append("?");
                namedParam.add(filter.value);
                break;
            case NOTLIKE:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" not like ").append("?");
                namedParam.add(filter.value);
                break;
            case GT:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" > ").append("?");
                namedParam.add(filter.value);
                break;
            case LT:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" < ").append("?");
                namedParam.add(filter.value);
                break;
            case GTE:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" >= ").append("?");
                namedParam.add(filter.value);
                break;
            case LTE:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" <= ").append("?");
                namedParam.add(filter.value);
                break;
            case BETWEEN:
                Object[] arr = (Object[]) filter.value;
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" between ").append("?").append(" and ").append("?");
                namedParam.add(arr[0]);
                namedParam.add(arr[1]);
                break;
            case ISNOTNULL:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" is not null");
                break;
            case ISNULL:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" is null");
                break;
            case ISEMPTY:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" = ''");
                break;
            case ISNOTEMPTY:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" <> ''");
                break;
            case NOT:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" <> ").append("?");
                namedParam.add(filter.value);
                break;
            case IN:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" in(").append(Sqls.inJoin(filter.value)).append(")");
                break;
            case NOTIN:
                specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" not in(").append(Sqls.inJoin(filter.value)).append(")");
                break;
            case OR:
            case AND:
                andHander(alias, specSql, filter);
                break;
            default:
                throw new RuntimeException("非法操作符");
            }
            if (filterIterator.hasNext()) {
                specSql.append(" and ");
            }
        }
        return specSql.toString();
    }

    private void andHander(String alias, StringBuilder specSql, SpecSupport filter) {
        Spec[] specs = (Spec[]) filter.value;
        StringBuilder innerSpecSql = new StringBuilder("(");
        for (Spec rule : specs) {
            // innerSpecSql 为空，开始进行or-and操作，前面没有or-and符号
            if (innerSpecSql.length() == 1) {
                // 如果当前Spec中只有一个条件，则不用加括号
                if (rule.size() == 1) {
                    innerSpecSql.append(buildQuerySpecification(rule, alias));
                } else {
                    innerSpecSql.append("(").append(buildQuerySpecification(rule, alias)).append(")");
                }
            } else { // 之后就是中间环节，要加or-and符号
                // or-and
                innerSpecSql.append(" ").append(filter.operator.name().toLowerCase());
                if (rule.size() == 1) {
                    innerSpecSql.append(" ").append(buildQuerySpecification(rule, alias));
                } else {
                    innerSpecSql.append(" (").append(buildQuerySpecification(rule, alias)).append(")");
                }
            }
        }
        innerSpecSql.append(")");
        specSql.append(innerSpecSql);
    }

    /**
     * 获得别名前缀
     */
    private String getTableAliasPrefix(String alias) {
        if (StringUtils.isBlank(alias)) {
            return "";
        }
        return alias + ".";
    }

    @Override
    public Spec clone() {
        try {
            Spec clone = (Spec) super.clone();
        } catch (CloneNotSupportedException ignored) {
        }
        Spec spec = new Spec();
        spec.specMap = Maps.newLinkedHashMap(this.specMap);
        spec.sort = this.sort.clone(spec);
        spec.groupBy = this.groupBy;
        spec.having = this.having;
        spec.limit = this.limit;
        spec.limitBegin = this.limitBegin;
        return spec;
    }

    @Override
    public String toString() {
        return Texts.format("Spec [sql:({0})  params:({1})  sort:({2})  groupby:({3})  having:({4}) limit:({5},{6})]", getFilterSql(), StringUtils.join(getFilterParams(), ","),
                String.valueOf(sort), groupBy, having, limitBegin + "", limit + "");
    }

    public boolean containsKey(Spec spec) {
        for (String k : spec.getSpecMap().keySet()) {
            if (this.specMap.containsKey(k)) {
                return true;
            }
        }
        return false;
    }

}
