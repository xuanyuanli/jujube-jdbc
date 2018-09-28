package org.jujubeframework.jdbc.base.jpa;

import lombok.NoArgsConstructor;
import org.jujubeframework.jdbc.base.jpa.strategy.*;

/**
 * jpa query代理类的上下文保存者
 *
 * @author John Li
 */
@NoArgsConstructor
public class JpaQueryProxyDaoHolder {

    /**
     * 查询上下文
     */
    private static final Querier QUERIER = new Querier();

    static {
        QUERIER.addStrategy(new FindAnyByIdQuery());
        QUERIER.addStrategy(new FindByAnyQuery());
        QUERIER.addStrategy(new FindAnyByAnyQuery());
        QUERIER.addStrategy(new GetCountByAnyQuery());
        QUERIER.addStrategy(new NoRealizeQuery());
    }

    public static Querier getQuerier() {
        return QUERIER;
    }
}
