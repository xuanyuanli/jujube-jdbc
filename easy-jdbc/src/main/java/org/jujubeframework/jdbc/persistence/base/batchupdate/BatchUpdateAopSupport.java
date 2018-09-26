package org.jujubeframework.jdbc.persistence.base.batchupdate;

import org.apache.commons.lang3.math.NumberUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jujubeframework.jdbc.constant.EasyJdbcConstants;
import org.jujubeframework.jdbc.persistence.base.BaseDao;
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
 * BaseDao逻辑修炼了，所以这里需要重新编码
 *
 * @author John Li
 */
@Aspect
public class BatchUpdateAopSupport {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterReturning(value = "@annotation(" + EasyJdbcConstants.BASE_PACKAGE_NAME + ".persistence.base.batchupdate.BatchUpdate)", returning = "ro")
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
            BaseDao<?> dao = (BaseDao<?>) joinPoint.getTarget();
            List<String> listValues = redisTemplate.opsForList().range(key, 0, size);
            List<Object> list = new ArrayList<>();
            for (String value : listValues) {
                list.add(Jsons.parseJson(value, dao.getRealGenericType()));
            }
            Beans.invoke(Beans.getDeclaredMethod(BaseDao.class, "batchUpdate", List.class), dao, list);

            // 把左侧对应数量的元素删除
            redisTemplate.opsForList().trim(key, size, -1);
            // 更新timeline到最新
            redisTemplate.opsForValue().set(timelineKey, String.valueOf(Dates.now()), 1, TimeUnit.DAYS);
        } else {
            redisTemplate.opsForList().rightPush(key, Jsons.toJson(ro));
        }
    }
}
