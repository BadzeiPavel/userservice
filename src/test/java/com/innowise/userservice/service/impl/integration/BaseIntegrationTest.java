package com.innowise.userservice.service.impl.integration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ContextConfiguration(initializers = BaseIntegrationTest.Initializer.class)
@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  @Container
  static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
      .withExposedPorts(6379);

  static class Initializer implements
      ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
      createSchema("user_schema");

      System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
      System.setProperty("spring.datasource.username", postgres.getUsername());
      System.setProperty("spring.datasource.password", postgres.getPassword());
      System.setProperty("spring.jpa.properties.hibernate.default_schema", "user_schema");

      System.setProperty("spring.liquibase.url", postgres.getJdbcUrl());
      System.setProperty("spring.liquibase.user", postgres.getUsername());
      System.setProperty("spring.liquibase.password", postgres.getPassword());
      System.setProperty("spring.liquibase.default-schema", "user_schema");

      System.setProperty("spring.data.redis.host", redis.getHost());
      System.setProperty("spring.data.redis.port", String.valueOf(redis.getMappedPort(6379)));

      System.setProperty("spring.datasource.hikari.max-lifetime", "30000");
      System.setProperty("spring.datasource.hikari.idle-timeout", "20000");
    }
  }

  private static void createSchema(String schemaName) {
    try (Connection connection = postgres.createConnection("");
        Statement stmt = connection.createStatement()) {
      stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create schema: " + schemaName, e);
    }
  }
}