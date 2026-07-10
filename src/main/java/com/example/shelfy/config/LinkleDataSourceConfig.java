package com.example.shelfy.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class LinkleDataSourceConfig {

    @Value("${linkle.datasource.url:}")
    private String linkleUrl;

    @Bean(name = "linkleDataSource")
    public DataSource linkleDataSource() {
        HikariConfig config = new HikariConfig();

        if (linkleUrl != null && !linkleUrl.isBlank()) {
            try {
                URI uri = new URI(linkleUrl.replace("postgresql://", "http://"));
                String host     = uri.getHost();
                int    port     = uri.getPort() == -1 ? 5432 : uri.getPort();
                String db       = uri.getPath().replaceFirst("/", "");
                String userInfo = uri.getUserInfo();
                String user     = userInfo.split(":")[0];
                String password = userInfo.contains(":") ? userInfo.split(":", 2)[1] : "";

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + db
                        + "?sslmode=require";

                config.setJdbcUrl(jdbcUrl);
                config.setUsername(user);
                config.setPassword(password);
                config.setDriverClassName("org.postgresql.Driver");
                config.setMaximumPoolSize(2);
                config.setMinimumIdle(1);
                config.setConnectionTimeout(30000);
                config.setMaxLifetime(1800000);
                config.setPoolName("LinklePool");
            } catch (Exception e) {
                throw new RuntimeException("LINKLE_DATABASE_URLの解析に失敗しました: " + e.getMessage(), e);
            }
        } else {
            config.setJdbcUrl("jdbc:h2:file:./data/linkle;AUTO_SERVER=TRUE");
            config.setDriverClassName("org.h2.Driver");
            config.setUsername("sa");
            config.setPassword("");
        }

        return new HikariDataSource(config);
    }

    @Bean(name = "linkleJdbcTemplate")
    public JdbcTemplate linkleJdbcTemplate() {
        return new JdbcTemplate(linkleDataSource());
    }
}
