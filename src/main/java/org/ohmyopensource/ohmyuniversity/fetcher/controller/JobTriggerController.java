package org.ohmyopensource.ohmyuniversity.fetcher.controller;

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
 * Protected by a shared admin secret header (X-Admin-Secret).
 * Only requests with the correct secret are accepted.
 * This prevents unauthorized job execution while still allowing
 * administrators to trigger jobs on demand (e.g. after a failure).
 *
 * Spring Batch prevents duplicate concurrent executions automatically —
 * if a job is already running, a second trigger is rejected safely.
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

  @Value("${fetcher.admin-secret}")
  private String adminSecret;

  public JobTriggerController(
      JobOperator jobOperator,
      @Qualifier(OrdiniJobConfig.JOB_NAME) Job ordiniJob,
      @Qualifier(IscrittixCorsoJobConfig.JOB_NAME) Job iscrittixCorsoJob,
      @Qualifier(ImmatricolatiJobConfig.JOB_NAME) Job immatricolatiJob) {
    this.jobOperator = jobOperator;
    this.ordiniJob = ordiniJob;
    this.iscrittixCorsoJob = iscrittixCorsoJob;
    this.immatricolatiJob = immatricolatiJob;
  }

  /**
   * Triggera un job per nome.
   *
   * @param jobName      nome del job: "ordini", "iscritti", "immatricolati"
   * @param secretHeader valore dell'header X-Admin-Secret
   * @return 202 Accepted se il job è partito,
   *         401 se il secret è errato,
   *         404 se il nome job è sconosciuto,
   *         409 se il job è già in esecuzione o fallisce l'avvio
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

  // ================================
  // Private helpers
  // ================================
  private Job resolveJob(String jobName) {
    return switch (jobName) {
      case "ordini" -> ordiniJob;
      case "iscritti" -> iscrittixCorsoJob;
      case "immatricolati" -> immatricolatiJob;
      default -> null;
    };
  }
}