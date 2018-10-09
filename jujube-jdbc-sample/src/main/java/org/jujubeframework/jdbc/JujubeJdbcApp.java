package org.jujubeframework.jdbc;

import org.jujubeframework.jdbc.base.h2.H2JdbcTemplateAopSupport;
import org.jujubeframework.jdbc.persistence.UserDao;
import org.jujubeframework.jdbc.spring.JujubeJdbcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author John Li
 */
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class JujubeJdbcApp {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(JujubeJdbcApp.class, args);
    }

    @Autowired
    UserDao userDao;

    @Bean
    public H2JdbcTemplateAopSupport h2JdbcTemplateAopSupport() {
        return new H2JdbcTemplateAopSupport();
    }

    @Bean
    public JujubeJdbcConfiguration jujubeJdbcFactoryBean() {
        JujubeJdbcConfiguration jujubeJdbcFactoryBean = new JujubeJdbcConfiguration();
        jujubeJdbcFactoryBean.setBasePackage("org.jujubeframework.jdbc");
        jujubeJdbcFactoryBean.setSqlBasePackage("dao-sql");
        return jujubeJdbcFactoryBean;
    }
}
