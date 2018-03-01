package com.yfs.persistence.base.jpa.handler;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.yfs.persistence.base.spec.Spec;
import com.yfs.util.Texts;

public class LimitHandler implements Handler {

    private static final String LIMIT_D = "Limit(\\d+)$";

    public static String clear(String methodName) {
        return methodName.replaceAll(LIMIT_D, "");
    }

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        String[] groups = Texts.getGroups(LIMIT_D, methodName);
        if (groups.length > 1) {
            int limit = NumberUtils.toInt(groups[1]);
            spec.limit(limit);
        }
        chain.handler(spec, clear(methodName), args);
    }
}
