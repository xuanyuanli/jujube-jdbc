package org.jujubeframework.jdbc.base.jpa.strategy;

import org.assertj.core.api.Assertions;
import org.jujubeframework.jdbc.base.jpa.handler.HandlerTest;
import org.junit.jupiter.api.Test;

import java.util.List;

class BaseQueryStrategyTest {

    @Test
    void getDbColumnNames() {
        List<String> fieldList = BaseQueryStrategy.getDbColumnNames(HandlerTest.demoMethod(), "RecommendStatusAndId");
        Assertions.assertThat(fieldList).hasSize(2).containsSequence("recommend_status", "id");

        fieldList = BaseQueryStrategy.getDbColumnNames(HandlerTest.demoMethod(), "Title");
        Assertions.assertThat(fieldList).hasSize(1).containsSequence("title");


        fieldList = BaseQueryStrategy.getDbColumnNames(HandlerTest.demoMethod(), "AndroidVersion");
        Assertions.assertThat(fieldList).hasSize(1).containsSequence("android_version");

        fieldList = BaseQueryStrategy.getDbColumnNames(HandlerTest.demoMethod(), "AndroidVersionAndAliasName");
        Assertions.assertThat(fieldList).hasSize(2).containsSequence("android_version", "mname");
    }
}
