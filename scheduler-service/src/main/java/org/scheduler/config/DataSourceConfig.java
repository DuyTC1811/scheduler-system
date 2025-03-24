package org.scheduler.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceConfig {
    public static DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        config.addDataSourceProperty("user", "postgres");
        config.addDataSourceProperty("password", "anhduy12");
        config.addDataSourceProperty("databaseName", "postgres");
        config.addDataSourceProperty("portNumber", "5432");
        config.addDataSourceProperty("serverName", "localhost");
        return new HikariDataSource(config);
    }
}
