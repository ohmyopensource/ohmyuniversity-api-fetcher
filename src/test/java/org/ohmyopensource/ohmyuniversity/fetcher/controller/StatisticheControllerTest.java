package org.ohmyopensource.ohmyuniversity.fetcher.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.ohmyopensource.ohmyuniversity.fetcher.controller.v1.StatisticheController;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiAteneo;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiClasse;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaIscritti;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaLaureati;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.TassoCompletamentoResponse;
import org.ohmyopensource.ohmyuniversity.fetcher.service.StatisticheService;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaIscrittiRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaLaureatiRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaImmatricolatiClasseRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaImmatricolatiAteneoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for {@link StatisticheController} using {@link MockMvc}.
 *
 * <p>{@link StatisticheService} is replaced by a Mockito mock. Each nested class covers
 * one endpoint, verifying both the happy path and the {@code 400 Bad Request} guard on
 * missing required filters.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class StatisticheControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private StatisticheService statisticheService;

  @MockitoBean
  private StatisticaIscrittiRepository iscrittiRepository;

  @MockitoBean
  private StatisticaLaureatiRepository laureatiRepository;

  @MockitoBean
  private StatisticaImmatricolatiClasseRepository immatricolatiClasseRepository;

  @MockitoBean
  private StatisticaImmatricolatiAteneoRepository immatricolatiAteneoRepository;

  private StatisticaIscritti sampleIscritti;
  private StatisticaLaureati sampleLaureati;
  private StatisticaImmatricolatiClasse sampleImmatricolatiClasse;
  private StatisticaImmatricolatiAteneo sampleImmatricolatiAteneo;

  /**
   * Initialises sample entities reused across all test cases.
   */
  @BeforeEach
  void setUp() {
    sampleIscritti = new StatisticaIscritti();
    sampleIscritti.setAnno("2023/2024");
    sampleIscritti.setCodiceAteneo("101");
    sampleIscritti.setNomeAteneo("Università di Torino");
    sampleIscritti.setClasseNumero("L-31");
    sampleIscritti.setCorsoNome("Informatica");
    sampleIscritti.setTotM(100);
    sampleIscritti.setTotF(50);
    sampleIscritti.setTot(150);

    sampleLaureati = new StatisticaLaureati();
    sampleLaureati.setAnnoLaurea(2023);
    sampleLaureati.setCodiceAteneo("101");
    sampleLaureati.setNomeAteneo("Università di Torino");
    sampleLaureati.setClasseNumero("L-31");
    sampleLaureati.setCorsoNome("Informatica");
    sampleLaureati.setTotM(80);
    sampleLaureati.setTotF(40);
    sampleLaureati.setTot(120);

    sampleImmatricolatiClasse = new StatisticaImmatricolatiClasse();
    sampleImmatricolatiClasse.setAnno("2023/2024");
    sampleImmatricolatiClasse.setClasseNumero("L-31");
    sampleImmatricolatiClasse.setClasseNome("Scienze e tecnologie informatiche");
    sampleImmatricolatiClasse.setTotM(500);
    sampleImmatricolatiClasse.setTotF(200);
    sampleImmatricolatiClasse.setTot(700);

    sampleImmatricolatiAteneo = new StatisticaImmatricolatiAteneo();
    sampleImmatricolatiAteneo.setAnno("2023/2024");
    sampleImmatricolatiAteneo.setCodiceAteneo("101");
    sampleImmatricolatiAteneo.setNomeAteneo("Università di Torino");
    sampleImmatricolatiAteneo.setTotM(3000);
    sampleImmatricolatiAteneo.setTotF(2000);
    sampleImmatricolatiAteneo.setTot(5000);
  }

  /**
   * Verifies the HTTP contract of {@code GET /api/statistiche/iscritti}.
   */
  @Nested
  @DisplayName("GET /api/statistiche/iscritti")
  class GetIscritti {

    /**
     * Verifies that a request without any filter produces a {@code 400 Bad Request}.
     */
    @Test
    @DisplayName("returns 400 when no filter provided")
    void returns400WhenNoFilter() throws Exception {
      mockMvc.perform(get("/api/statistiche/iscritti"))
          .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that a request with {@code classe} produces a {@code 200 OK} response.
     */
    @Test
    @DisplayName("returns 200 filtered by classe")
    void returns200ByClasse() throws Exception {
      when(statisticheService.findIscrittiPerClasse("L-31"))
          .thenReturn(List.of(sampleIscritti));

      mockMvc.perform(get("/api/statistiche/iscritti").param("classe", "L-31"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].classeNumero").value("L-31"))
          .andExpect(jsonPath("$[0].tot").value(150));
    }

    /**
     * Verifies that a request with {@code ateneo} produces a {@code 200 OK} response.
     */
    @Test
    @DisplayName("returns 200 filtered by ateneo")
    void returns200ByAteneo() throws Exception {
      when(statisticheService.findIscrittiPerAteneo("101"))
          .thenReturn(List.of(sampleIscritti));

      mockMvc.perform(get("/api/statistiche/iscritti").param("ateneo", "101"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].codiceAteneo").value("101"));
    }

    /**
     * Verifies that a request with {@code corso} produces a {@code 200 OK} response.
     */
    @Test
    @DisplayName("returns 200 filtered by corso")
    void returns200ByCorso() throws Exception {
      when(statisticheService.searchIscrittiPerCorso("Informatica"))
          .thenReturn(List.of(sampleIscritti));

      mockMvc.perform(get("/api/statistiche/iscritti").param("corso", "Informatica"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].corsoNome").value("Informatica"));
    }
  }

  /**
   * Verifies the HTTP contract of {@code GET /api/statistiche/laureati}.
   */
  @Nested
  @DisplayName("GET /api/statistiche/laureati")
  class GetLaureati {

    /**
     * Verifies that a request without any filter produces a {@code 400 Bad Request}.
     */
    @Test
    @DisplayName("returns 400 when no filter provided")
    void returns400WhenNoFilter() throws Exception {
      mockMvc.perform(get("/api/statistiche/laureati"))
          .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that a request with {@code classe} produces a {@code 200 OK} response.
     */
    @Test
    @DisplayName("returns 200 filtered by classe")
    void returns200ByClasse() throws Exception {
      when(statisticheService.findLaureatiPerClasse("L-31"))
          .thenReturn(List.of(sampleLaureati));

      mockMvc.perform(get("/api/statistiche/laureati").param("classe", "L-31"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].classeNumero").value("L-31"))
          .andExpect(jsonPath("$[0].tot").value(120));
    }

    /**
     * Verifies that a request with {@code ateneo} produces a {@code 200 OK} response.
     */
    @Test
    @DisplayName("returns 200 filtered by ateneo")
    void returns200ByAteneo() throws Exception {
      when(statisticheService.findLaureatiPerAteneo("101"))
          .thenReturn(List.of(sampleLaureati));

      mockMvc.perform(get("/api/statistiche/laureati").param("ateneo", "101"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].codiceAteneo").value("101"));
    }
  }

  /**
   * Verifies the HTTP contract of {@code GET /api/statistiche/immatricolati/classe}.
   */
  @Nested
  @DisplayName("GET /api/statistiche/immatricolati/classe")
  class GetImmatricolatiClasse {

    /**
     * Verifies that a request without any filter produces a {@code 400 Bad Request}.
     */
    @Test
    @DisplayName("returns 400 when no filter provided")
    void returns400WhenNoFilter() throws Exception {
      mockMvc.perform(get("/api/statistiche/immatricolati/classe"))
          .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that a request with {@code classe} produces a {@code 200 OK} response.
     */
    @Test
    @DisplayName("returns 200 filtered by classe")
    void returns200ByClasse() throws Exception {
      when(statisticheService.findImmatricolatiPerClasse("L-31"))
          .thenReturn(List.of(sampleImmatricolatiClasse));

      mockMvc.perform(get("/api/statistiche/immatricolati/classe").param("classe", "L-31"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].classeNumero").value("L-31"))
          .andExpect(jsonPath("$[0].tot").value(700));
    }
  }

  /**
   * Verifies the HTTP contract of {@code GET /api/statistiche/immatricolati/ateneo}.
   */
  @Nested
  @DisplayName("GET /api/statistiche/immatricolati/ateneo")
  class GetImmatricolatiAteneo {

    /**
     * Verifies that a request without any filter produces a {@code 400 Bad Request}.
     */
    @Test
    @DisplayName("returns 400 when no filter provided")
    void returns400WhenNoFilter() throws Exception {
      mockMvc.perform(get("/api/statistiche/immatricolati/ateneo"))
          .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that a request with {@code ateneo} produces a {@code 200 OK} response.
     */
    @Test
    @DisplayName("returns 200 filtered by ateneo")
    void returns200ByAteneo() throws Exception {
      when(statisticheService.findImmatricolatiPerAteneo("101"))
          .thenReturn(List.of(sampleImmatricolatiAteneo));

      mockMvc.perform(get("/api/statistiche/immatricolati/ateneo").param("ateneo", "101"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].codiceAteneo").value("101"))
          .andExpect(jsonPath("$[0].tot").value(5000));
    }
  }

  /**
   * Verifies the HTTP contract of {@code GET /api/statistiche/tasso-completamento}.
   */
  @Nested
  @DisplayName("GET /api/statistiche/tasso-completamento")
  class GetTassoCompletamento {

    /**
     * Verifies that a valid request produces a {@code 200 OK} response with the
     * expected completion rate fields.
     */
    @Test
    @DisplayName("returns 200 with completion rate data")
    void returns200() throws Exception {
      TassoCompletamentoResponse r = new TassoCompletamentoResponse();
      r.setCodiceAteneo("101");
      r.setNomeAteneo("Università di Torino");
      r.setClasseNumero("L-31");
      r.setCorsoNome("Informatica");
      r.setAnnoIscrizione("2019/2020");
      r.setAnnoLaureaRiferimento(2022);
      r.setIscrittiAnnoBase(150);
      r.setLaureatiAnnoRiferimento(120);
      r.setTassoCompletamentoPct(80.0);

      when(statisticheService.calcolaTassoCompletamento(anyString(), anyString()))
          .thenReturn(List.of(r));

      mockMvc.perform(get("/api/statistiche/tasso-completamento")
              .param("classe", "L-31")
              .param("annoIscrizione", "2019/2020"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].codiceAteneo").value("101"))
          .andExpect(jsonPath("$[0].tassoCompletamentoPct").value(80.0));
    }

    /**
     * Verifies that when the service throws {@link IllegalArgumentException},
     * the controller produces a {@code 400 Bad Request} response.
     */
    @Test
    @DisplayName("returns 400 when service throws IllegalArgumentException")
    void returns400OnInvalidYear() throws Exception {
      when(statisticheService.calcolaTassoCompletamento(any(), any()))
          .thenThrow(new IllegalArgumentException("Invalid year format"));

      mockMvc.perform(get("/api/statistiche/tasso-completamento")
              .param("classe", "L-31")
              .param("annoIscrizione", "invalid"))
          .andExpect(status().isBadRequest());
    }
  }
}