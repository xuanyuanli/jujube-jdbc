package org.jujubeframework.jdbc.spring;

import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

import org.jujubeframework.jdbc.base.BaseDao;
import org.springframework.lang.Nullable;

/**
 * @author John Li
 */
public class ProxyBeanContext {
    private static final ConcurrentHashMap<Class<? extends BaseDao>, Object> CURRENT_PROXY = new ConcurrentHashMap<>();

    public static Object currentProxy(Class<? extends BaseDao> cl) throws IllegalStateException {
        if (Proxy.isProxyClass(cl)){
            cl = (Class<? extends BaseDao>) cl.getInterfaces()[0];
        }
        Object proxy = CURRENT_PROXY.get(cl);
        if (proxy == null) {
            throw new IllegalStateException("Cannot find current proxy: Set 'exposeProxy' property on Advised to 'true' to make it available.");
        }
        return proxy;
    }

    /**
     * Make the given proxy available via the {@code currentProxy()} method.
     * <p>
     * Note that the caller should be careful to keep the old value as appropriate.
     * 
     * @param proxy
     *            the proxy to expose (or {@code null} to reset it)
     * @see #currentProxy(Class)
     */
    @Nullable
    static void setCurrentProxy(Class<? extends BaseDao> cl, @Nullable Object proxy) {
        Object old = CURRENT_PROXY.get(cl);
        if (proxy != null) {
            CURRENT_PROXY.put(cl, proxy);
        } else {
            CURRENT_PROXY.remove(cl);
        }
    }

}
