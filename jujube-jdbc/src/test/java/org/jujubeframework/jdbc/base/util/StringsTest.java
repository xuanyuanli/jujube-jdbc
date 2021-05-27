package org.jujubeframework.jdbc.base.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class StringsTest {

    @Test
    public void splitByAnd() {
        List<String> list = Arrays.asList(Strings.splitByAnd("IdEqAndNameEqAndAgeGtAndAndroidEq"));
        assertThat(list).hasSize(4).containsSequence("IdEq", "NameEq", "AgeGt", "AndroidEq");

        list = Arrays.asList(Strings.splitByAnd("IdEqAndNameEqAndAgeGt"));
        assertThat(list).hasSize(3).containsSequence("IdEq", "NameEq", "AgeGt");

        list = Arrays.asList(Strings.splitByAnd("AndroidEqAndIdEqAndNameEqAndAgeGt"));
        assertThat(list).hasSize(4).containsSequence("AndroidEq", "IdEq", "NameEq", "AgeGt");

        list = Arrays.asList(Strings.splitByAnd("IdEqAndAndroidEqAndNameEqAndAgeGt"));
        assertThat(list).hasSize(4).containsSequence("IdEq", "AndroidEq", "NameEq", "AgeGt");

        list = Arrays.asList(Strings.splitByAnd("IdEq"));
        assertThat(list).hasSize(1).containsSequence("IdEq");

        list = Arrays.asList(Strings.splitByAnd("AndroidEq"));
        assertThat(list).hasSize(1).containsSequence("AndroidEq");
    }
}
