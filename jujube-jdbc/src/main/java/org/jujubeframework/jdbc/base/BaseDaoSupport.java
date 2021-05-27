package org.jujubeframework.jdbc.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jujubeframework.jdbc.base.dialect.Dialect;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.base.util.JdbcPojos;
import org.jujubeframework.jdbc.base.util.Sqls;
import org.jujubeframework.jdbc.binding.DaoSqlRegistry;
import org.jujubeframework.jdbc.binding.SqlBuilder;
import org.jujubeframework.jdbc.spring.SpringContextHolder;
import org.jujubeframework.jdbc.spring.event.EntitySaveEvent;
import org.jujubeframework.jdbc.spring.event.EntityUpdateEvent;
import org.jujubeframework.jdbc.support.entity.BaseEntity;
import org.jujubeframework.jdbc.base.jpa.entity.RecordEntity;
import org.jujubeframework.jdbc.support.pagination.Pageable;
import org.jujubeframework.jdbc.support.pagination.PageableRequest;
import org.jujubeframework.lang.Record;
import org.jujubeframework.util.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基础Dao支持。子类都要重写getTableName()方法 <br>
 * 备注：
 *
 * <pre>
 * 1、查询单行数据，如果为空，则返回null
 * 2、查询单个数据，如果为空，则返回null；如果为int、float、long、double基本类型，则返回默认值
 * 3、查询多行数据，永远不会返回null。判断时用isEmpty()方法即可
 * </pre>
 *
 * @author John Li
 */
@Slf4j
public class BaseDaoSupport<T extends BaseEntity, PK extends Serializable> implements BaseDao<T, PK> {
    /**
     * 真实的Entity类型
     */
    private final Class<T> realGenericType;
    /**
     * 真实主键的类型
     */
    private final Class<PK> realPrimayKeyType;
    /**
     * 表名
     */
    private final String tableName;
    private String primaryKeyName = "id";

    private JdbcTemplate jdbcTemplate;

    public BaseDaoSupport(Class<T> realGenericType, Class<PK> realPrimayKeyType, String tableName) {
        this.realGenericType = realGenericType;
        this.realPrimayKeyType = realPrimayKeyType;
        this.tableName = tableName;
    }

    public static final Dialect DIALECT = Dialect.DEFAULT;

    /** 对象转换为Record */
    private static <T extends BaseEntity> Record toRecord(T obj) {
        Record record = new Record();
        List<JdbcPojos.FieldColumn> fieldColumns = JdbcPojos.getFieldColumns(obj.getClass());
        for (JdbcPojos.FieldColumn fieldColumn : fieldColumns) {
            Object property = Beans.getProperty(obj, fieldColumn.getField());
            if (property != null) {
                record.set(fieldColumn.getColumn(), property);
                if (property.equals(BaseEntity.STRING_NULL)) {
                    record.set(fieldColumn.getColumn(), null);
                }
            }
        }
        return record;
    }

