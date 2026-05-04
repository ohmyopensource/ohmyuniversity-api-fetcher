package org.ohmyopensource.ohmyuniversity.fetcher.infrastructure;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ohmyopensource.ohmyuniversity.fetcher.TestcontainersConfiguration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@DisplayName("Kafka connection")
class KafkaConnectionTest {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Test
  @DisplayName("should autowire KafkaTemplate correctly")
  void shouldAutowireKafkaTemplate() {
    assertNotNull(kafkaTemplate);
  }

  @Test
  @DisplayName("should send a message to a test topic without throwing")
  void shouldSendMessageToKafka() {
    assertDoesNotThrow(() ->
        kafkaTemplate.send("fetcher-test-topic", "ping").get()
    );
  }
}