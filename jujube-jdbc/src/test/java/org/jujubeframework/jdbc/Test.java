package org.jujubeframework.jdbc;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jujubeframework.jdbc.base.jpa.handler.Handler;
import org.jujubeframework.jdbc.base.util.Strings;
import org.jujubeframework.jdbc.binding.fmtmethod.TypeOfMethod;
import org.jujubeframework.util.Ftls;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Test {
    private static final String FIND_BY_ID2_PREFIX = "find(.+?)ById";

    public static void main(String[] args2) {
        String field="nameDesc";
        ;
        System.out.println(field.substring(0, field.length() - "Desc".length()));
    }

    private static void test1() {
        System.out.println(Arrays.asList(Strings.splitByAnd("IdEqAndAndroidEqAndNameEqAndAgeGt")));

        String text = "NameGt";
        System.out.println(text.substring(0, text.length() - 2));
    }
}
