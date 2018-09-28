package org.jujubeframework.jdbc.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jujubeframework.jdbc.base.dialect.Dialect;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.base.util.Sqls;
import org.jujubeframework.jdbc.support.entity.BaseEntity;
import org.jujubeframework.jdbc.support.pagination.Pageable;
import org.jujubeframework.jdbc.support.pagination.PageableRequest;
import org.jujubeframework.jdbc.util.DataTypeConvertor;
import org.jujubeframework.lang.Record;
import org.jujubeframework.util.Beans;
import org.jujubeframework.util.Collections3;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基础Dao支持。子类都要重写getTableName()方法 <br>
 * 备注：
 *
 * <pre>
 * 1、查询单行数据，如果为空，则返回null
 * 2、查询单个数据，如果为空，则返回null；如果为int、float、long、double，则返回默认值
 * 3、查询多行数据，永远不会返回null。判断时用isEmpty()方法即可
 * </pre>
 *
 * @author John Li
 */
@Slf4j
public class BaseDaoSupport<T extends BaseEntity,PK extends Serializable> implements BaseDao<T, PK> {
    /** 真实的类型 */
    private final Class<T> realGenericType;
    /** 真实主键的类型 */
    private final Class<PK> realPrimayKeyType;
    /** 表名 */
    private final String tableName;

    private  JdbcTemplate jdbcTemplate;

    public BaseDaoSupport(Class<T> realGenericType,Class<PK> realPrimayKeyType, String tableName) {
        this.realGenericType = realGenericType;
        this.realPrimayKeyType = realPrimayKeyType;
        this.tableName = tableName;
    }

    public static Dialect getDialect() {
        return DIALECT;
    }

    public static final Dialect DIALECT = Dialect.DEFAULT;

    @Override
    public long save(T t) {
        Record currenRecord = Record.valueOf(t);

        List<Object> params = new ArrayList<Object>();
        String sql = DIALECT.forDbSave(getTableName(), currenRecord, params);
        if (sql.length() == 0) {
            return 0;
        }
        return save(sql, params.toArray());
    }

    @Override
    public boolean update(T t) {
        Record currenRecord = Record.valueOf(t);

        String primaryKeyName = getPrimayKeyName();
        Object id = currenRecord.get(primaryKeyName);
        if (id == null) {
            throw new IllegalArgumentException("没有id（更新数据库表）");
        }

        currenRecord.remove(primaryKeyName);

        List<Object> paras = new ArrayList<Object>();
        String sql = DIALECT.forDbUpdate(getTableName(), primaryKeyName, id, currenRecord, paras);

        if (sql.length() == 0) {
            return false;
        }
        return getJdbcTemplate().update(sql, paras.toArray()) > 0 ? true : false;
    }

    @Override
    public long saveOrUpdate(T t) {
        String primaryKeyName = getPrimayKeyName();
        Object id = Beans.getProperty(t, primaryKeyName);
        // 保存
        if (id == null) {
            id = save(t);
        } else {
            T perT = findById("id", (PK) id);
            if (perT == null) {
                throw new RuntimeException("数据库不存在此条数据（更新数据库表）");
            }
            update(t);
        }
        Beans.setProperty2(t, "id", id);
        return (long) id;
    }

    @Override
    public boolean deleteById(PK id) {
        String sql = DIALECT.forDbDeleteById(getTableName(), getPrimayKeyName());
        return getJdbcTemplate().update(sql, id) > 0 ? true : false;
    }

    /**
     * 根据条件删除数据
     */
    public boolean delete(Spec spec) {
        if (spec.isEmpty()) {
            throw new IllegalArgumentException("此为删除全部，请谨慎操作");
        }
        String sql = DIALECT.forDbDelete(getTableName(), spec.getFilterSql());
        boolean result = getJdbcTemplate().update(sql, spec.getFilterParams()) > 0;
        return result ? true : false;
    }

    /**
     * 批量更新数据
     */
    public void batchUpdate(String sql) {
        getJdbcTemplate().update(sql);
    }

    public void batchUpdate(String sql, Object... param) {
        getJdbcTemplate().update(sql, param);
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
        List<T> records = DataTypeConvertor.convertListRecordToListBean(realGenericType, list);
        return records;
    }

