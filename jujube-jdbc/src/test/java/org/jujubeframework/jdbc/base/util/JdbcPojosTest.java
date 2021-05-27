package org.jujubeframework.jdbc.base.util;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import org.assertj.core.api.Assertions;
import org.jujubeframework.jdbc.support.annotation.Column;
import org.jujubeframework.jdbc.support.entity.BaseEntity;
import org.jujubeframework.lang.Record;
import org.junit.jupiter.api.Test;

import java.util.List;

class JdbcPojosTest {

    @Data
    @Accessors(chain = true)
    public static class JdbcPojosTestUser implements BaseEntity {
        private Long id;
        private String name;
        @Column("t_title")
        private String title;
    }

    @Test
    void mapping() {
        Record record = new Record();
        record.set("id", 10L).set("name", "li").set("t_title", "art");
        JdbcPojosTestUser user = JdbcPojos.mapping(record, JdbcPojosTestUser.class);
        Assertions.assertThat(user.getId()).isEqualTo(10L);
        Assertions.assertThat(user.getName()).isEqualTo("li");
        Assertions.assertThat(user.getTitle()).isEqualTo("art");
    }

    @Test
    void mappingArray() {
        Record record = new Record();
        record.set("id", 10L).set("name", "li").set("t_title", "art");
        List<JdbcPojosTestUser> users = JdbcPojos.mappingArray(Lists.newArrayList(record), JdbcPojosTestUser.class);
        Assertions.assertThat(users).hasSize(1);
        JdbcPojosTestUser user = users.get(0);
        Assertions.assertThat(user.getId()).isEqualTo(10L);
        Assertions.assertThat(user.getName()).isEqualTo("li");
        Assertions.assertThat(user.getTitle()).isEqualTo("art");
    }
}
