package org.ohmyopensource.ohmyuniversity.fetcher.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.ohmyopensource.ohmyuniversity.fetcher.TestcontainersConfiguration;
import org.ohmyopensource.ohmyuniversity.fetcher.controller.v1.JobTriggerController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for {@link JobTriggerController} using {@link MockMvc}.
 *
 * <p>Verifies authentication, job resolution and error handling without actually
 * launching any Spring Batch job.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class JobTriggerControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private static final String VALID_SECRET = "test-secret";
  private static final String ADMIN_HEADER = "X-Admin-Secret";

  /**
   * Verifies the authentication guard on {@code POST /api/jobs/{jobName}/run}.
   */
  @Nested
  @DisplayName("Authentication")
  class Authentication {

    /**
     * Verifies that a request without the {@code X-Admin-Secret} header
     * produces a {@code 401 Unauthorized} response.
     */
    @Test
    @DisplayName("returns 401 when header is missing")
    void returns401WhenHeaderMissing() throws Exception {
      mockMvc.perform(post("/api/jobs/ordini/run"))
          .andExpect(status().isUnauthorized());
    }

    /**
     * Verifies that a request with a wrong secret produces a {@code 401 Unauthorized} response.
     */
    @Test
    @DisplayName("returns 401 when secret is wrong")
    void returns401WhenSecretWrong() throws Exception {
      mockMvc.perform(post("/api/jobs/ordini/run")
              .header(ADMIN_HEADER, "wrong-secret"))
          .andExpect(status().isUnauthorized());
    }
  }

  /**
   * Verifies job resolution on {@code POST /api/jobs/{jobName}/run}.
   */
  @Nested
  @DisplayName("Job resolution")
  class JobResolution {

    /**
     * Verifies that a request with an unknown job name produces a {@code 404 Not Found}
     * response.
     */
    @Test
    @DisplayName("returns 404 when job name is unknown")
    void returns404WhenJobUnknown() throws Exception {
      mockMvc.perform(post("/api/jobs/unknown-job/run")
              .header(ADMIN_HEADER, VALID_SECRET))
          .andExpect(status().isNotFound());
    }

    /**
     * Verifies that a request for the {@code ordini} job with the correct secret
     * produces a {@code 202 Accepted} response.
     */
    @Test
    @DisplayName("returns 202 when ordini job triggered with valid secret")
    void returns202ForOrdiniJob() throws Exception {
      mockMvc.perform(post("/api/jobs/ordini/run")
              .header(ADMIN_HEADER, VALID_SECRET))
          .andExpect(status().isAccepted());
    }

    /**
     * Verifies that a request for the {@code timetables} job with the correct secret
     * produces a {@code 202 Accepted} response.
     */
    @Test
    @DisplayName("returns 202 when timetables job triggered with valid secret")
    void returns202ForTimetablesJob() throws Exception {
      mockMvc.perform(post("/api/jobs/timetables/run")
              .header(ADMIN_HEADER, VALID_SECRET))
          .andExpect(status().isAccepted());
    }
  }
}