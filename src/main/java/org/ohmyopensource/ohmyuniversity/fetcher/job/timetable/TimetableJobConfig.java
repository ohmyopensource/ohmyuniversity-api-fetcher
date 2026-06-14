package org.ohmyopensource.ohmyuniversity.fetcher.job.timetable;

import org.ohmyopensource.ohmyuniversity.fetcher.service.TimetableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch job configuration for importing university timetable PDF links.
 *
 * <p>The job reads the cleaned timetable JSON resource file for each configured
 * university and upserts the discovered PDF links into the {@code timetable_link}
 * table via {@link TimetableService#importTimetables(String)}.
 *
 * <p>The job is triggered by {@link TimetableScheduler} according to the cron
 * expression defined in {@code fetcher.schedule.timetables} — by default nightly
 * at 03:30, after the semester changeover periods in which UNIMOL typically
 * updates its PDF files.
 */
@Configuration
public class TimetableJobConfig {

  private static final Logger log = LoggerFactory.getLogger(TimetableJobConfig.class);

  /**
   * List of university identifiers for which timetable import is enabled.
   * Each entry must have a corresponding JSON resource file at
   * {@code classpath:universities/<id>/<id>_timetables.json}.
   */
  private static final String[] UNIVERSITY_IDS = {"unimol"};

  private final TimetableService timetableService;

  // ============ Constructor ============

  /**
   * Creates the job configuration with the required service dependency.
   *
   * @param timetableService service that performs the JSON parsing and DB upsert
   */
  public TimetableJobConfig(TimetableService timetableService) {
    this.timetableService = timetableService;
  }

  // ============ Class Methods ============

  /**
   * Defines the {@code importTimetablesJob} Spring Batch job.
   *
   * <p>The job consists of a single tasklet step that iterates over all configured
   * universities and imports their timetable data.
   *
   * @param jobRepository        Spring Batch job repository for execution metadata
   * @param importTimetablesStep the single step composing this job
   * @return the configured {@link Job} bean
   */
  @Bean
  public Job importTimetablesJob(JobRepository jobRepository, Step importTimetablesStep) {
    return new JobBuilder("importTimetablesJob", jobRepository)
        .start(importTimetablesStep)
        .build();
  }

  /**
   * Defines the {@code importTimetablesStep} tasklet step.
   *
   * <p>Iterates over {@link #UNIVERSITY_IDS} and calls
   * {@link TimetableService#importTimetables(String)} for each university.
   * Failures for individual universities are logged and do not abort the step.
   *
   * @param jobRepository      Spring Batch job repository
   * @param transactionManager platform transaction manager
   * @return the configured {@link Step} bean
   */
  @Bean
  public Step importTimetablesStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager) {

    return new StepBuilder("importTimetablesStep", jobRepository)
        .tasklet((contribution, chunkContext) -> {
          log.info("TimetableJobConfig: starting timetable import for {} universities",
              UNIVERSITY_IDS.length);

          for (String universityId : UNIVERSITY_IDS) {
            try {
              timetableService.importTimetables(universityId.toUpperCase());
              log.info("TimetableJobConfig: import complete for university={}", universityId);
            } catch (Exception e) {
              log.error("TimetableJobConfig: import failed for university={} — {}",
                  universityId, e.getMessage());
            }
          }

          return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
  }
}