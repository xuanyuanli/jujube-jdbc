package org.jujubeframework.jdbc.spring;

import org.jujubeframework.jdbc.binding.DaoSqlRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.*;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;


/**
 * JujubeJdbc配置类，也是一个Bean注册器后置处理器。会把{@link #basePackage}下所有的Dao注册到Spring容器中，并建立Dao与Sql文件间的对应信息
 *
 * @author John Li
 */
public class JujubeJdbcConfiguration implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationListener<ApplicationEvent> {

   static final String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

   /**Dao接口所在的package*/
    private String basePackage;

    /**Dao Sql所在的路径（相当于classpath来说）。如果不设置，默认为{@link #basePackage}.sql*/
    private String sqlBasePackage;

    /**是否监听Dao Sql变化而动态刷新sql缓存*/
    private boolean watchSqlFile;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //注册dao与dao sql的对应信息
        DaoSqlRegistry.setBasePackage(basePackage);
        DaoSqlRegistry.setSqlBasePackage(getSqlBasePackage());
        DaoSqlRegistry.setWatchSqlFile(watchSqlFile);
        DaoSqlRegistry.init();

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

    public String getSqlBasePackage() {
        if (sqlBasePackage ==null){
            sqlBasePackage = basePackage+".sql";
        }
        return sqlBasePackage;
    }

    public void setWatchSqlFile(boolean watchSqlFile) {
        this.watchSqlFile = watchSqlFile;
    }
}
