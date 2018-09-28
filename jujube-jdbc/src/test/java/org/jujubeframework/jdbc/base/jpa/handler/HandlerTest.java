package org.jujubeframework.jdbc.base.jpa.handler;

import com.google.common.collect.Lists;
import static org.assertj.core.api.Assertions.assertThat;
import org.jujubeframework.jdbc.base.jpa.strategy.BaseQueryStrategy;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.util.Texts;
import org.junit.Test;

import java.util.List;

public class HandlerTest {

    @Test
    public void testSimpleHandler() {
        EqHandler eqHandler = new EqHandler();
        Spec spec = new Spec();
        String methodName = "UserType";
        List<Object> args = Lists.newArrayList("1");
        eqHandler.handler(spec, methodName, args, null);
        assertThat(spec.getFilterSql()).isEqualTo("`user_type`= ?");
        assertThat(spec.getFilterParams()).contains("1").hasSize(1);
        assertThat(args).isEmpty();
    }

    @Test
    public void testHandler1() {
        Spec spec = Spec.newS();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "AgeAndNameLikeAndTypeBetweenAndSourceInAndTitleIsNullAndSubTitleIsNotNullAndMobileNotSortByIdDescLimit10";
        List<Object> args = Lists.newArrayList("12", "微软", 3, 6, Lists.newArrayList(1, 2, 3), "15911105446");
        selfChain.handler(spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo("(`age`= ? and `name` like ? and `type` between ? and ? and `source` in(1,2,3) and `title` is null and `sub_title` is not null and `mobile` <> ?)");
        assertThat(spec.sort().buildSqlSort()).isEqualTo(" order by `id` desc");
        assertThat(spec.getLimit()).isEqualTo(10);
        assertThat(args).isEmpty();
    }

    @Test
    public void testHandler2() {
        Spec spec = Spec.newS();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "IdAndIndustryTitleIsNotEmpty";
        List<Object> args = Lists.newArrayList(12);
        selfChain.handler(spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo("(`id`= ? and `industry_title` <> '')");
        Object[] filterParams = spec.getFilterParams();
        assertThat(filterParams).hasSize(1);
        assertThat(filterParams[0]).isEqualTo(12);
    }

    @Test
    public void testHandler3() {
        String mname = "findIdByRecommendStatusIndexSortByOrderNumLimit14";
        String[] arr = Texts.getGroups("find(.+?)By(.+)", mname);
        String queryField = arr[1];
        queryField = BaseQueryStrategy.realField(queryField);
        String tmname = mname.replaceAll("find(.+?)By", "");
        assertThat(tmname).isEqualTo("RecommendStatusIndexSortByOrderNumLimit14");
        assertThat(queryField).isEqualTo("id");
    }

    @Test
    public void testHandler4() {
        Spec spec = Spec.newS();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "AgeGtAndTypeLteAndSourceGteAndIdLt";
        List<Object> args = Lists.newArrayList(3, 6, 1, 2);
        selfChain.handler(spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo("(`age` > ? and `type` <= ? and `source` >= ? and `id` < ?)");
        assertThat(spec.getFilterParams()).containsSequence(3, 6, 1, 2).hasSize(4);
    }
}
