package com.example.banktransfer.global.config;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class DatabaseCleaner {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clean() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                String.class
        );
        for (String table : tables) {
            jdbcTemplate.execute("TRUNCATE TABLE `" + table + "`");
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}
