package com.example.banktransfer.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainerConfiguration {
    static {
        // ✅ Docker API 버전 강제 설정
        System.setProperty("DOCKER_API_VERSION", "1.41");
    }

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("bank_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);  // 컨테이너 재사용
    }
}
