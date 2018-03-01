package com.yfs.persistence.base.batchupdate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.math.NumberUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.yfs.constant.Constants;
import com.yfs.persistence.base.BaseDao;
import com.yfs.util.Beans;
import com.yfs.util.Dates;
import com.yfs.util.Jsons;

/** BaseDao逻辑修炼了，所以这里需要重新编码 */
@Aspect
public class BatchUpdateAopSupport {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterReturning(value = "@annotation(" + Constants.BASE_PACKAGE_NAME + ".persistence.base.batchupdate.BatchUpdate)", returning = "ro")
    public void batchUpdate(JoinPoint joinPoint, Object ro) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod(); // 当前访问的方法

        BatchUpdate batchUpdate = AnnotationUtils.findAnnotation(method, BatchUpdate.class);
        String key = batchUpdate.value();
        int size = batchUpdate.size();

        int expire = 60; // 多久执行批量（秒）
        long timeline = 0;
        String timelineKey = "batchupload:" + key;
        if (redisTemplate.hasKey(timelineKey)) {
            timeline = NumberUtils.toLong(redisTemplate.opsForValue().get(timelineKey));
        } else {
            timeline = Dates.now();
            redisTemplate.opsForValue().set(timelineKey, String.valueOf(timeline), 1, TimeUnit.DAYS);
        }
        if (redisTemplate.opsForList().size(key) >= size || Dates.now() - timeline > expire) { // 批量更新
            BaseDao<?> dao = (BaseDao<?>) joinPoint.getTarget();
            List<String> listValues = redisTemplate.opsForList().range(key, 0, size);
            List<Object> list = new ArrayList<>();
            for (String value : listValues) {
                list.add(Jsons.parseJson(value, dao.getRealGenericType()));
            }
            Beans.invoke(Beans.getDeclaredMethod(BaseDao.class, "batchUpdate", List.class), dao, list);

            redisTemplate.opsForList().trim(key, size, -1); // 把左侧对应数量的元素删除
            redisTemplate.opsForValue().set(timelineKey, String.valueOf(Dates.now()), 1, TimeUnit.DAYS); // 更新timeline到最新
        } else {
            redisTemplate.opsForList().rightPush(key, Jsons.toJson(ro));
        }
    }
}
