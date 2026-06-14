package org.ohmyopensource.ohmyuniversity.fetcher.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.ohmyopensource.ohmyuniversity.fetcher.TestcontainersConfiguration;
import org.ohmyopensource.ohmyuniversity.fetcher.controller.v1.OrdineProfessionaleController;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.CategoriaOrdine;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.OrdineProfessionale;
import org.ohmyopensource.ohmyuniversity.fetcher.service.OrdineProfessionaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

/**
 * Unit tests for {@link OrdineProfessionaleController} using {@link MockMvc}.
 *
 * <p>{@link OrdineProfessionaleService} is replaced by a Mockito mock.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class OrdineProfessionaleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private OrdineProfessionaleService service;

  private OrdineProfessionale sampleOrdine;
  private UUID sampleId;

  /**
   * Initialises a sample {@link OrdineProfessionale} entity reused across all test cases.
   */
  @BeforeEach
  void setUp() {
    sampleId = UUID.randomUUID();
    sampleOrdine = new OrdineProfessionale();
    sampleOrdine.setCodice("AVVOCATI");
    sampleOrdine.setNome("Ordine degli Avvocati");
    sampleOrdine.setCategoria(CategoriaOrdine.LEGALE);
    sampleOrdine.setOrganismoNazionale("Consiglio Nazionale Forense");
    sampleOrdine.setRequisitiLaurea(new String[]{"LMG/01"});
  }

  /**
   * Verifies the HTTP contract of {@code GET /api/ordini-professionali}, covering
   * retrieval without filters, with categoria filter and with classeLaurea filter.
   */
  @Nested
  @DisplayName("GET /api/ordini-professionali")
  class GetAll {

    /**
     * Verifies that a request without filters returns all orders via
     * {@link OrdineProfessionaleService#findAll} and produces a {@code 200 OK} response.
     */
    @Test
    @DisplayName("returns 200 with all orders when no filter provided")
    void returns200WithAllOrders() throws Exception {
      when(service.findAll()).thenReturn(List.of(sampleOrdine));

      mockMvc.perform(get("/api/ordini-professionali"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].codice").value("AVVOCATI"))
          .andExpect(jsonPath("$[0].nome").value("Ordine degli Avvocati"))
          .andExpect(jsonPath("$[0].categoria").value("LEGALE"));
    }

    /**
     * Verifies that a request with {@code classeLaurea} delegates to
     * {@link OrdineProfessionaleService#findByClasseLaurea} and produces a
     * {@code 200 OK} response.
     */
    @Test
    @DisplayName("returns 200 filtered by classeLaurea")
    void returns200FilteredByClasseLaurea() throws Exception {
      when(service.findByClasseLaurea("LMG/01")).thenReturn(List.of(sampleOrdine));

      mockMvc.perform(get("/api/ordini-professionali")
              .param("classeLaurea", "LMG/01"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].codice").value("AVVOCATI"));
    }

    /**
     * Verifies that a request with {@code categoria} delegates to
     * {@link OrdineProfessionaleService#findByCategoria} and produces a
     * {@code 200 OK} response.
     */
    @Test
    @DisplayName("returns 200 filtered by categoria")
    void returns200FilteredByCategoria() throws Exception {
      when(service.findByCategoria(CategoriaOrdine.LEGALE)).thenReturn(List.of(sampleOrdine));

      mockMvc.perform(get("/api/ordini-professionali")
              .param("categoria", "LEGALE"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].categoria").value("LEGALE"));
    }

    /**
     * Verifies that {@code classeLaurea} takes precedence over {@code categoria} when both
     * are provided.
     */
    @Test
    @DisplayName("classeLaurea takes precedence over categoria when both provided")
    void classeLaureaTakesPrecedence() throws Exception {
      when(service.findByClasseLaurea("LMG/01")).thenReturn(List.of(sampleOrdine));

      mockMvc.perform(get("/api/ordini-professionali")
              .param("classeLaurea", "LMG/01")
              .param("categoria", "LEGALE"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].codice").value("AVVOCATI"));
    }

    /**
     * Verifies that an empty result produces a {@code 200 OK} with an empty JSON array.
     */
    @Test
    @DisplayName("returns 200 with empty list when no orders found")
    void returns200WhenEmpty() throws Exception {
      when(service.findAll()).thenReturn(List.of());

      mockMvc.perform(get("/api/ordini-professionali"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isEmpty());
    }
  }

  /**
   * Verifies the HTTP contract of {@code GET /api/ordini-professionali/{id}}, covering
   * successful retrieval and not found.
   */
  @Nested
  @DisplayName("GET /api/ordini-professionali/{id}")
  class GetById {

    /**
     * Verifies that a request with a valid UUID produces a {@code 200 OK} response with
     * the expected order fields.
     */
    @Test
    @DisplayName("returns 200 when order found")
    void returns200WhenFound() throws Exception {
      when(service.findById(any())).thenReturn(Optional.of(sampleOrdine));

      mockMvc.perform(get("/api/ordini-professionali/" + sampleId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.codice").value("AVVOCATI"))
          .andExpect(jsonPath("$.nome").value("Ordine degli Avvocati"));
    }

    /**
     * Verifies that a request with an unknown UUID produces a {@code 404 Not Found} response.
     */
    @Test
    @DisplayName("returns 404 when order not found")
    void returns404WhenNotFound() throws Exception {
      when(service.findById(any())).thenReturn(Optional.empty());

      mockMvc.perform(get("/api/ordini-professionali/" + UUID.randomUUID()))
          .andExpect(status().isNotFound());
    }
  }
}