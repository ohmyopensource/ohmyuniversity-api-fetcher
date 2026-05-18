package org.ohmyopensource.ohmyuniversity.fetcher.config;

import org.ohmyopensource.ohmyuniversity.fetcher.job.ordini.OrdiniJobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

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
@Configuration
@EnableScheduling
public class SchedulerConfig {

  private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);

  private final JobOperator jobOperator;
  private final Job ordiniJob;

  public SchedulerConfig(
      JobOperator jobOperator,
      @Qualifier(OrdiniJobConfig.JOB_NAME) Job ordiniJob) {
    this.jobOperator = jobOperator;
    this.ordiniJob = ordiniJob;
  }

  /**
   * Run the ordini job on the schedule defined in fetcher.schedule.ordini.
   * Default: first day of February and August at 01:00.
   */
  @Scheduled(cron = "${fetcher.schedule.ordini}")
  public void runOrdiniJob() {
    log.info("Scheduler: triggering ordiniJob");
    try {
      JobParameters params = new JobParametersBuilder()
          .addLong("run.at", System.currentTimeMillis())
          .toJobParameters();
      jobOperator.start(ordiniJob, params);
    } catch (Exception e) {
      log.error("Scheduler: ordiniJob failed", e);
    }
  }
}