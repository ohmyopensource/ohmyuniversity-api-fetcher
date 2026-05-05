package org.ohmyopensource.ohmyuniversity.fetcher.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * Loads environment variables from .env file into Spring's property sources.
 * Runs before the application context is initialized so that ${VAR} placeholders
 * in application.yml are resolved correctly.
 */
public class DotenvLoader implements EnvironmentPostProcessor {

  private static final String DOTENV_PROPERTY_SOURCE_NAME = "dotenvProperties";

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment,
      SpringApplication application) {

    try {
      Dotenv dotenv = Dotenv.configure()
          .directory(System.getProperty("user.dir"))
          .ignoreIfMissing()
          .ignoreIfMalformed()
          .load();

      Properties props = new Properties();

      String[] keys = {
          "POSTGRES_URL", "POSTGRES_USERNAME", "POSTGRES_PASSWORD",
          "KAFKA_BOOTSTRAP_SERVERS", "INPA_API_KEY", "EPSO_API_KEY",
          "SPRING_PROFILES_ACTIVE"
      };

      for (String key : keys) {
        try {
          String value = dotenv.get(key);
          props.put(key, value);
        } catch (Exception ignored) {
        }
      }

      if (!props.isEmpty()) {
        environment.getPropertySources()
            .addFirst(new PropertiesPropertySource(DOTENV_PROPERTY_SOURCE_NAME, props));
      }

    } catch (Exception e) {
      System.out.println("=== DOTENV ERROR: " + e.getMessage());
    }
  }
}