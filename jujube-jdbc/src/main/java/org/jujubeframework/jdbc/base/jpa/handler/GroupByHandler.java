package org.jujubeframework.jdbc.base.jpa.handler;

import org.apache.commons.lang3.StringUtils;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.base.util.Strings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author John Li
 */
public class GroupByHandler implements Handler {
    public static final String GROUP_BY = "GroupBy";

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.contains(GROUP_BY)) {
            int index = truncationMethodName.indexOf(GROUP_BY);
            String mname = truncationMethodName.substring(index + GROUP_BY.length());
            String[] sarr = Strings.splitByAnd(mname);
            List<String> groups = new ArrayList<>();
            for (String field : sarr) {
                field = getDbColumn(method, field);
                groups.add(field);
            }
            spec.groupBy(StringUtils.join(groups, ","));
            truncationMethodName = truncationMethodName.substring(0, index);
        }
        chain.handler(method, spec, truncationMethodName, args);
    }

}
