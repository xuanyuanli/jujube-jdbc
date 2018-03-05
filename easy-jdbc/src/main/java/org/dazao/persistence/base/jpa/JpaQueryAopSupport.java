package org.dazao.persistence.base.jpa;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.apache.commons.lang3.math.NumberUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.dazao.constant.Constants;
import org.dazao.persistence.base.BaseDao;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class JpaQueryAopSupport {

    @Around("@annotation(" + Constants.BASE_PACKAGE_NAME + ".persistence.base.jpa.JpaQuery)")
    public Object cacheAround(final ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();// 参数值
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod(); // 当前访问的方法
        Object target = joinPoint.getTarget();

        JpaQueryProxyDao proxyDao = new JpaQueryProxyDao((BaseDao<?>) target, method, args);
        Object result = proxyDao.jpaQuery();

        return convertToReturnType(result, method.getReturnType());
    }

    /** 将返回值转换为对应的类型 */
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
