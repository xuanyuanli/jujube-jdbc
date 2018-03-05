package org.dazao.persistence.base.spec;

import static org.dazao.persistence.base.spec.SpecSupport.Op.BETWEEN;
import static org.dazao.persistence.base.spec.SpecSupport.Op.EQ;
import static org.dazao.persistence.base.spec.SpecSupport.Op.GT;
import static org.dazao.persistence.base.spec.SpecSupport.Op.GTE;
import static org.dazao.persistence.base.spec.SpecSupport.Op.IN;
import static org.dazao.persistence.base.spec.SpecSupport.Op.ISNOTEMPTY;
import static org.dazao.persistence.base.spec.SpecSupport.Op.ISNOTNULL;
import static org.dazao.persistence.base.spec.SpecSupport.Op.ISNULL;
import static org.dazao.persistence.base.spec.SpecSupport.Op.LIKE;
import static org.dazao.persistence.base.spec.SpecSupport.Op.LT;
import static org.dazao.persistence.base.spec.SpecSupport.Op.LTE;
import static org.dazao.persistence.base.spec.SpecSupport.Op.NOT;
import static org.dazao.persistence.base.spec.SpecSupport.Op.join;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.dazao.lang.Record;
import org.dazao.persistence.base.spec.SpecSupport.Op;
import org.dazao.persistence.base.util.Sqls;
import org.dazao.support.pagination.SearchSpec;
import org.dazao.util.Beans;
import org.dazao.util.CamelCase;
import org.dazao.util.Texts;

import com.google.common.collect.Maps;

import lombok.EqualsAndHashCode;

/**
 * 构建查询规格（Specification）
 * 
 * @author 李衡 Email：li15038043160@163.com
 */
@EqualsAndHashCode
public class Spec implements Cloneable {
    private Map<String, Object> specMap = Maps.newLinkedHashMap();

    private List<Object> namedParam = new ArrayList<Object>();
    private Sort sort = new Sort(this);
    private String groupBy;
    private String having;

    private int limit;
    private int limitBegin;

    public Map<String, Object> getSpecMap() {
        return specMap;
    }

    /**
     * 等于
     */
    public Spec eq(String fieldName, Object value) {
        specMap.put(join(EQ, fieldName), value);
        return this;
    }

    /**
     * like
     */
    public Spec like(String fieldName, Object value) {
        specMap.put(join(LIKE, fieldName), value);
        return this;
    }

    /**
     * not like
     */
    public Spec notlike(String fieldName, Object value) {
        specMap.put(join(Op.NOTLIKE, fieldName), value);
        return this;
    }

    /**
     * 大于
     */
    public Spec gt(String fieldName, Object value) {
        specMap.put(join(GT, fieldName), value);
        return this;
    }

    /**
     * 小于
     */
    public Spec lt(String fieldName, Object value) {
        specMap.put(join(LT, fieldName), value);
        return this;
    }

    /**
     * 大于等于
     */
    public Spec gte(String fieldName, Object value) {
        specMap.put(join(GTE, fieldName), value);
        return this;
    }

    /**
     * 小于等于
     */
    public Spec lte(String fieldName, Object value) {
        specMap.put(join(LTE, fieldName), value);
        return this;
    }

    /**
     * 不等于<>
     */
    public Spec not(String fieldName, Object value) {
        specMap.put(join(NOT, fieldName), value);
        return this;
    }

    /**
     * is null
     */
    public Spec isNull(String fieldName) {
        specMap.put(join(ISNULL, fieldName), null);
        return this;
    }

    /**
     * is not null
     */
    public Spec isNotNull(String fieldName) {
        specMap.put(join(ISNOTNULL, fieldName), null);
        return this;
    }

    /**
     * <> ''
     */
    public Spec isNotEmpty(String fieldName) {
        specMap.put(join(ISNOTEMPTY, fieldName), null);
        return this;
    }

    /**
     * in
     */
    public Spec in(String fieldName, Iterable<?> value) {
        Validate.isTrue(value != null && value.iterator().hasNext());
        specMap.put(join(IN, fieldName), value);
        return this;
    }

