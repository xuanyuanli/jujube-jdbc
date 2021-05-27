package org.jujubeframework.jdbc.base.jpa.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.lang.reflect.Method;

/**
 * jpa 查询前事件
 *
 * @author John Li
 */
public class JpaQueryPreEvent extends ApplicationEvent {
    public JpaQueryPreEvent(Method method, Object[] args) {
        super(new JpaQueryPreEventSource(method, args));
    }

    @Data
    @AllArgsConstructor
    public static class JpaQueryPreEventSource {
        private Method method;
        private Object[] args;
    }
}
