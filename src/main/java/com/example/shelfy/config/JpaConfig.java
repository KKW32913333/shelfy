package com.example.shelfy.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.shelfy.repository",
    entityManagerFactoryRef = "shelfyEntityManagerFactory",
    transactionManagerRef = "shelfyTransactionManager"
)
public class JpaConfig {

    @Primary
    @Bean(name = "shelfyEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean shelfyEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("shelfyDataSource") DataSource dataSource) {
        return builder
            .dataSource(dataSource)
            .packages("com.example.shelfy.model")
            .persistenceUnit("shelfy")
            .properties(Map.of(
                "hibernate.hbm2ddl.auto", "update",
                "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"
            ))
            .build();
    }

    @Primary
    @Bean(name = "shelfyTransactionManager")
    public PlatformTransactionManager shelfyTransactionManager(
            @Qualifier("shelfyEntityManagerFactory")
            LocalContainerEntityManagerFactoryBean factory) {
        return new JpaTransactionManager(factory.getObject());
    }
}
