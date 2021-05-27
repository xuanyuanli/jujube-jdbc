package org.jujubeframework.jdbc.base.jpa.handler;

import org.apache.commons.lang3.math.NumberUtils;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.base.util.Strings;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author John Li
 */
public class LimitHandler implements Handler {

    private static final String LIMIT_D = "Limit(\\d+)$";
    private static final String LIMIT = "Limit";

    public static String clear(String methodName) {
        return methodName.substring(0, methodName.lastIndexOf(LIMIT));
    }

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        String[] groups = Strings.getGroups(LIMIT_D, truncationMethodName);
        if (groups.length > 1) {
            int limit = NumberUtils.toInt(groups[1]);
            spec.limit(limit);
            truncationMethodName = clear(truncationMethodName);
        } else if (truncationMethodName.endsWith(LIMIT)) {
            int index = args.size() - 1;
            spec.limit((int) args.get(index));
            args.remove(index);
            truncationMethodName = clear(truncationMethodName);
        }
        chain.handler(method, spec, truncationMethodName, args);
    }
}
