package org.jujubeframework.jdbc.base.jpa.handler;

import java.lang.reflect.Method;
import java.util.List;

import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.base.util.Strings;

/**
 * @author John Li
 */
public class OrderByHandler implements Handler {

    public static final String ORDER_BY = "OrderBy";
    private static final String DESC = "Desc";
    private static final String ASC = "Asc";

    @Override
    public void handler(Method method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.contains(ORDER_BY)) {
            int index = truncationMethodName.indexOf(ORDER_BY);
            String mname = truncationMethodName.substring(index + ORDER_BY.length());
            String[] sarr = Strings.splitByAnd(mname);
            for (String field : sarr) {
                processField(method, spec, field);
            }
            truncationMethodName = truncationMethodName.substring(0, index);
        }
        chain.handler(method, spec, truncationMethodName, args);
    }

    private void processField(Method method, Spec spec, String field) {
        if (field.endsWith(DESC)) {
            field = getDbColumn(method, field.substring(0, field.length() - DESC.length()));
            spec.sort().desc(field);
        } else if (field.endsWith(ASC)) {
            spec.sort().asc(getDbColumn(method, field.substring(0, field.length() - ASC.length())));
        } else {
            spec.sort().asc(getDbColumn(method, field));
        }
    }

}