    public List<Record> findRecord(String sql, Object[] params) {
        List<Map<String, Object>> list = getJdbcTemplate().queryForList(sql, params);
        List<Record> records = DataTypeConvertor.convertListMapToListRecord(list);
        return records;
    }

    public Record findOneRecord(String sql, Object[] params) {
        return new Record(getJdbcTemplate().queryForMap(sql, params));
    }

    @Override
    public List<T> findAll() {
        String sql = DIALECT.forDbSimpleQuery("*", getTableName());
        return find(sql, toArrary());
    }

    @Override
    public List<Long> findIds() {
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
        String sql = DIALECT.forDbSimpleQuery(fields, getTableName(), spec.getFilterSql());
        if (StringUtils.isNotBlank(spec.getGroupBy())) {
            sql += (" group by " + spec.getGroupBy());
        }
        if (StringUtils.isNotBlank(spec.getHaving())) {
            sql += (" having " + spec.getHaving());
        }
        sql += spec.sort().buildSqlSort();
        int begin = spec.getLimitBegin() > 0 ? spec.getLimitBegin() : 0;
        if (spec.getLimit() > 0) {
            sql = DIALECT.forDbPaginationQuery(sql, begin, spec.getLimit());
        }
        return find(sql, spec.getFilterParams());
    }

