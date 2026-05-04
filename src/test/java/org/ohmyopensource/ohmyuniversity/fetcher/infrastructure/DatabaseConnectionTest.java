package org.ohmyopensource.ohmyuniversity.fetcher.infrastructure;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.ohmyopensource.ohmyuniversity.fetcher.TestcontainersConfiguration;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;



@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@DisplayName("PostgreSQL connection")
class DatabaseConnectionTest {

  @Autowired
  private DataSource dataSource;

  @Test
  @DisplayName("should connect to PostgreSQL and return a valid connection")
  void shouldConnectToPostgres() throws Exception {
    assertNotNull(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      assertTrue(connection.isValid(2));
    }
  }

  @Test
  @DisplayName("should return correct database product name")
  void shouldBePostgresDatabase() throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      String productName = connection.getMetaData().getDatabaseProductName();
      assertTrue(
          productName.toLowerCase().contains("postgresql"),
          "Expected PostgreSQL but got: " + productName
      );
    }
  }
}