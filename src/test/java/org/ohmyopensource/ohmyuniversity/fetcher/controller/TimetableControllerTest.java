package org.ohmyopensource.ohmyuniversity.fetcher.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.ohmyopensource.ohmyuniversity.fetcher.TestcontainersConfiguration;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.TimetableResponse;
import org.ohmyopensource.ohmyuniversity.fetcher.service.TimetableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for
 * {@link org.ohmyopensource.ohmyuniversity.fetcher.controller.v1.TimetableController} using
 * {@link MockMvc}.
 *
 * <p>{@link TimetableService} is replaced by a Mockito mock injected via
 * {@link MockitoBean}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class TimetableControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TimetableService timetableService;

  private TimetableResponse sampleResponse;

  /**
   * Initialises a sample {@link TimetableResponse} reused across all test cases.
   */
  @BeforeEach
  void setUp() {
    sampleResponse = new TimetableResponse();
    sampleResponse.setId("uuid-1");
    sampleResponse.setUniversityId("UNIMOL");
    sampleResponse.setDepartmentId("bioscienze-e-territorio");
    sampleResponse.setDepartmentName("Bioscienze e Territorio");
    sampleResponse.setDegreeType("triennali");
    sampleResponse.setTimetablePageUrl(
        "https://www3.unimol.it/dipartimenti/bioscienze-e-territorio/corso/informatica_lezioni");
    sampleResponse.setPdfUrl(
        "https://apistrapi.unimol.it/uploads/411_Isem_2025_26_Ianno_Termoli_fcc28dd1b2.pdf");
    sampleResponse.setLabel("PRIMO ANNO CV \"SOFTWARE TECHNOLOGIES\" SEDE DI TERMOLI");
    sampleResponse.setActive(true);
    sampleResponse.setFetchedAt("2026-06-14T18:12:01Z");
  }

  /**
   * Verifies the HTTP contract of {@code GET /api/v1/fetcher/timetables}.
   */
  @Nested
  @DisplayName("GET /api/v1/fetcher/timetables")
  class GetTimetables {

    /**
     * Verifies that a request with only {@code universityId} returns all active links
     * and produces a {@code 200 OK} response with the expected JSON fields.
     */
    @Test
    @DisplayName("returns 200 with all timetables when no filter provided")
    void returns200WithAllTimetables() throws Exception {
      when(timetableService.getTimetables("UNIMOL")).thenReturn(List.of(sampleResponse));

      mockMvc.perform(get("/api/v1/fetcher/timetables")
              .param("universityId", "UNIMOL"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].universityId").value("UNIMOL"))
          .andExpect(jsonPath("$[0].departmentId").value("bioscienze-e-territorio"))
          .andExpect(jsonPath("$[0].degreeType").value("triennali"))
          .andExpect(jsonPath("$[0].active").value(true));
    }

    /**
     * Verifies that a request with {@code departmentId} delegates to
     * {@link TimetableService#getTimetablesByDepartment}.
     */
    @Test
    @DisplayName("returns 200 filtered by departmentId")
    void returns200FilteredByDepartment() throws Exception {
      when(timetableService.getTimetablesByDepartment(
          eq("UNIMOL"), eq("bioscienze-e-territorio")))
          .thenReturn(List.of(sampleResponse));

      mockMvc.perform(get("/api/v1/fetcher/timetables")
              .param("universityId", "UNIMOL")
              .param("departmentId", "bioscienze-e-territorio"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].departmentId").value("bioscienze-e-territorio"));
    }

    /**
     * Verifies that a request with {@code degreeType} delegates to
     * {@link TimetableService#getTimetablesByDegreeType}.
     */
    @Test
    @DisplayName("returns 200 filtered by degreeType")
    void returns200FilteredByDegreeType() throws Exception {
      when(timetableService.getTimetablesByDegreeType(
          eq("UNIMOL"), eq("triennali")))
          .thenReturn(List.of(sampleResponse));

      mockMvc.perform(get("/api/v1/fetcher/timetables")
              .param("universityId", "UNIMOL")
              .param("degreeType", "triennali"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].degreeType").value("triennali"));
    }

    /**
     * Verifies that {@code departmentId} takes precedence over {@code degreeType}
     * when both are provided.
     */
    @Test
    @DisplayName("departmentId takes precedence over degreeType when both provided")
    void departmentIdTakesPrecedence() throws Exception {
      when(timetableService.getTimetablesByDepartment(any(), any()))
          .thenReturn(List.of(sampleResponse));

      mockMvc.perform(get("/api/v1/fetcher/timetables")
              .param("universityId", "UNIMOL")
              .param("departmentId", "bioscienze-e-territorio")
              .param("degreeType", "triennali"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].departmentId").value("bioscienze-e-territorio"));
    }

    /**
     * Verifies that an empty list produces a {@code 200 OK} with an empty JSON array.
     */
    @Test
    @DisplayName("returns 200 with empty list when no results found")
    void returns200WhenEmpty() throws Exception {
      when(timetableService.getTimetables("UNIMOL")).thenReturn(List.of());

      mockMvc.perform(get("/api/v1/fetcher/timetables")
              .param("universityId", "UNIMOL"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());
    }
  }
}