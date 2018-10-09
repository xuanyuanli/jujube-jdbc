package org.jujubeframework.jdbc.base.jpa.strategy;

import com.google.common.collect.Lists;
import org.jujubeframework.jdbc.base.jpa.handler.DefaultHandlerChain;
import org.jujubeframework.jdbc.base.jpa.handler.HandlerContext;
import org.jujubeframework.jdbc.base.spec.Spec;
import org.jujubeframework.jdbc.support.entity.RecordEntity;
import org.jujubeframework.util.Collections3;
import org.jujubeframework.util.Texts;

import java.lang.reflect.Method;
import java.util.List;


/**
 * @author John Li
 */
public class FindAnyByAnyQuery extends BaseQueryStrategy {

    private static final String FIND_BY2 = "find(.+?)By(.+)";
    private static final String FIND_BY2_PREFIX = "find(.+?)By";

    @Override
    boolean accept(Method method) {
        String mname = method.getName();
        return Texts.find(mname, FIND_BY2);
    }

    @Override
    Object query(Method method, Object[] args) {
        String mname = method.getName();
        String[] arr = Texts.getGroups(FIND_BY2, mname);
        String queryField = arr[1];
        queryField = realField(queryField);
        String tmname = mname.replaceAll(FIND_BY2_PREFIX, EMPTY);

        Spec spec = Spec.newS();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        selfChain.handler(spec, tmname, Lists.newArrayList(args));

        boolean isFindOne = false;
        // 如果以findOneBy开头，或返回值是Record与BaseEntity
        if (!List.class.equals(method.getReturnType())) {
            isFindOne = true;
        }

        String newQueryField = buildQueryField(queryField);
        if (isFindOne) {
            RecordEntity record = proxyDao.findOne(newQueryField, spec);
            if (record != null) {
                return record.get(queryField);
            }
        } else {
            List<RecordEntity> list = proxyDao.find(newQueryField, spec);
            return Collections3.extractToList(list, queryField);
        }
        return null;
    }

}
