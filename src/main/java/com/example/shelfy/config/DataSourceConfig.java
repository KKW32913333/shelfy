package com.example.shelfy.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Shelfy用DB（プライマリ）
 * SHELFY_DATABASE_URL未設定時はH2ローカルDBを使用
 */
@Configuration
public class DataSourceConfig {

    @Value("${shelfy.datasource.url:}")
    private String databaseUrl;

    @Primary
    @Bean(name = "shelfyDataSource")
    public DataSource shelfyDataSource() {
        HikariConfig config = new HikariConfig();

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            // Neon PostgreSQL（本番）
            String jdbcUrl = convertToJdbc(databaseUrl);
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName("org.postgresql.Driver");
            config.addDataSourceProperty("ssl", "true");
            config.addDataSourceProperty("sslmode", "require");
            config.setMaximumPoolSize(3);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
        } else {
            // H2（ローカル開発）
            config.setJdbcUrl("jdbc:h2:file:./data/shelfy;AUTO_SERVER=TRUE");
            config.setDriverClassName("org.h2.Driver");
            config.setUsername("sa");
            config.setPassword("");
        }

        return new HikariDataSource(config);
    }

    /**
     * postgresql://user:pass@host/db → jdbc:postgresql://host/db?user=...&password=...
     */
    private String convertToJdbc(String url) {
        if (url.startsWith("jdbc:")) return url;
        // postgresql://username:password@host:port/database
        String withoutScheme = url.replace("postgresql://", "");
        String[] atParts = withoutScheme.split("@", 2);
        String credentials = atParts[0];
        String hostAndDb = atParts[1];
        String[] credParts = credentials.split(":", 2);
        String user = credParts[0];
        String password = credParts.length > 1 ? credParts[1] : "";
        return "jdbc:postgresql://" + hostAndDb + "?user=" + user
                + "&password=" + password + "&sslmode=require";
    }
}
