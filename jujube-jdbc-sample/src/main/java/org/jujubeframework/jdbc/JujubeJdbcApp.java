package org.jujubeframework.jdbc;

import org.jujubeframework.jdbc.persistence.base.h2.H2JdbcTemplateAopSupport;
import org.jujubeframework.jdbc.persistence.base.jpa.JpaQueryAopSupport;
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
    
    @Bean
    public H2JdbcTemplateAopSupport h2JdbcTemplateAopSupport() {
		return new H2JdbcTemplateAopSupport();
	}

    @Bean
	public JpaQueryAopSupport jpaQueryAopSupport(){return new JpaQueryAopSupport();}
}
