package org.jujubeframework.jdbc.base.util;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class SqlsTest {

    @Test
    public void getSecurityFieldName() {
        Assertions.assertThat(Sqls.getSecurityFieldName("name")).isEqualTo("`name`");
        Assertions.assertThat(Sqls.getSecurityFieldName("u.name")).isEqualTo("u.`name`");
    }
}