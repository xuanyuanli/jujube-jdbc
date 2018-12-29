package org.jujubeframework.jdbc.base.jpa.handler;

import org.jujubeframework.jdbc.base.spec.Spec;

import java.util.List;


/**
 * @author John Li
 */
public class OrderByHandler implements Handler {

    private static final String AND = "And";
    private static final String ORDER_BY = "OrderBy";
    private static final String DESC = "Desc";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.contains(ORDER_BY)) {
            int index = methodName.indexOf(ORDER_BY);
            String mname = methodName.substring(index + ORDER_BY.length());
            String[] sarr = mname.split(AND);
            for (String field : sarr) {
                processField(spec, field);
            }
            methodName = methodName.substring(0, index);
        }
        chain.handler(spec, methodName, args);
    }

    private void processField(Spec spec, String field) {
        if (field.endsWith(DESC)) {
            field = realField(field.replace(DESC, ""));
            spec.sort().desc(field);
        } else {
            spec.sort().asc(realField(field));
        }
    }

}
