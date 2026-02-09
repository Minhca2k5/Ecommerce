package com.minzetsu.ecommerce.integration;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class InfrastructureContainersIntegrationTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("ecommerce_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7.2-alpine")
            .withExposedPorts(6379);

    @Test
    void mysqlContainer_shouldAcceptSqlConnections() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                mysql.getJdbcUrl(),
                mysql.getUsername(),
                mysql.getPassword());
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT 1")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(1);
        }
    }

    @Test
    void redisContainer_shouldStoreAndReadValues() {
        String address = "redis://" + redis.getHost() + ":" + redis.getMappedPort(6379);
        RedisClient client = RedisClient.create(address);

        try (StatefulRedisConnection<String, String> connection = client.connect()) {
            var commands = connection.sync();
            commands.set("integration:key", "ok");
            assertThat(commands.get("integration:key")).isEqualTo("ok");
        } finally {
            client.shutdown();
        }
    }
}
