package org.jujubeframework.jdbc.persistence.base.h2;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * h2数据库返回的字段名都为大写，跟mysql不兼容，此处做一下处理
 *
 * @author John Li
 */
@Aspect
public class H2JdbcTemplateAopSupport {

    @Around("execution(* org.springframework.jdbc.core.JdbcTemplate.queryForList(..))")
    public Object queryForListAfter(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 当前访问的方法
        Method method = signature.getMethod();
        Type type = method.getGenericReturnType();
        Object result = joinPoint.proceed();
        String returnType = "java.util.List<java.util.Map<java.lang.String, java.lang.Object>>";
        if (returnType.equals(type.getTypeName())) {
            @SuppressWarnings("unchecked") List<Map<String, Object>> list = (List<Map<String, Object>>) result;
            result = convertListNewRecord(list);
        }
        return result;
    }

    @Around("execution(* org.springframework.jdbc.core.JdbcTemplate.queryForMap(..))")
    public Object queryForMapAfter(final ProceedingJoinPoint joinPoint) throws Throwable {
        @SuppressWarnings("unchecked") Map<String, Object> result = (Map<String, Object>) joinPoint.proceed();
        return convertNewRecord(result);
    }

    private List<Map<String, Object>> convertListNewRecord(List<Map<String, Object>> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> map : list) {
            result.add(convertNewRecord(map));
        }
        return result;
    }

    private static Map<String, Object> convertNewRecord(Map<String, Object> result) {
        Map<String, Object> newResult = new LinkedCaseInsensitiveMap<>();
        for (String key : result.keySet()) {
            newResult.put(key.toLowerCase(), result.get(key));
        }
        return newResult;
    }
}
