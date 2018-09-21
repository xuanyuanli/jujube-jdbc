package org.dazao.persistence.base.jpa.handler;

import org.dazao.persistence.base.spec.Spec;

import java.util.List;


public class SortHandler implements Handler {

    private static final String AND = "And";
    private static final String SORT_BY = "SortBy";
    private static final String DESC = "Desc";

    @Override
    public void handler(Spec spec, String methodName, List<Object> args, HandlerChain chain) {
        if (methodName.contains(SORT_BY)) {
            int index = methodName.indexOf(SORT_BY);
            String mname = methodName.substring(index + SORT_BY.length());
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
            field = Handler.realField(field.replace(DESC, ""));
            spec.sort().desc(field);
        } else {
            spec.sort().asc(Handler.realField(field));
        }
    }

}
