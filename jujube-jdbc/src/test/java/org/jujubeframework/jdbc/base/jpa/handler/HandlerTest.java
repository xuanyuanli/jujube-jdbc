package org.jujubeframework.jdbc.base.jpa.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.base.jpa.strategy.BaseQueryStrategy;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.support.annotation.Column;
import org.jujubeframework.jdbc.support.entity.BaseEntity;
import org.jujubeframework.util.Beans;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import lombok.Data;

public class HandlerTest {
    public interface HandlerEntityDao extends BaseDao<HandlerEntity, Long> {
        long findIdByRecommendStatus(int status);

        long demo();
    }

    @Data
    public static class HandlerEntity implements BaseEntity {
        private Long id;
        private Long recommendStatus;
        private String name;
        private String source;
        private String title;
        private String mobile;
        private String subTitle;
        private Integer userType;
        private Integer age;
        private Integer type;
        private Integer androidVersion;

        @Column("mname")
        private String aliasName;
    }

    private static Method getMethod(String methodName, Class<?>... clazz) {
        return Beans.getMethod(HandlerEntityDao.class, methodName, clazz);
    }

    public static Method demoMethod() {
        return getMethod("demo");
    }

    @Test
    public void eqHandler() {
        EqHandler eqHandler = new EqHandler();
        Spec spec = new Spec();
        String methodName = "UserType";
        List<Object> args = Lists.newArrayList(1);
        eqHandler.handler(demoMethod(), spec, methodName, args, null);
        assertThat(spec.getFilterSql()).isEqualTo("`user_type`= ?");
        assertThat(spec.getFilterParams()).contains(1).hasSize(1);
        assertThat(args).isEmpty();
    }

    @Test
    public void likeHandler() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "NameLikeAndSourceNotLike";
        List<Object> args = Lists.newArrayList("12", "微软");
        selfChain.handler(demoMethod(), spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo("(`name` like ? and `source` not like ?)");
        assertThat(args).isEmpty();
    }

    @Test
    public void complexSpec() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "AgeAndNameLikeAndTypeBetweenAndSourceInAndTitleIsNullAndSubTitleIsNotNullAndMobileNotOrderByIdDescLimit10";
        List<Object> args = Lists.newArrayList(12, "微软", 3, 6, Lists.newArrayList("a\\", "b'"), "15911105446");
        selfChain.handler(demoMethod(), spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo(
                "(`age`= ? and `name` like ? and `type` between ? and ? and `source` in('a\\\\','b\\'') and `title` is null and `sub_title` is not null and `mobile` <> ?)");
        assertThat(spec.sort().buildSqlSort()).isEqualTo(" order by id desc");
        assertThat(spec.getLimit()).isEqualTo(10);
        assertThat(args).isEmpty();
        assertThat(spec.getFilterParams()).hasSize(5).containsSequence(12, "微软", 3, 6, "15911105446");
    }

    @Test
    public void complexSpec2() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "SourceNotInAndTitleIn";
        List<Object> args = Lists.newArrayList(Lists.newArrayList("1", "2"), Lists.newArrayList("a", "b"));
        selfChain.handler(demoMethod(), spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo("(`source` not in('1','2') and `title` in('a','b'))");
        assertThat(args).isEmpty();
    }

    @Test
    public void groupBy() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.handler(demoMethod(), spec, "GroupByIdAndAgeLimit1", null);
        assertThat(spec.getGroupBy()).isEqualTo("id,age");
        assertThat(spec.getLimit()).isEqualTo(1);
    }

    @Test
    public void orderBy() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.handler(demoMethod(), spec, "OrderByIdAndAgeDescAndTitleAsc", null);
        assertThat(spec.sort().buildSqlSort()).isEqualTo(" order by id,age desc,title");
    }

    @Test
    public void simpleSpec() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "IdAndTitleIsNotEmpty";
        List<Object> args = Lists.newArrayList(12L);
        selfChain.handler(demoMethod(), spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo("(`id`= ? and `title` <> '')");
        Object[] filterParams = spec.getFilterParams();
        assertThat(filterParams).hasSize(1).containsSequence(12L);
    }

    @Test
    public void findAndByAnd() {
        Method method = getMethod("findIdByRecommendStatus", int.class);
        String queryField = BaseQueryStrategy.getDbColumnName(method, "RecommendStatus");
        assertThat(queryField).isEqualTo("recommend_status");

        queryField = BaseQueryStrategy.getDbColumnName(method, "aliasName");
        assertThat(queryField).isEqualTo("mname");
    }

    @Test
    public void handlerChain() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "AgeGtAndTypeLteAndSourceGteAndIdLt";
        List<Object> args = Lists.newArrayList(3, 6, 1, 2);
        selfChain.handler(demoMethod(), spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo("(`age` > ? and `type` <= ? and `source` >= ? and `id` < ?)");
        assertThat(spec.getFilterParams()).containsSequence(3, 6, 1, 2).hasSize(4);
    }


}
