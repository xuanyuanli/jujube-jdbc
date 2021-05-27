package org.jujubeframework.jdbc.spring.event;

import org.springframework.context.ApplicationEvent;

/**
 * Object source 是Entity对象
 * 
 * @author John Li
 */
public class EntitySaveEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source
     *            the object on which the event initially occurred (never
     *            {@code null})
     */
    public EntitySaveEvent(Object source) {
        super(source);
    }
}
