package org.jujubeframework.jdbc.base.datasource;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

/**
 * 数据源切片
 *
 * @author John Li
 */
@Slf4j
public class DateSourceServiceAspect {

    /**
     * 决策是否只读
     *
     * @param pjp 织入点
     */
    public void determineReadOrWriteDb(JoinPoint pjp) {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        // 重新获取方法，否则传递的是接口的方法信息
        boolean isReadCacheValue = isChoiceReadDb(method);
        if (isReadCacheValue) {
            DynamicDataSourceHolder.markRead();
        } else {
            DynamicDataSourceHolder.markWrite();
        }
    }

    /**
     * 判断是否只读方法
     *
     * @param method 执行方法
     * @return 当前方法是否只读
     */
    private boolean isChoiceReadDb(Method method) {
        boolean result = true;
        Transactional transactionalAnno = AnnotationUtils.findAnnotation(method, Transactional.class);
        // 如果是事务方法，则false
        if (transactionalAnno != null && !transactionalAnno.readOnly()) {
            result = false;
        }
        if (log.isDebugEnabled()) {
            log.debug("经过方法{}，结果：{}", method, result);
        }
        return result;
    }
}
