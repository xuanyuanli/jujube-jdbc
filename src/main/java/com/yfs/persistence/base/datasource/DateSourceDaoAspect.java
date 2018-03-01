package com.yfs.persistence.base.datasource;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import com.yfs.support.log.Logable;

/**
 * 数据源切片
 */
public class DateSourceDaoAspect extends Logable {

    static final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();

    /**
     * 决策是否只读
     * 
     * @param pjp
     *            织入点
     */
    public Object determineReadOrWriteDB(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        // 重新获取方法，否则传递的是接口的方法信息
        Boolean isReadCacheValue = isChoiceReadDB(method);
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
     * @param method
     *            执行方法
     * @return 当前方法是否只读
     */
    private boolean isChoiceReadDB(Method method) {
        String methodName = method.getName();
        Boolean result = cache.get(methodName);
        if (result == null) {
            result = true;
            if (DynamicDataSourceHolder.isChoiceWrite() || !StringUtils.startsWith(methodName, "query")) {
                result = false;
            }
            cache.put(methodName, result);
        }
        logger.debug("经过方法{}，结果：{}", method, result);
        return result;
    }
}
