package com.example.banktransfer.global.config;

import com.example.banktransfer.global.annotation.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@IntegrationTest
class TestcontainerConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void 연결정보_확인() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            System.out.println("===== Testcontainers 연결 정보 =====");
            System.out.println("JDBC URL: " + metaData.getURL());
            // 출력: jdbc:mysql://localhost:32768/bank_test

            System.out.println("Username: " + metaData.getUserName());
            // 출력: test@localhost

            System.out.println("Product: " + metaData.getDatabaseProductName());
            // 출력: MySQL

            System.out.println("Version: " + metaData.getDatabaseProductVersion());
            // 출력: 8.0.xx
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
