package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.iscritti;

import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaIscritti;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaIscrittiRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common.CkanClient;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common.MurCsvReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.ExitStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch configuration for the iscritti-per-corso job.
 *
 * <p>Single step with chunk size 1000. The processor accumulates all records
 * in memory and aggregates M+F totals — the writer must therefore be notified
 * at the end of the step via {@code StepExecutionListener.afterStep} to trigger
 * the final flush.
 *
 * <p>The high chunk size is intentional: since the processor always returns null,
 * Spring Batch accumulates up to the chunk size before calling the writer, which
 * receives an empty chunk. The real write happens in afterStep.
 */
@Configuration
public class IscrittixCorsoJobConfig {

  private static final Logger log = LoggerFactory.getLogger(IscrittixCorsoJobConfig.class);
  private static final int CHUNK_SIZE = 1000;
  public static final String JOB_NAME = "iscrittixCorsoJob";

  @Bean
  public IscrittixCorsoReader iscrittixCorsoReader(
      CkanClient ckanClient,
      MurCsvReader murCsvReader,
      @Value("${fetcher.mur.ckan-base-url}") String ckanBaseUrl,
      @Value("${fetcher.mur.iscritti-dataset-id}") String datasetId) {
    return new IscrittixCorsoReader(ckanClient, murCsvReader, ckanBaseUrl, datasetId);
  }

  @Bean
  public IscrittixCorsoProcessor iscrittixCorsoProcessor() {
    return new IscrittixCorsoProcessor();
  }

  @Bean
  public IscrittixCorsoWriter iscrittixCorsoWriter(
      StatisticaIscrittiRepository repository,
      IscrittixCorsoProcessor processor) {
    return new IscrittixCorsoWriter(repository, processor);
  }

  @Bean
  public Step iscrittixCorsoStep(
      JobRepository jobRepository,
      IscrittixCorsoReader reader,
      IscrittixCorsoProcessor processor,
      IscrittixCorsoWriter writer) {
    return new ChunkOrientedStepBuilder<IscrittoRecord, StatisticaIscritti>(
        "iscrittixCorsoStep", jobRepository, CHUNK_SIZE)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .listener(new StepExecutionListener() {
          @Override
          public ExitStatus afterStep(StepExecution stepExecution) {
            log.info("iscrittixCorsoStep: afterStep — flushing aggregated records");
            writer.flush();
            return stepExecution.getExitStatus();
          }
        })
        .build();
  }

  @Bean(name = JOB_NAME)
  public Job iscrittixCorsoJob(JobRepository jobRepository, Step iscrittixCorsoStep) {
    return new JobBuilder(JOB_NAME, jobRepository)
        .start(iscrittixCorsoStep)
        .build();
  }
}