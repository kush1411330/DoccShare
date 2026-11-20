package com.doccshare.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url}")
    private String rawUrl;

    @Value("${spring.datasource.username:cloudshare}")
    private String defaultUsername;

    @Value("${spring.datasource.password:cloudshare123}")
    private String defaultPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        String url = rawUrl;
        String username = defaultUsername;
        String password = defaultPassword;

        // Automatically convert cloud provider URLs (postgres:// or postgresql://)
        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            try {
                URI uri = new URI(url);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath() != null ? uri.getPath() : "/cloudshare";

                if (uri.getUserInfo() != null) {
                    String[] userParts = uri.getUserInfo().split(":", 2);
                    username = userParts[0];
                    if (userParts.length > 1) {
                        password = userParts[1];
                    }
                }

                url = String.format("jdbc:postgresql://%s:%d%s?sslmode=prefer", host, port, path);
                log.info("Successfully converted cloud PostgreSQL connection URL for host: {}", host);
            } catch (Exception e) {
                log.warn("Failed to parse PostgreSQL URI: {}", e.getMessage());
            }
        } else if (url != null && url.startsWith("jdbc:postgresql://") && url.contains("@")) {
            // Handle jdbc:postgresql://user:password@host:port/database format
            try {
                String clean = url.substring("jdbc:".length());
                URI uri = new URI(clean);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath() != null ? uri.getPath() : "/cloudshare";

                if (uri.getUserInfo() != null) {
                    String[] userParts = uri.getUserInfo().split(":", 2);
                    username = userParts[0];
                    if (userParts.length > 1) {
                        password = userParts[1];
                    }
                }

                url = String.format("jdbc:postgresql://%s:%d%s?sslmode=prefer", host, port, path);
                log.info("Parsed credentials from JDBC URL for host: {}", host);
            } catch (Exception e) {
                log.warn("Failed to parse JDBC URI with userinfo: {}", e.getMessage());
            }
        }

        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        return new HikariDataSource(config);
    }
}
