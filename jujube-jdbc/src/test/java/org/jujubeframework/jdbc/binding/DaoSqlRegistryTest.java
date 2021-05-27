package org.jujubeframework.jdbc.binding;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DaoSqlRegistryTest {

    @Test public void isJpaMethod() {
        Assertions.assertThat(DaoSqlRegistry.isJpaMethod("findAllOrderByName")).isTrue();
        Assertions.assertThat(DaoSqlRegistry.isJpaMethod("findByName")).isTrue();
        Assertions.assertThat(DaoSqlRegistry.isJpaMethod("findAgeByName")).isTrue();
        Assertions.assertThat(DaoSqlRegistry.isJpaMethod("findAgeAndTypeByName")).isTrue();
        Assertions.assertThat(DaoSqlRegistry.isJpaMethod("findAgeAndTypeById")).isTrue();
        Assertions.assertThat(DaoSqlRegistry.isJpaMethod("findByIdAndType")).isTrue();
        Assertions.assertThat(DaoSqlRegistry.isJpaMethod("getCountByName")).isTrue();
        Assertions.assertThat(DaoSqlRegistry.isJpaMethod("getSumOfAgeByName")).isTrue();


        Assertions.assertThat(DaoSqlRegistry.isJpaMethod("AfindaByName")).isFalse();
        Assertions.assertThat(DaoSqlRegistry.isJpaMethod("getCountryByName")).isFalse();
        Assertions.assertThat(DaoSqlRegistry.isJpaMethod("AgetCountryByName")).isFalse();
        Assertions.assertThat(DaoSqlRegistry.isJpaMethod("getSumOfByName")).isFalse();
    }
}
