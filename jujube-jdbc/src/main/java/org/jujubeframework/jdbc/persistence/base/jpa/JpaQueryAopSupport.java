package org.jujubeframework.jdbc.persistence.base.jpa;

import org.apache.commons.lang3.math.NumberUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jujubeframework.jdbc.constant.EasyJdbcConstants;
import org.jujubeframework.jdbc.persistence.base.BaseDao;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.math.BigDecimal;


/**
 * @author John Li
 */
@Component
@Aspect
public class JpaQueryAopSupport {

    @Around("@annotation(" + EasyJdbcConstants.BASE_PACKAGE_NAME + ".persistence.base.jpa.JpaQuery)")
    public Object japQueryAround(final ProceedingJoinPoint joinPoint) {
        // 参数值
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 当前访问的方法
        Method method = signature.getMethod();
        Object target = joinPoint.getTarget();

        JpaQueryProxyDao proxyDao = new JpaQueryProxyDao((BaseDao<?>) target, method, args);
        Object result = proxyDao.jpaQuery();

        return convertToReturnType(result, method.getReturnType());
    }

    /**
     * 将返回值转换为对应的类型
     */
    private Object convertToReturnType(Object obj, Class<?> returnType) {
        Object result = obj;
        if (obj == null) {
            if (returnType.equals(int.class) || returnType.equals(long.class) || returnType.equals(double.class)) {
                result = 0;
            }
        } else {
            // BigDecimal-to-Double
            if (obj instanceof BigDecimal) {
                BigDecimal number = (BigDecimal) obj;
                if (returnType.equals(Double.class) || returnType.equals(double.class)) {
                    result = NumberUtils.toDouble(number.toPlainString());
                }
            }
        }
        return result;
    }
}
