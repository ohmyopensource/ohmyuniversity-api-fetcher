package org.ohmyopensource.ohmyuniversity.fetcher.job.timetable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled trigger for the timetable PDF import job.
 *
 * <p>Launches {@code importTimetablesJob} according to the cron expression
 * configured in {@code fetcher.schedule.timetables}. The job reads the cleaned
 * timetable JSON files from the classpath and upserts all PDF links into the
 * database.
 *
 * <p>Each execution uses a unique {@code runId} parameter so that Spring Batch
 * treats consecutive runs as distinct job instances rather than re-runs of a
 * completed one.
 *
 * <p>In the test profile, the cron expression is set to {@code 0 0 0 31 2 *}
 * (February 31, which never occurs) to prevent the job from firing during tests.
 */
@Component
public class TimetableScheduler {

  private static final Logger log = LoggerFactory.getLogger(TimetableScheduler.class);

  private final Job importTimetablesJob;
  private final JobOperator jobOperator;

  // ============ Constructor ============

  /**
   * Creates the scheduler with the required Spring Batch dependencies.
   *
   * @param jobOperator         Spring Batch job operator used to start job executions
   * @param importTimetablesJob the timetable import job bean
   */
  public TimetableScheduler(
      JobOperator jobOperator,
      @Qualifier("importTimetablesJob") Job importTimetablesJob) {
    this.jobOperator = jobOperator;
    this.importTimetablesJob = importTimetablesJob;
  }

  // ============ Class Methods ============

  /**
   * Triggers the timetable import job on the configured schedule.
   *
   * <p>The cron expression is read from {@code fetcher.schedule.timetables}.
   * Defaults to {@code 0 30 3 * * *} (daily at 03:30 UTC) — after the nightly
   * maintenance window and before the start of the academic day.
   *
   * <p>Launch failures are logged and swallowed so that a single failed run
   * does not affect subsequent scheduled executions.
   */
  @Scheduled(cron = "${fetcher.schedule.timetables:0 30 3 * * *}")
  public void runImportTimetablesJob() {
    log.info("TimetableScheduler: launching importTimetablesJob");
    try {
      JobParameters params = new JobParametersBuilder()
          .addLong("runId", System.currentTimeMillis())
          .toJobParameters();
      jobOperator.start(importTimetablesJob, params);
    } catch (Exception e) {
      log.error("TimetableScheduler: importTimetablesJob failed — {}", e.getMessage());
    }
  }
}