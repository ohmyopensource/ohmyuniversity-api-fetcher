package org.ohmyopensource.ohmyuniversity.fetcher.config;

import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.immatricolati.ImmatricolatiJobConfig;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.iscritti.IscrittixCorsoJobConfig;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.laureati.LaureatiPerCorsoJobConfig;
import org.ohmyopensource.ohmyuniversity.fetcher.job.ordini.OrdiniJobConfig;
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
 * Scheduler for all batch jobs.
 *
 * <p>All cron expressions are read from {@code application.yaml} via {@code @Scheduled}
 * so they can be overridden per environment without code changes.
 * In tests, all crons are set to the impossible date "Feb 31" to prevent any firing.
 *
 * <p>Production schedule:
 * <ul>
 *   <li>ordiniJob — February 1st and August 1st at 01:00 (semi-annual data)</li>
 *   <li>iscrittixCorsoJob — October 1st at 02:00 (MUR publishes in September)</li>
 *   <li>immatricolatiJob — October 15th at 02:00 (follows iscritti publication)</li>
 *   <li>laureatiPerCorsoJob — November 1st at 03:00 (MUR publishes in October)</li>
 * </ul>
 */
@Component
public class SchedulerConfig {

  private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);

  private final JobOperator jobOperator;
  private final Job ordiniJob;
  private final Job iscrittixCorsoJob;
  private final Job immatricolatiJob;
  private final Job laureatiPerCorsoJob;

  public SchedulerConfig(
      JobOperator jobOperator,
      @Qualifier(OrdiniJobConfig.JOB_NAME) Job ordiniJob,
      @Qualifier(IscrittixCorsoJobConfig.JOB_NAME) Job iscrittixCorsoJob,
      @Qualifier(ImmatricolatiJobConfig.JOB_NAME) Job immatricolatiJob,
      @Qualifier(LaureatiPerCorsoJobConfig.JOB_NAME) Job laureatiPerCorsoJob) {
    this.jobOperator = jobOperator;
    this.ordiniJob = ordiniJob;
    this.iscrittixCorsoJob = iscrittixCorsoJob;
    this.immatricolatiJob = immatricolatiJob;
    this.laureatiPerCorsoJob = laureatiPerCorsoJob;
  }

  @Scheduled(cron = "${fetcher.schedule.ordini}")
  public void runOrdiniJob() {
    runJob(ordiniJob, OrdiniJobConfig.JOB_NAME);
  }

  @Scheduled(cron = "${fetcher.schedule.iscritti}")
  public void runIscrittixCorsoJob() {
    runJob(iscrittixCorsoJob, IscrittixCorsoJobConfig.JOB_NAME);
  }

  @Scheduled(cron = "${fetcher.schedule.immatricolati}")
  public void runImmatricolatiJob() {
    runJob(immatricolatiJob, ImmatricolatiJobConfig.JOB_NAME);
  }

  @Scheduled(cron = "${fetcher.schedule.laureati}")
  public void runLaureatiPerCorsoJob() {
    runJob(laureatiPerCorsoJob, LaureatiPerCorsoJobConfig.JOB_NAME);
  }

  private void runJob(Job job, String jobName) {
    try {
      JobParameters params = new JobParametersBuilder()
          .addLong("run.at", System.currentTimeMillis())
          .toJobParameters();
      jobOperator.start(job, params);
      log.info("SchedulerConfig: job '{}' started by scheduler", jobName);
    } catch (Exception e) {
      log.error("SchedulerConfig: failed to start job '{}'", jobName, e);
    }
  }
}