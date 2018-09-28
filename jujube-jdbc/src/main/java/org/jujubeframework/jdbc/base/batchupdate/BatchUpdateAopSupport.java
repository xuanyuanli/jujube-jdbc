package org.jujubeframework.jdbc.base.batchupdate;

import org.apache.commons.lang3.math.NumberUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.base.BaseDaoSupport;
import org.jujubeframework.jdbc.constant.JujubeJdbcConstants;
import org.jujubeframework.util.Beans;
import org.jujubeframework.util.Dates;
import org.jujubeframework.util.Jsons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 参考{@link BatchUpdate}注解<br>
 * 要使用此注解，需要手动注入这个类，而且需要Spring Redis的环境
 *
 * @author John Li
 */
@Aspect
public class BatchUpdateAopSupport {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterReturning(value = "@annotation(" + JujubeJdbcConstants.BASE_PACKAGE_NAME + ".persistence.base.batchupdate.BatchUpdate)", returning = "ro")
    public void batchUpdate(JoinPoint joinPoint, Object ro) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 当前访问的方法
        Method method = signature.getMethod();

        BatchUpdate batchUpdate = AnnotationUtils.findAnnotation(method, BatchUpdate.class);
        String key = batchUpdate.value();
        int size = batchUpdate.size();

        // 多久执行批量（秒）
        int expire = 60;
        long timeline = 0;
        String timelineKey = "batchupload:" + key;
        if (redisTemplate.hasKey(timelineKey)) {
            timeline = NumberUtils.toLong(redisTemplate.opsForValue().get(timelineKey));
        } else {
            timeline = Dates.now();
            redisTemplate.opsForValue().set(timelineKey, String.valueOf(timeline), 1, TimeUnit.DAYS);
        }
        // 批量更新
        if (redisTemplate.opsForList().size(key) >= size || Dates.now() - timeline > expire) {
            BaseDao<?,?> dao = (BaseDaoSupport<?,?>) joinPoint.getTarget();
            List<String> listValues = redisTemplate.opsForList().range(key, 0, size);
            List<Object> list = new ArrayList<>();
            for (String value : listValues) {
                list.add(Jsons.parseJson(value, Beans.getClassGenericType(dao.getClass())));
            }
            Beans.invoke(Beans.getDeclaredMethod(BaseDaoSupport.class, "batchUpdate", List.class), dao, list);

            // 把左侧对应数量的元素删除
            redisTemplate.opsForList().trim(key, size, -1);
            // 更新timeline到最新
            redisTemplate.opsForValue().set(timelineKey, String.valueOf(Dates.now()), 1, TimeUnit.DAYS);
        } else {
            redisTemplate.opsForList().rightPush(key, Jsons.toJson(ro));
        }
    }
}
