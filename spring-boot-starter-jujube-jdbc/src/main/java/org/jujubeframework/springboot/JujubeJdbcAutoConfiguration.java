package org.jujubeframework.springboot;

import org.jujubeframework.jdbc.spring.JujubeJdbcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

/**
 * 这里不能用@EnableConfigurationProperties的方式来获得属性，因为JujubeJdbcConfiguration本身是一个BeanDefinitionRegistryPostProcessor。<br>
 * 而@EnableConfigurationProperties的逻辑是：把Properties类动态注入到容器，然后用BindBeanPostProcessor来绑定属性。这里有一个矛盾就是JujubeJdbcConfiguration会先执行，所以获得的Properties类中的属性为空，因为他们还没有被绑定
 * @author John Li
 */
@Configuration
public class JujubeJdbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JujubeJdbcConfiguration jujubeJdbcFactoryBean(Environment environment){
        JujubeJdbcConfiguration jujubeJdbcFactoryBean = new JujubeJdbcConfiguration();
        jujubeJdbcFactoryBean.setBasePackage(getBasePackage(environment));
        jujubeJdbcFactoryBean.setSqlBasePackage(getSqlBasePackage(environment));
        return jujubeJdbcFactoryBean;
    }

    private String getBasePackage(Environment environment) {
        String property = environment.getProperty("jujube.jdbc.base-package");
        if (property==null){
            property = environment.getProperty("jujube.jdbc.basePackage");
        }
        return property;
    }

    private String getSqlBasePackage(Environment environment) {
        String property = environment.getProperty("jujube.jdbc.sql-base-package");
        if (property==null){
            property = environment.getProperty("jujube.jdbc.sqlBasePackage");
        }
        return property;
    }

}
