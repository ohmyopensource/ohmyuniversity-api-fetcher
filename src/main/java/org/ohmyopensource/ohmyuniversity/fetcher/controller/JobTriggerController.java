package org.ohmyopensource.ohmyuniversity.fetcher.controller;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for manually triggering batch jobs.
 *
 * <p>Protected by a shared admin secret header (X-Admin-Secret).
 * Spring Batch prevents duplicate concurrent executions automatically.
 *
 * <p>Available jobs:
 * <ul>
 *   <li>{@code ordini} — Italian professional orders</li>
 *   <li>{@code iscritti} — enrolled students per course, university and degree class</li>
 *   <li>{@code immatricolati} — first-year students per class and per university</li>
 *   <li>{@code laureati} — graduates per course, university and degree class</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/jobs")
public class JobTriggerController {

  private static final Logger log = LoggerFactory.getLogger(JobTriggerController.class);
  private static final String ADMIN_SECRET_HEADER = "X-Admin-Secret";

  private final JobOperator jobOperator;
  private final Job ordiniJob;
  private final Job iscrittixCorsoJob;
  private final Job immatricolatiJob;
  private final Job laureatiPerCorsoJob;

  @Value("${fetcher.admin-secret}")
  private String adminSecret;

  public JobTriggerController(
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

  /**
   * Triggers a batch job by name.
   *
   * @param jobName      job name: "ordini", "iscritti", "immatricolati", "laureati"
   * @param secretHeader value of the X-Admin-Secret header
   * @return 202 Accepted if the job started,
   *         401 if the secret is wrong,
   *         404 if the job name is unknown,
   *         409 if the job is already running or failed to start
   */
  @PostMapping("/{jobName}/run")
  public ResponseEntity<String> triggerJob(
      @PathVariable String jobName,
      @RequestHeader(value = ADMIN_SECRET_HEADER, required = false) String secretHeader) {

    if (secretHeader == null || !secretHeader.equals(adminSecret)) {
      log.warn("JobTriggerController: unauthorized trigger attempt for job '{}'", jobName);
      return ResponseEntity.status(401).body("Unauthorized");
    }

    Job job = resolveJob(jobName);
    if (job == null) {
      return ResponseEntity.status(404).body("Unknown job: " + jobName);
    }

    try {
      JobParameters params = new JobParametersBuilder()
          .addLong("run.at", System.currentTimeMillis())
          .toJobParameters();
      jobOperator.start(job, params);
      log.info("JobTriggerController: job '{}' triggered manually", jobName);
      return ResponseEntity.accepted().body("Job '" + jobName + "' started");
    } catch (Exception e) {
      log.error("JobTriggerController: failed to start job '{}'", jobName, e);
      return ResponseEntity.status(409).body("Job already running or failed to start: "
          + e.getMessage());
    }
  }

  private Job resolveJob(String jobName) {
    return switch (jobName) {
      case "ordini" -> ordiniJob;
      case "iscritti" -> iscrittixCorsoJob;
      case "immatricolati" -> immatricolatiJob;
      case "laureati" -> laureatiPerCorsoJob;
      default -> null;
    };
  }
}