    /**
     * 构建查询规格获得数据，可自定义select与from之间要查询的字段.永远不会返回null
     */
    public List<Record> findRecord(String fields, Spec spec) {
        String sql = DIALECT.forDbSimpleQuery(fields, getTableName(), spec.getFilterSql());
        if (StringUtils.isNotBlank(spec.getGroupBy())) {
            sql += (" group by " + spec.getGroupBy());
        }
        if (StringUtils.isNotBlank(spec.getHaving())) {
            sql += (" having " + spec.getHaving());
        }
        sql += spec.sort().buildSqlSort();
        int begin = spec.getLimitBegin() > 0 ? spec.getLimitBegin() : 0;
        if (spec.getLimit() > 0) {
            sql = DIALECT.forDbPaginationQuery(sql, begin, spec.getLimit());
        }
        return findRecord(sql, spec.getFilterParams());
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
     * 根据sql和params获得数据
     */
    public T findOne(String sql, Object[] params) {
        sql = DIALECT.forDbPaginationQuery(sql, 0, 1);
        Map<String, Object> map = queryForMap(sql, params);
        if (map != null) {
            return DataTypeConvertor.convertRecordToBean(realGenericType, new Record(map));
        }
        return null;
    }

    /**
     * 总数的查询
     */
    public long getCount(Spec spec) {
        spec.limitBegin(0).limit(Integer.MAX_VALUE);
        String sql = DIALECT.forDbSimpleQuery("count(*) as num", getTableName(), spec.getFilterSql());
        if (StringUtils.isNotBlank(spec.getGroupBy())) {
            sql += (" group by " + spec.getGroupBy());
        }
        if (StringUtils.isNotBlank(spec.getHaving())) {
            sql += (" having " + spec.getHaving());
        }
        return queryForObject(sql, Long.class, spec.getFilterParams());
    }

    public <E> E queryForObject(String sql, Class<E> requiredType, Object... params) {
        try {
            return getJdbcTemplate().queryForObject(sql, requiredType, params);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 主要用于针对一张表的分页
     */
    @SuppressWarnings("AlibabaUndefineMagicConstant")
    public Pageable<T> pagination(String fields, Spec spec, PageableRequest request) {
        // 防止sql报错
        String symbol = "*";
        if (fields.contains(symbol)) {
            fields = fields.replace(symbol, getTableName() + ".*");
        }
        request = PageableRequest.buildPageRequest(request);
        Pageable<T> result = request.newPageable();
        result.setTotalElements(request.getTotalElements());
        if (request.getIndex() == 1 || request.getTotalElements() < 1) {
            Spec countSpec = spec.clone();
            countSpec.sort().cleanValues();
            Long count = getCount(countSpec);
            count = count == null ? 0 : count;
            result.setTotalElements(count);
        }
        spec.limitBegin(result.getStart()).limit(result.getSize());
        result.setData(new ArrayList<>());
        if (result.getTotalElements() > 0) {
            result.setData(find(fields, spec));
        }
        return result;
    }

    /**
     * 主要用于针对一张表的分页
     */
    public Pageable<T> pagination(Spec spec, PageableRequest request) {
        return pagination("*", spec, request);
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
            count = count == null ? 0 : count;
            pageable.setTotalElements(count);
        }

        String cSql = DIALECT.forDbPaginationQuery(sql, pageable.getStart(), pageable.getSize());
        pageable.setData(new ArrayList<>());
        if (pageable.getTotalElements() > 0) {
            pageable.setData(DataTypeConvertor.convertListMapToListRecord(getJdbcTemplate().queryForList(cSql, filterParams)));
        }
        return pageable;
    }

    /**
     * 根据sql进行分页处理，用于两个集合union分页
     */
    public Pageable<Record> paginationBySqlOfUnion(PageableRequest request, String sqlA, String sqlB, Object[] filterParamsA, Object[] filterParamsB) {
        request = PageableRequest.buildPageRequest(request);
        Pageable<Record> pageable = request.newPageable();
        long ta = 0;
        long tb = 0;

        String countSql = Sqls.getCountSql(sqlA);
        Long count = getJdbcTemplate().queryForObject(countSql, Long.class, filterParamsA);
        count = count == null ? 0 : count;
        ta = count;

        countSql = Sqls.getCountSql(sqlB);
        count = getJdbcTemplate().queryForObject(countSql, Long.class, filterParamsB);
        count = count == null ? 0 : count;
        tb = count;

        pageable.setTotalElements(ta + tb);
        pageable.setData(new ArrayList<>());

        if (pageable.getTotalElements() > 0) {
            long curSizeEnd = pageable.getSize() * pageable.getIndex();
            // 如果第一个集合够填满当前分页,只取第一个集合内数据
            if (ta >= curSizeEnd) {
                String cSql = DIALECT.forDbPaginationQuery(sqlA, pageable.getStart(), pageable.getSize());
                pageable.setData(DataTypeConvertor.convertListMapToListRecord(getJdbcTemplate().queryForList(cSql, filterParamsA)));
                // 如果第一个集合不足以填满,取第一个集合数据+第二个集合数据
            } else if (ta > pageable.getStart()) {
                List<Map<String, Object>> data = new ArrayList<>();
                String cSql = DIALECT.forDbPaginationQuery(sqlA, pageable.getStart(), (int) (ta - pageable.getStart()));
                data.addAll(getJdbcTemplate().queryForList(cSql, filterParamsA));

                cSql = DIALECT.forDbPaginationQuery(sqlB, 0, (int) (curSizeEnd - ta));
                data.addAll(getJdbcTemplate().queryForList(cSql, filterParamsB));

                pageable.setData(DataTypeConvertor.convertListMapToListRecord(data));
            } else { // 如果第一个集合以用完，则只取第二个集合
                int start = (int) (pageable.getStart() - ta);
                String cSql = DIALECT.forDbPaginationQuery(sqlB, start, pageable.getSize());
                pageable.setData(DataTypeConvertor.convertListMapToListRecord(getJdbcTemplate().queryForList(cSql, filterParamsB)));
            }
        }

        return pageable;
    }

    /**
     * 处理过异常的queryForMap()方法
     */
    private Map<String, Object> queryForMap(String sql, Object... args) {
        try {
            return getJdbcTemplate().queryForMap(sql, args);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 可以返回id的方法,如果保存失败，会返回-1
     */
    private long save(final String sql, final Object... args) {
        long result = -1;
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            getJdbcTemplate().update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    if (args != null && args.length != 0) {
                        for (int j = 0; j < args.length; j++) {
                            ps.setObject(j + 1, args[j]);
                        }
                    }
                    return ps;
                }
            }, keyHolder);
            result = keyHolder.getKey().longValue();
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

    public String getPrimayKeyName() {
        return "id";
    }

    public String likeWrap(String field) {
        return Sqls.likeWrap(field);
    }

    public String leftLikeWrap(String field) {
        return Sqls.leftLikeWrap(field);
    }

    public String rightLikeWrap(String field) {
        return Sqls.likeWrap(field);
    }

    public boolean isNotBlank(String value) {
        return StringUtils.isNotBlank(value);
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


}
