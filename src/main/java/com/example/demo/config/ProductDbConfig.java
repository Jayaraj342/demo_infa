package com.example.demo.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.example.demo.repository"})
public class ProductDbConfig {

    @Value("${product.datasource.cachePrepStmts:true}")
    private boolean cachePrepStmts;

    @Value("${product.datasource.prepStmtCacheSize:250}")
    private int prepStmtCacheSize;

    @Value("${product.datasource.prepStmtCacheSqlLimit:2048}")
    private int prepStmtCacheSqlLimit;

    @Value("${product.datasource.queryTimeoutKillsConnection:true}")
    private boolean queryTimeoutKillsConnection;

    @Value("${product.datasource.driver-class-name}")
    private String jdbcDriverClassName;

    @Value("${product.datasource.url}")
    private String jdbcUrl;

    @Value("${product.datasource.username}")
    private String dbUser;

    @Value("${product.datasource.password}")
    private String dbPassword;

//    @Value("${datasource.dbTransactionDefaultTimeout:5}")
//    private int dbTransactionDefaultTimeout;


    @Primary
    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "product.datasource")
    public DataSource dataSource() {
        HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder.create()
                .driverClassName(jdbcDriverClassName)
                .url(jdbcUrl)
                .username(dbUser)
                .password(dbPassword)
                .build();

        dataSource.addDataSourceProperty("cachePrepStmts", cachePrepStmts);
        dataSource.addDataSourceProperty("prepStmtCacheSize", prepStmtCacheSize);
        dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", prepStmtCacheSqlLimit);
//        dataSource.addDataSourceProperty("queryTimeoutKillsConnection", queryTimeoutKillsConnection);
//        dataSource.addDataSourceProperty("jakartaPersistenceQueryTimeout", 3);
//        dataSource.addDataSourceProperty("spring.jpa.properties.javax.persistence.query.timeout", 3000);
        return dataSource;
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("dataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.demo.model")
                .persistenceUnit("product")
                .build();
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory);
//        transactionManager.setDefaultTimeout(dbTransactionDefaultTimeout);
        return transactionManager;
    }

//    @Bean(name = "failFastTransactionManager")
//    public PlatformTransactionManager failFastTransactionManager(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
//
//        JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory);
//        transactionManager.setDefaultTimeout(20);
//        return transactionManager;
//    }
}