    @Override
    public PK save(T t) {
        Record currenRecord = toRecord(t);

        List<Object> params = new ArrayList<>();
        String sql = DIALECT.forDbSave(getTableName(), currenRecord, params);
        if (sql.length() == 0) {
            return DataGenerator.generateDefaultValueByParamType(realPrimayKeyType);
        }
        PK id = save(sql, params.toArray());
        id = Beans.getExpectTypeValue(id, getRealPrimayKeyType());
        Beans.setProperty(t, getPrimayKeyName(), id);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    SpringContextHolder.getApplicationContext().publishEvent(new EntitySaveEvent(t));
                }
            });
        } else {
            SpringContextHolder.getApplicationContext().publishEvent(new EntitySaveEvent(t));
        }
        return id;
    }

    @Override
    public boolean update(T t) {
        Record currenRecord = toRecord(t);
        String primaryKeyName = getPrimayKeyName();
        Object id = currenRecord.get(primaryKeyName);
        if (id == null) {
            throw new IllegalArgumentException("没有id（更新数据库表）");
        }
        currenRecord.remove(primaryKeyName);
        List<Object> paras = new ArrayList<>();
        String sql = DIALECT.forDbUpdate(getTableName(), primaryKeyName, id, currenRecord, paras);
        if (sql.length() == 0) {
            return false;
        }
        boolean result = getJdbcTemplate().update(sql, paras.toArray()) > 0;
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}], params:[{}]", sql, StringUtils.join(paras, ","));
        }
        if (result) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        SpringContextHolder.getApplicationContext().publishEvent(new EntityUpdateEvent(t));
                    }
                });
            } else {
                SpringContextHolder.getApplicationContext().publishEvent(new EntityUpdateEvent(t));
            }
        }

        return result;
    }

    @Override
    public PK saveOrUpdate(T t) {
        String primaryKeyName = getPrimayKeyName();
        Object pk = Beans.getProperty(t, primaryKeyName);
        // 保存
        if (pk == null) {
            pk = save(t);
        } else {
            T per = findById(getPrimayKeyName(), (PK) pk);
            if (per == null) {
                throw new RuntimeException("数据库不存在此条数据（更新数据库表）");
            }
            update(t);
        }
        Beans.setProperty(t, getPrimayKeyName(), pk);
        return Beans.getExpectTypeValue(pk, getRealPrimayKeyType());
    }

    @Override
    public boolean deleteById(PK id) {
        String sql = DIALECT.forDbDeleteById(getTableName(), getPrimayKeyName());
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}], params:[{}]", sql, id);
        }
        return getJdbcTemplate().update(sql, id) > 0;
    }

    /**
     * 根据条件删除数据
     */
    public boolean delete(Spec spec) {
        if (spec.isEmpty()) {
            throw new IllegalArgumentException("此为删除全部，请谨慎操作");
        }
        String sql = DIALECT.forDbDelete(getTableName(), spec.getFilterSql());
        Object[] filterParams = spec.getFilterParams();
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}], params:[{}]", sql, StringUtils.join(filterParams, ","));
        }
        return getJdbcTemplate().update(sql, filterParams) > 0;
    }

    /**
     * 批量更新数据
     */
    public void batchUpdate(String sql) {
        getJdbcTemplate().update(sql);
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}]", sql);
        }
    }

    public void batchUpdate(String sql, Object... param) {
        getJdbcTemplate().update(sql, param);
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}], params:[{}]", sql, StringUtils.join(param, ","));
        }
    }

    @Override
    public void batchUpdate(List<T> list) {
        for (T t : list) {
            try {
                saveOrUpdate(t);
            } catch (Exception e) {
                log.error("batchUpdate", e);
            }
        }
    }

    @Override
    public T findById(PK id) {
        return findOne(newSpec().eq(getPrimayKeyName(), id));
    }

    @Override
    public boolean exists(PK id) {
        return findById(getPrimayKeyName(), id) != null;
    }

    @Override
    public T findById(String fields, PK id) {
        return findOne(fields, newSpec().eq(getPrimayKeyName(), id));
    }

    /**
     * 根据sql和params获得数据，用于where后还有order by，group by等的情况.永远不会返回null
     */
    public List<T> find(String sql, Object[] params) {
        List<Record> list = findRecord(sql, params);
        if (RecordEntity.class.isAssignableFrom(realGenericType)) {
            return list.stream().map(r -> (T) new RecordEntity(r)).collect(Collectors.toList());
        } else {
            return JdbcPojos.mappingArray(list, realGenericType);
        }
    }

    public List<Record> findRecord(String sql, Object[] params) {
        SqlQueryPostHandler.SqlQuery sqlQuery = sqlPostHandle(sql, params);
        sql = sqlQuery.getSql();
        params = sqlQuery.getParams();
        List<Map<String, Object>> list = getJdbcTemplate().queryForList(sql, params);
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}], params:[{}]", sql, StringUtils.join(params, ","));
        }
        return list.stream().map(Record::new).collect(Collectors.toList());
    }

    /** sql后置处理 */
    private SqlQueryPostHandler.SqlQuery sqlPostHandle(String sql, Object[] params) {
        Set<SqlQueryPostHandler> sqlQueryPostHandlers = DaoSqlRegistry.getSqlQueryPostHandlers();
        if (!sqlQueryPostHandlers.isEmpty()) {
            for (SqlQueryPostHandler sqlQueryPostHandler : sqlQueryPostHandlers) {
                SqlQueryPostHandler.SqlQuery sqlQuery = sqlQueryPostHandler.postHandle(sql, params);
                sql = sqlQuery.getSql();
                params = sqlQuery.getParams();
            }
        }
        return new SqlQueryPostHandler.SqlQuery(sql, params);
    }

    public Record findRecordOne(String sql, Object[] params) {
        List<Record> record = findRecord(sql, params);
        if (record != null && !record.isEmpty()) {
            return record.get(0);
        }
        return null;
    }

    @Override
    public List<T> findAll() {
        String sql = DIALECT.forDbSimpleQuery("*", getTableName());
        return find(sql, toArrary());
    }

    @Override
    public List<PK> findIds() {
        return Collections3.extractToList(find(getPrimayKeyName(), newSpec()), getPrimayKeyName());
    }

    /**
     * 构建查询规格获得数据.永远不会返回null
     */
    public List<T> find(Spec spec) {
        return find("*", spec);
    }

    /**
     * 构建查询规格获得数据，可自定义select与from之间要查询的字段.永远不会返回null
     */
    public List<T> find(String fields, Spec spec) {
        // 如果查询条件为空，则不进行查询，防止搜索全表
        if (spec.isEmpty() && StringUtils.isBlank(spec.getGroupBy()) && spec.getLimit() <= 0 && spec.sort().isEmpty()) {
            return new ArrayList<>();
        }
        String securityTableName = DIALECT.getSecurityTableName(getTableName());
        String sql = DIALECT.forDbSimpleQuery(getSecurityField(fields, securityTableName), getTableName(), spec.getFilterSql(securityTableName));
        if (StringUtils.isNotBlank(spec.getGroupBy())) {
            sql += (" group by " + getSecurityField(spec.getGroupBy(), securityTableName));
        }
        if (StringUtils.isNotBlank(spec.getHaving())) {
            sql += (" having " + spec.getHaving());
        }
        sql += spec.sort().buildSqlSort();
        int begin = Math.max(spec.getLimitBegin(), 0);
        if (spec.getLimit() > 0) {
            sql = DIALECT.forDbPaginationQuery(sql, begin, spec.getLimit());
        }
        return find(sql, spec.getFilterParams());
    }

    /**
     * 构建查询规格获得一条数据
     */
    public T findOne(Spec spec) {
        return findOne("*", spec);
    }

    /**
     * 构建查询规格获得一条数据，可自定义select与from之间要查询的字段
     */
    public T findOne(String fields, Spec spec) {
        spec.limit(1);
        List<T> list = find(fields, spec);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 总数的查询
     */
    public long getCount(Spec spec) {
        spec.limitBegin(0).limit(Integer.MAX_VALUE);
        String securityTableName = DIALECT.getSecurityTableName(getTableName());
        String sql = DIALECT.forDbSimpleQuery("count(*)", getTableName(), spec.getFilterSql(securityTableName));
        Object[] filterParams = spec.getFilterParams();
        SqlQueryPostHandler.SqlQuery sqlQuery = sqlPostHandle(sql, filterParams);
        sql = sqlQuery.getSql();
        filterParams = sqlQuery.getParams();
        if (StringUtils.isNotBlank(spec.getGroupBy())) {
            sql += (" group by " + getSecurityField(spec.getGroupBy(), securityTableName));
            if (StringUtils.isNotBlank(spec.getHaving())) {
                sql += (" having " + spec.getHaving());
            }
            sql = Sqls.getCountSql(sql);
        }
        return Optional.ofNullable(queryForLong(sql, filterParams)).orElse(0L);
    }

    /** 获得安全group by后的字段 */
    private String getSecurityField(String field, String securityTableName) {
        if (field.contains(",")) {
            String[] arr = StringUtils.splitByWholeSeparator(field, ",");
            StringBuilder fields = new StringBuilder();
            for (String ele : arr) {
                if (!ele.contains(".")) {
                    fields.append(securityTableName).append(".").append(ele).append(",");
                }
            }
            if (fields.length() > 0) {
                return fields.substring(0, fields.length() - 1);
            }
        } else {
            if (!field.contains(".")) {
                return securityTableName + "." + field;
            }
        }
        return field;
    }

    /**
     * 总数的查询
     */
    public double getSumOf(String sumField, Spec spec) {
        spec.limitBegin(0).limit(Integer.MAX_VALUE);
        String securityTableName = DIALECT.getSecurityTableName(getTableName());
        String sql = DIALECT.forDbSimpleQuery("sum(" + getSecurityField(Sqls.getSecurityFieldName(sumField), securityTableName) + ")", getTableName(),
                spec.getFilterSql(securityTableName));
        Object[] filterParams = spec.getFilterParams();
        SqlQueryPostHandler.SqlQuery sqlQuery = sqlPostHandle(sql, filterParams);
        sql = sqlQuery.getSql();
        filterParams = sqlQuery.getParams();
        return Optional.ofNullable(queryForDouble(sql, filterParams)).orElse(0.0);
    }

    /**
     * 根据sql进行分页处理
     */
    public Pageable<Record> paginationBySql(String sql, PageableRequest request, Object... filterParams) {
        request = PageableRequest.buildPageRequest(request);
        Pageable<Record> pageable = request.newPageable();
        pageable.setTotalElements(request.getTotalElements());

        if (request.getIndex() == 1 || request.getTotalElements() < 1) {
            String countSql = Sqls.getCountSql(sql);
            Long count = getJdbcTemplate().queryForObject(countSql, Long.class, filterParams);
            if (log.isDebugEnabled()) {
                log.debug("sql:[{}], params:[{}]", countSql, StringUtils.join(filterParams, ","));
            }
            count = count == null ? 0 : count;
            pageable.setTotalElements(count);
        }

        String cSql = DIALECT.forDbPaginationQuery(sql, pageable.getStart(), pageable.getSize());
        pageable.setData(new ArrayList<>());
        if (pageable.getTotalElements() > 0) {
            List<Map<String, Object>> list = getJdbcTemplate().queryForList(cSql, filterParams);
            if (log.isDebugEnabled()) {
                log.debug("sql:[{}], params:[{}]", cSql, StringUtils.join(filterParams, ","));
            }
            pageable.setData(list.stream().map(Record::new).collect(Collectors.toList()));
        }
        return pageable;
    }

    /**
     * 根据sql进行分页处理，用于两个集合union分页
     */
    public Pageable<Record> paginationBySqlOfUnion(PageableRequest request, String sql1, Object[] filterParams1, List<SqlBuilder.UnionSqlInfo> unionSqlInfos) {
        request = PageableRequest.buildPageRequest(request);
        Pageable<Record> pageable = request.newPageable();
        long totalCount = 0;

        // 将基础sql封装到集合中做统一处理
        SqlBuilder.UnionSqlInfo baseSql = new SqlBuilder.UnionSqlInfo().setSql(sql1).setFilterParams(filterParams1);
        unionSqlInfos.add(0, baseSql);
        // 计算数据总量
        for (SqlBuilder.UnionSqlInfo sqlInfo : unionSqlInfos) {
            String countSql = Sqls.getCountSql(sqlInfo.getSql());
            Object[] filterParams = sqlInfo.getFilterParams();
            Long count = getJdbcTemplate().queryForObject(countSql, Long.class, filterParams);
            if (log.isDebugEnabled()) {
                log.debug("sql:[{}], params:[{}]", countSql, StringUtils.join(filterParams, ","));
            }
            count = count == null ? 0 : count;
            sqlInfo.setSqlCount(count);
            totalCount += count;
        }
        pageable.setTotalElements(totalCount);
        pageable.setData(new ArrayList<>());

        // 若总数据量不为空则查询并封装数据
        if (pageable.getTotalElements() > 0) {
            // 当前查询页结束数量
            long curSizeEnd = pageable.getStart() + pageable.getSize();
            // 累计查询数量
            long cumulativeNnm = 0;
            // 当前sql的起始索引
            int currentSqlIndex = pageable.getStart();
            // 剩余数据数量
            int surplusDataNum = pageable.getSize();
            // 数据集合
            List<Map<String, Object>> list = new ArrayList<>();

            for (SqlBuilder.UnionSqlInfo sqlInfo : unionSqlInfos) {
                cumulativeNnm += sqlInfo.getSqlCount();
                currentSqlIndex = Math.max(currentSqlIndex, 0);
                if (surplusDataNum > 0) {
                    // 如果第一个集合够填满当前分页,只取第一个集合内数据
                    Object[] filterParams = sqlInfo.getFilterParams();
                    if (cumulativeNnm > curSizeEnd) {
                        String cSql = DIALECT.forDbPaginationQuery(sqlInfo.getSql(), currentSqlIndex, surplusDataNum);
                        List<Map<String, Object>> listMap = getJdbcTemplate().queryForList(cSql, filterParams);
                        if (log.isDebugEnabled()) {
                            log.debug("sql:[{}], params:[{}]", cSql, StringUtils.join(filterParams, ","));
                        }
                        list.addAll(listMap);
                        surplusDataNum = 0;
                        // 如果第一个集合不足以填满,则将第一个集合数据取完,不足的数据在下次循环中补充
                    } else if (cumulativeNnm > pageable.getStart()) {
                        String cSql = DIALECT.forDbPaginationQuery(sqlInfo.getSql(), currentSqlIndex, surplusDataNum);
                        List<Map<String, Object>> listMap = getJdbcTemplate().queryForList(cSql, filterParams);
                        if (log.isDebugEnabled()) {
                            log.debug("sql:[{}], params:[{}]", cSql, StringUtils.join(filterParams, ","));
                        }
                        surplusDataNum -= listMap.size();
                        list.addAll(listMap);
                    }
                }
                curSizeEnd -= sqlInfo.getSqlCount();
                currentSqlIndex -= sqlInfo.getSqlCount();
            }
            // 若数据不为空则封装到分页对象中
            if (!list.isEmpty()) {
                pageable.setData(list.stream().map(Record::new).collect(Collectors.toList()));
            }
        }
        return pageable;
    }

    private Long queryForLong(String sql, Object... params) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("sql:[{}], params:[{}]", sql, StringUtils.join(params, ","));
            }
            return getJdbcTemplate().queryForObject(sql, Long.class, params);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Double queryForDouble(String sql, Object... params) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("sql:[{}], params:[{}]", sql, StringUtils.join(params, ","));
            }
            return getJdbcTemplate().queryForObject(sql, Double.class, params);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 可以返回id的方法,如果保存失败，会返回-1
     */
    private PK save(final String sql, final Object... args) {
        PK result;
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            getJdbcTemplate().update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                if (args != null && args.length != 0) {
                    for (int j = 0; j < args.length; j++) {
                        ps.setObject(j + 1, args[j]);
                    }
                }
                return ps;
            }, keyHolder);
            if (log.isDebugEnabled()) {
                log.debug("sql:[{}], params:[{}]", sql, StringUtils.join(args, ","));
            }
            result = (PK) keyHolder.getKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public Object[] toArrary(Object... objects) {
        return objects;
    }

    public Spec newSpec() {
        return new Spec();
    }

    @Override
    public String getPrimayKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    public Class<T> getRealGenericType() {
        return realGenericType;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Class<PK> getRealPrimayKeyType() {
        return realPrimayKeyType;
    }

    public BaseDaoSupport cloneSelf() {
        BaseDaoSupport jpaBaseDaoSupport = new BaseDaoSupport(this.getRealGenericType(), this.getRealPrimayKeyType(), this.getTableName());
        jpaBaseDaoSupport.setPrimaryKeyName(this.getPrimayKeyName());
        jpaBaseDaoSupport.setJdbcTemplate(this.getJdbcTemplate());
        return jpaBaseDaoSupport;
    }
}
