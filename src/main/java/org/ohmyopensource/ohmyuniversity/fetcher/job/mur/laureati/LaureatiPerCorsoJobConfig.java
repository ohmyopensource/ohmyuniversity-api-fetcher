package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.laureati;

import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaLaureati;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaLaureatiRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common.CkanClient;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common.MurCsvReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Spring Batch configuration for the laureati-per-corso job.
 *
 * Single step, same pattern as the iscritti job: the processor accumulates
 * all records in memory and the writer flushes in afterStep.
 * Processor and writer are {@code @Scope(value = "step", proxyMode = org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS)} to reset state
 * between executions.
 */
@Configuration
public class LaureatiPerCorsoJobConfig {

  private static final Logger log = LoggerFactory.getLogger(LaureatiPerCorsoJobConfig.class);
  private static final int CHUNK_SIZE = 1000;
  public static final String JOB_NAME = "laureatiPerCorsoJob";

  @Bean
  public LaureatiPerCorsoReader laureatiPerCorsoReader(
      CkanClient ckanClient,
      MurCsvReader murCsvReader,
      @Value("${fetcher.mur.ckan-base-url}") String ckanBaseUrl,
      @Value("${fetcher.mur.laureati-dataset-id}") String datasetId) {
    return new LaureatiPerCorsoReader(ckanClient, murCsvReader, ckanBaseUrl, datasetId);
  }

  @Bean
  @Scope(value = "step", proxyMode = org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS)
  public LaureatiPerCorsoProcessor laureatiPerCorsoProcessor() {
    return new LaureatiPerCorsoProcessor();
  }

  @Bean
  @Scope(value = "step", proxyMode = org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS)
  public LaureatiPerCorsoWriter laureatiPerCorsoWriter(
      StatisticaLaureatiRepository repository,
      LaureatiPerCorsoProcessor processor) {
    return new LaureatiPerCorsoWriter(repository, processor);
  }

  @Bean
  public Step laureatiPerCorsoStep(
      JobRepository jobRepository,
      LaureatiPerCorsoReader reader,
      LaureatiPerCorsoProcessor processor,
      LaureatiPerCorsoWriter writer) {
    return new ChunkOrientedStepBuilder<LaureatiRecord, StatisticaLaureati>(
        "laureatiPerCorsoStep", jobRepository, CHUNK_SIZE)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .listener(new StepExecutionListener() {
          @Override
          public ExitStatus afterStep(StepExecution stepExecution) {
            log.info("laureatiPerCorsoStep: afterStep — flushing aggregated records");
            writer.flush();
            return stepExecution.getExitStatus();
          }
        })
        .build();
  }

  @Bean(name = JOB_NAME)
  public Job laureatiPerCorsoJob(JobRepository jobRepository, Step laureatiPerCorsoStep) {
    return new JobBuilder(JOB_NAME, jobRepository)
        .start(laureatiPerCorsoStep)
        .build();
  }
}