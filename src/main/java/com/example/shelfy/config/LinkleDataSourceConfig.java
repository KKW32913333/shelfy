package com.example.shelfy.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Linkle用DB（セカンダリ）
 * ・app_user / group / group_membership を読み取り（認証用）
 * ・shopping_item へ書き込み（在庫0連携）
 */
@Configuration
public class LinkleDataSourceConfig {

    @Value("${linkle.datasource.url:}")
    private String linkleUrl;

    @Bean(name = "linkleDataSource")
    public DataSource linkleDataSource() {
        HikariConfig config = new HikariConfig();

        if (linkleUrl != null && !linkleUrl.isBlank()) {
            String jdbcUrl = convertToJdbc(linkleUrl);
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName("org.postgresql.Driver");
            config.addDataSourceProperty("ssl", "true");
            config.addDataSourceProperty("sslmode", "require");
            config.setMaximumPoolSize(2);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(30000);
            config.setMaxLifetime(1800000);
            config.setPoolName("LinklePool");
        } else {
            // ローカル開発時：Linkle DB未設定ならH2で代替
            config.setJdbcUrl("jdbc:h2:file:./data/linkle;AUTO_SERVER=TRUE");
            config.setDriverClassName("org.h2.Driver");
            config.setUsername("sa");
            config.setPassword("");
        }

        return new HikariDataSource(config);
    }

    /**
     * Linkle DBへの生SQLアクセス用JdbcTemplate
     * （JPA管理外のLinkleテーブルを操作）
     */
    @Bean(name = "linkleJdbcTemplate")
    public JdbcTemplate linkleJdbcTemplate() {
        return new JdbcTemplate(linkleDataSource());
    }

    private String convertToJdbc(String url) {
        if (url.startsWith("jdbc:")) return url;
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
