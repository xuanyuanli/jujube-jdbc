package org.jujubeframework.jdbc.base.datasource;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源切片
 *
 * @author John Li
 */
@Slf4j
public class DateSourceDaoAspect {

    static final ConcurrentHashMap<String, Boolean> CACHE = new ConcurrentHashMap<>();

    /**
     * 决策是否只读
     *
     * @param pjp 织入点
     */
    public Object determineReadOrWriteDb(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        // 重新获取方法，否则传递的是接口的方法信息
        boolean isReadCacheValue = isChoiceReadDb(method);
        if (isReadCacheValue) {
            DynamicDataSourceHolder.markRead();
        } else {
            DynamicDataSourceHolder.markWrite();
        }
        return pjp.proceed();
    }

    /**
     * 判断是否只读方法
     *
     * @param method 执行方法
     * @return 当前方法是否只读
     */
    private boolean isChoiceReadDb(Method method) {
        String methodName = method.getName();
        Boolean result = CACHE.get(methodName);
        if (result == null) {
            result = true;
            String query = "query";
            if (DynamicDataSourceHolder.isChoiceWrite() || !StringUtils.startsWith(methodName, query)) {
                result = false;
            }
            CACHE.put(methodName, result);
        }
        if (log.isDebugEnabled()) {
            log.debug("经过方法{}，结果：{}", method, result);
        }
        return result;
    }
}
