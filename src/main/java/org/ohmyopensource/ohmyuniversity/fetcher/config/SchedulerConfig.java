package org.ohmyopensource.ohmyuniversity.fetcher.config;

import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.immatricolati.ImmatricolatiJobConfig;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.iscritti.IscrittixCorsoJobConfig;
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
 * Scheduler that triggers Spring Batch jobs on cron expressions
 * defined in application-dev.yaml / application-prod.yaml.
 *
 * Each job run gets a unique timestamp parameter so Spring Batch
 * does not skip it as a duplicate of a previous successful run.
 *
 * Scheduling is disabled in tests via application-test.yaml:
 * {@code spring.task.scheduling.pool.size: 0}
 */
@Component
public class SchedulerConfig {

  private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);

  private final JobOperator jobOperator;
  private final Job ordiniJob;
  private final Job iscrittixCorsoJob;
  private final Job immatricolatiJob;

  public SchedulerConfig(
      JobOperator jobOperator,
      @Qualifier(OrdiniJobConfig.JOB_NAME) Job ordiniJob,
      @Qualifier(IscrittixCorsoJobConfig.JOB_NAME) Job iscrittixCorsoJob,
      @Qualifier(ImmatricolatiJobConfig.JOB_NAME) Job immatricolatiJob) {
    this.jobOperator = jobOperator;
    this.ordiniJob = ordiniJob;
    this.iscrittixCorsoJob = iscrittixCorsoJob;
    this.immatricolatiJob = immatricolatiJob;
  }

  /** Ordini professionali — 1 feb e 1 ago alle 01:00. */
  @Scheduled(cron = "${fetcher.schedule.ordini}")
  public void runOrdiniJob() {
    runJob(ordiniJob, OrdiniJobConfig.JOB_NAME);
  }

  /** Iscritti per corso — 1 ottobre alle 02:00. */
  @Scheduled(cron = "${fetcher.schedule.iscritti}")
  public void runIscrittixCorsoJob() {
    runJob(iscrittixCorsoJob, IscrittixCorsoJobConfig.JOB_NAME);
  }

  /** Immatricolati — 15 ottobre alle 02:00. */
  @Scheduled(cron = "${fetcher.schedule.immatricolati}")
  public void runImmatricolatiJob() {
    runJob(immatricolatiJob, ImmatricolatiJobConfig.JOB_NAME);
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