    /**
     * not in
     */
    public Spec notin(String fieldName, Iterable<?> value) {
        if (value != null && value.iterator().hasNext()) {
            specMap.put(join(Op.NOTIN, fieldName), value);
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
        specMap.put(join(Op.OR, "spec"), rule);
        return this;
    }

    /**
     * and
     */
    public Spec and(Spec... rule) {
        Validate.isTrue(rule != null && rule.length != 0);
        // 此处的字段名spec是无意义的,因为解析的时候用不到它，可以为任何非空值
        specMap.put(join(Op.AND, "spec"), rule);
        return this;
    }

    /**
     * between
     */
    public Spec between(String fieldName, Object valuePrev, Object valueNext) {
        specMap.put(join(BETWEEN, fieldName), new Object[] { valuePrev, valueNext });
        return this;
    }

    /** 返回排序对象 */
    public Sort sort() {
        return sort;
    }

    public Spec groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
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

    /** 查询条数限制 */
    public Spec limit(int size) {
        limit = size;
        return this;
    }

    /** 从第几条开始查询 */
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

    /** 获得过滤条件的sql */
    public String getFilterSql() {
        return getFilterSql(null);
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

    /** 获得过滤条件的params(必须先执行getFilterSql方法，namedParam才会有值) */
    public Object[] getFilterParams() {
        return namedParam.toArray();
    }

    /**
     * 构建数据规格说明
     * 
     * @param specMap
     *            规格
     * @param namedParam
     *            命名参数值（如果是=，为了安全，则需要使用命名参数）
     * @return
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
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append("= ?");
                namedParam.add(filter.value);
                break;
            case LIKE:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" like ").append("?");
                namedParam.add(filter.value);
                break;
            case NOTLIKE:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" not like ").append("?");
                namedParam.add(filter.value);
                break;
            case GT:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" > ").append("?");
                namedParam.add(filter.value);
                break;
            case LT:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" < ").append("?");
                namedParam.add(filter.value);
                break;
            case GTE:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" >= ").append("?");
                namedParam.add(filter.value);
                break;
            case LTE:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" <= ").append("?");
                namedParam.add(filter.value);
                break;
            case BETWEEN:
                Object[] arr = (Object[]) filter.value;
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" between ").append("?").append(" and ").append("?");
                namedParam.add(arr[0]);
                namedParam.add(arr[1]);
                break;
            case ISNOTNULL:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" is not null");
                break;
            case ISNULL:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" is null");
                break;
            case ISEMPTY:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" = ''");
                break;
            case ISNOTEMPTY:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" <> ''");
                break;
            case NOT:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" <> ").append("?");
                namedParam.add(filter.value);
                break;
            case IN:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" in(").append(Sqls.inJoin(filter.value)).append(")");
                break;
            case NOTIN:
                specSql.append(getTableAliasPrefix(alias) + Sqls.getSecurityFieldName(filter.fieldName)).append(" not in(").append(Sqls.inJoin(filter.value)).append(")");
                break;
            case OR:
            case AND:
                Spec[] specs = (Spec[]) filter.value;
                StringBuilder innerSpecSql = new StringBuilder("(");
                for (Spec rule : specs) {
                    // innerSpecSql 为空，开始进行or-and操作，前面没有or-and符号
                    if (innerSpecSql.length() == 1) {
                        if (rule.size() == 1) { // 如果当前Spec中只有一个条件，则不用加括号
                            innerSpecSql.append(buildQuerySpecification(rule, alias));
                        } else {
                            innerSpecSql.append("(").append(buildQuerySpecification(rule, alias)).append(")");
                        }
                    } else { // 之后就是中间环节，要加or-and符号
                        innerSpecSql.append(" ").append(filter.operator.name().toLowerCase()); // or-and
                        if (rule.size() == 1) {
                            innerSpecSql.append(" ").append(buildQuerySpecification(rule, alias));
                        } else {
                            innerSpecSql.append(" (").append(buildQuerySpecification(rule, alias)).append(")");
                        }
                    }
                }
                innerSpecSql.append(")");
                specSql.append(innerSpecSql);
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

    private String getTableAliasPrefix(String alias) {
        if (StringUtils.isBlank(alias)) {
            return "";
        }
        return alias + ".";
    }

    public Spec clone() {
        Spec spec = new Spec();
        spec.specMap = Maps.newLinkedHashMap(this.specMap);
        spec.sort = this.sort.clone(spec);
        spec.groupBy = this.groupBy;
        spec.having = this.having;
        spec.limit = this.limit;
        spec.limitBegin = this.limitBegin;
        return spec;
    }

    public static Spec newS() {
        return new Spec();
    }

    /** searchSpec转换为Spec */
    public static Spec valueOf(SearchSpec searchSpec) {
        Spec spec = newS();
        Record simpleSearchSpec = searchSpec.getSimpleSpec();
        for (String key : simpleSearchSpec.keySet()) {
            Object value = simpleSearchSpec.get(key);
            String[] arr = key.split("_");
            if (arr.length == 2) {
                String methodName = arr[0];
                String field = CamelCase.toUnderlineName(arr[1]);
                if ("sortby".equals(methodName)) {
                    // 排序
                    if (field.endsWith("_desc")) {
                        spec.sort().desc(field.substring(0, field.length() - 5));
                    } else {
                        spec.sort().asc(field);
                    }
                } else {
                    // 现在只支持eq、like、notlike、gt、lt、gte、lte、not系列方法
                    Method declaredMethod = Beans.getDeclaredMethod(Spec.class, methodName, String.class, Object.class);
                    if (declaredMethod != null) {
                        if (methodName.equalsIgnoreCase(Op.LIKE.name()) || methodName.equalsIgnoreCase(Op.NOTLIKE.name())) {
                            value = "%" + value + "%";
                        }
                        Beans.invoke(declaredMethod, spec, field, value);
                    }
                }
            }
        }
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
