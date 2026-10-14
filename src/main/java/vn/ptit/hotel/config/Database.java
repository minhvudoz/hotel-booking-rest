package vn.ptit.hotel.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;

public final class Database {
    private static volatile HikariDataSource dataSource;

    private Database() {
    }

    public static Connection connection() throws SQLException {
        if (dataSource == null) {
            synchronized (Database.class) {
                if (dataSource == null) {
                    dataSource = new HikariDataSource(config());
                }
            }
        }
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static HikariConfig config() {
        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("org.postgresql.Driver");
        cfg.setMaximumPoolSize(5);
        String renderUrl = System.getenv("DATABASE_URL");
        if (renderUrl != null && !renderUrl.isBlank()) {
            // Dạng postgresql://user:pass@host[:port]/db[?sslmode=require...] (Render, Neon); giữ lại query vì Neon bắt buộc SSL
            URI uri = URI.create(renderUrl);
            String[] credentials = uri.getUserInfo().split(":", 2);
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
            cfg.setJdbcUrl("jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + query);
            cfg.setUsername(credentials[0]);
            cfg.setPassword(credentials.length > 1 ? credentials[1] : "");
        } else {
            cfg.setJdbcUrl(Env.require("DB_URL"));
            cfg.setUsername(Env.require("DB_USER"));
            cfg.setPassword(Env.require("DB_PASSWORD"));
        }
        return cfg;
    }
}
