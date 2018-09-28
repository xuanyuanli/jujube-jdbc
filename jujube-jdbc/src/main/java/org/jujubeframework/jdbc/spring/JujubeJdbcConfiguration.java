package org.jujubeframework.jdbc.spring;

import org.jujubeframework.jdbc.base.BaseDao;
import org.jujubeframework.jdbc.base.BaseDaoSupport;
import org.jujubeframework.util.Beans;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.*;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.type.ClassMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.util.Arrays;


/**
 * @author John Li
 */
public class JujubeJdbcConfiguration implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationListener<ApplicationEvent> {

   static final String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

    private String basePackage;

    /**默认为{@value #basePackage}.sql*/
    private String sqlBasePackage;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            //刷新注册器
        }
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setSqlBasePackage(String sqlBasePackage) {
        this.sqlBasePackage = sqlBasePackage;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //注册dao与dao sql的对应信息

        //代理BaseDao的所有子接口
        ClassPathDaoScanner scanner = new ClassPathDaoScanner(registry);
        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));

        BeanDefinitionBuilder jobDetailBuilder = BeanDefinitionBuilder.genericBeanDefinition(SpringContextHolder.class);
        registry.registerBeanDefinition("springContextHolder",jobDetailBuilder.getBeanDefinition());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }
}
