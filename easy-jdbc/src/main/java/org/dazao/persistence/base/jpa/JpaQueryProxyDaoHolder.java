package org.dazao.persistence.base.jpa;

import lombok.NoArgsConstructor;
import org.dazao.persistence.base.jpa.strategy.*;

/**
 * jpa query代理类的上下文保存者
 *
 * @author John Li
 */
@NoArgsConstructor
public class JpaQueryProxyDaoHolder {
    private static final ThreadLocal<JpaQueryProxyDao> HOLDER = new ThreadLocal<>();

    public static void setJpaQueryProxyDao(JpaQueryProxyDao target) {
        HOLDER.set(target);
    }

    public static JpaQueryProxyDao getJpaQueryProxyDao() {
        return HOLDER.get();
    }

    public  static  void remove(){
        HOLDER.remove();
    }
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
