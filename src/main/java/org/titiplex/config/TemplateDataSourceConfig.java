package org.titiplex.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class TemplateDataSourceConfig {

    @Bean(name = "templatesDataSource")
    @ConfigurationProperties(prefix = "app.templates.datasource")
    public DataSource templatesDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "templatesJdbcTemplate")
    public JdbcTemplate templatesJdbcTemplate(@Qualifier("templatesDataSource") DataSource templatesDataSource) {
        return new JdbcTemplate(templatesDataSource);
    }

    @Bean(name = "templatesTransactionManager")
    public PlatformTransactionManager templatesTransactionManager(
            @Qualifier("templatesDataSource") DataSource templatesDataSource
    ) {
        return new DataSourceTransactionManager(templatesDataSource);
    }
}
