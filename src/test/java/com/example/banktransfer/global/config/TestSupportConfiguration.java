package com.example.banktransfer.global.config;

import com.example.banktransfer.global.progress.InMemoryProgressRecorder;
import com.example.banktransfer.global.progress.ProgressRecorder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@TestConfiguration
public class TestSupportConfiguration {
    @Bean
    public DatabaseCleaner databaseCleaner(JdbcTemplate jdbcTemplate) {
        return new DatabaseCleaner(jdbcTemplate);
    }

    @Bean
    @Primary
    public ProgressRecorder progressRecorder() {
        return new InMemoryProgressRecorder();
    }
}
