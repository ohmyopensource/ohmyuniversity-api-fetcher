package org.ohmyopensource.ohmyuniversity.fetcher.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeastOnce;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.TimetableLink;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.TimetableLinkRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.TimetableResponse;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

/**
 * Unit tests for {@link TimetableService}.
 *
 * <p>All repositories are replaced by Mockito mocks so that no database or Spring context is
 * required. Each test group covers a single public method of the service, verifying both the
 * happy path and the relevant exception paths.
 */
@ExtendWith(MockitoExtension.class)
class TimetableServiceTest {

  @Mock
  private TimetableLinkRepository repository;

  @InjectMocks
  private TimetableService timetableService;

  private TimetableLink sampleLink;

  /**
   * Initialises a sample {@link TimetableLink} entity and injects a real {@link JsonMapper}
   * into the service before each test.
   */
  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(timetableService, "objectMapper", JsonMapper.builder().build());

    sampleLink = new TimetableLink();
    ReflectionTestUtils.setField(sampleLink, "id", UUID.randomUUID());
    sampleLink.setUniversityId("UNIMOL");
    sampleLink.setDepartmentId("bioscienze-e-territorio");
    sampleLink.setDepartmentName("Bioscienze e Territorio");
    sampleLink.setDegreeType("triennali");
    sampleLink.setTimetablePageUrl(
        "https://www3.unimol.it/dipartimenti/bioscienze-e-territorio/corso/informatica_lezioni");
    sampleLink.setPdfUrl(
        "https://apistrapi.unimol.it/uploads/411_Isem_2025_26_Ianno_Termoli_fcc28dd1b2.pdf");
    sampleLink.setLabel("PRIMO ANNO CV \"SOFTWARE TECHNOLOGIES\" SEDE DI TERMOLI");
    sampleLink.setActive(true);
    sampleLink.setLastChecked(Instant.now());
    sampleLink.setFetchedAt(Instant.now());
    sampleLink.setCreatedAt(Instant.now());
    sampleLink.setUpdatedAt(Instant.now());
  }

  /**
   * Verifies {@link TimetableService#getTimetables} covering successful retrieval and
   * empty result.
   */
  @Nested
  @DisplayName("getTimetables")
  class GetTimetables {

    /**
     * Verifies that {@link TimetableService#getTimetables} delegates to the repository and
     * maps each entity to a {@link TimetableResponse} correctly.
     */
    @Test
    @DisplayName("returns mapped responses for active links ordered by department")
    void returnsMappedResponses() {
      when(repository.findByUniversityIdAndIsActiveTrueOrderByDepartmentIdAsc("UNIMOL"))
          .thenReturn(List.of(sampleLink));

      List<TimetableResponse> result = timetableService.getTimetables("UNIMOL");

      assertThat(result).hasSize(1);
      assertThat(result.get(0).getUniversityId()).isEqualTo("UNIMOL");
      assertThat(result.get(0).getDepartmentId()).isEqualTo("bioscienze-e-territorio");
      assertThat(result.get(0).getDegreeType()).isEqualTo("triennali");
      assertThat(result.get(0).isActive()).isTrue();
    }

    /**
     * Verifies that an empty repository result produces an empty list without throwing.
     */
    @Test
    @DisplayName("returns empty list when no active links found")
    void returnsEmptyList() {
      when(repository.findByUniversityIdAndIsActiveTrueOrderByDepartmentIdAsc("UNIMOL"))
          .thenReturn(List.of());

      List<TimetableResponse> result = timetableService.getTimetables("UNIMOL");

      assertThat(result).isEmpty();
    }
  }

  /**
   * Verifies {@link TimetableService#getTimetablesByDepartment} covering successful retrieval.
   */
  @Nested
  @DisplayName("getTimetablesByDepartment")
  class GetTimetablesByDepartment {

    /**
     * Verifies that {@link TimetableService#getTimetablesByDepartment} delegates to the
     * repository with the correct university and department identifiers.
     */
    @Test
    @DisplayName("returns links filtered by department")
    void returnsFilteredByDepartment() {
      when(repository.findByUniversityIdAndDepartmentIdAndIsActiveTrue(
          "UNIMOL", "bioscienze-e-territorio"))
          .thenReturn(List.of(sampleLink));

      List<TimetableResponse> result =
          timetableService.getTimetablesByDepartment("UNIMOL", "bioscienze-e-territorio");

      assertThat(result).hasSize(1);
      assertThat(result.get(0).getDepartmentId()).isEqualTo("bioscienze-e-territorio");
    }
  }

  /**
   * Verifies {@link TimetableService#getTimetablesByDegreeType} covering successful retrieval.
   */
  @Nested
  @DisplayName("getTimetablesByDegreeType")
  class GetTimetablesByDegreeType {

    /**
     * Verifies that {@link TimetableService#getTimetablesByDegreeType} delegates to the
     * repository with the correct university and degree type identifiers.
     */
    @Test
    @DisplayName("returns links filtered by degree type")
    void returnsFilteredByDegreeType() {
      when(repository.findByUniversityIdAndDegreeTypeAndIsActiveTrue("UNIMOL", "triennali"))
          .thenReturn(List.of(sampleLink));

      List<TimetableResponse> result =
          timetableService.getTimetablesByDegreeType("UNIMOL", "triennali");

      assertThat(result).hasSize(1);
      assertThat(result.get(0).getDegreeType()).isEqualTo("triennali");
    }
  }

  /**
   * Verifies {@link TimetableService#importTimetables} covering successful import,
   * resource not found, and upsert behaviour.
   */
  @Nested
  @DisplayName("importTimetables")
  class ImportTimetables {

    /**
     * Verifies that {@link TimetableService#importTimetables} throws
     * {@link IllegalArgumentException} when no JSON resource file exists for the given
     * university identifier.
     */
    @Test
    @DisplayName("throws IllegalArgumentException when resource file not found")
    void throwsWhenResourceNotFound() {
      assertThatThrownBy(() -> timetableService.importTimetables("NONEXISTENT"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("nonexistent_timetables.json");
    }

    /**
     * Verifies that {@link TimetableService#importTimetables} inserts a new row when no
     * existing entry matches the upsert key.
     */
    @Test
    @DisplayName("inserts new row when no existing entry found")
    void insertsWhenNotExists() {
      when(repository.findByUniversityIdAndTimetablePageUrlAndPdfUrl(
          any(), any(), any())).thenReturn(Optional.empty());
      when(repository.save(any())).thenReturn(sampleLink);

      timetableService.importTimetables("UNIMOL");

      verify(repository, atLeastOnce()).save(any());
    }

    /**
     * Verifies that {@link TimetableService#importTimetables} updates an existing row when
     * one already matches the upsert key.
     */
    @Test
    @DisplayName("updates existing row when entry already exists")
    void updatesWhenExists() {
      when(repository.findByUniversityIdAndTimetablePageUrlAndPdfUrl(
          any(), any(), any())).thenReturn(Optional.of(sampleLink));
      when(repository.save(any())).thenReturn(sampleLink);

      timetableService.importTimetables("UNIMOL");

      verify(repository, atLeastOnce()).save(sampleLink);
    }
  }
}