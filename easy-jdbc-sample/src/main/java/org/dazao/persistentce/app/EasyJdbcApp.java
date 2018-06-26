package org.dazao.persistentce.app;

import org.dazao.persistence.base.h2.H2JdbcTemplateAopSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan({ "org.dazao.persistence" })
public class EasyJdbcApp {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(EasyJdbcApp.class, args);
    }
    
    @Bean
    public H2JdbcTemplateAopSupport h2JdbcTemplateAopSupport() {
		return new H2JdbcTemplateAopSupport();
	}
}
