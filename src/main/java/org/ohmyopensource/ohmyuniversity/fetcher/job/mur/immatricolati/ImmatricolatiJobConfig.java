package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.immatricolati;

import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiAteneo;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiClasse;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaImmatricolatiAteneoRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaImmatricolatiClasseRepository;
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
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * Spring Batch configuration for the immatricolati job.
 *
 * Processor and writer use {@code @Scope("step")} with {@code proxyMode = TARGET_CLASS}
 * to avoid ScopeNotActiveException at startup.
 */
@Configuration
public class ImmatricolatiJobConfig {

  private static final Logger log = LoggerFactory.getLogger(ImmatricolatiJobConfig.class);
  private static final int CHUNK_SIZE = 500;
  public static final String JOB_NAME = "immatricolatiJob";

  // ================================
  // Step 1: per classe
  // ================================

  @Bean
  public ImmatricolatiPerClasseReader immatricolatiPerClasseReader(
      CkanClient ckanClient,
      MurCsvReader murCsvReader,
      @Value("${fetcher.mur.ckan-base-url}") String ckanBaseUrl,
      @Value("${fetcher.mur.immatricolati-dataset-id}") String datasetId) {
    return new ImmatricolatiPerClasseReader(ckanClient, murCsvReader, ckanBaseUrl, datasetId);
  }

  @Bean
  @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
  public ImmatricolatiPerClasseProcessor immatricolatiPerClasseProcessor() {
    return new ImmatricolatiPerClasseProcessor();
  }

  @Bean
  @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
  public ImmatricolatiPerClasseWriter immatricolatiPerClasseWriter(
      StatisticaImmatricolatiClasseRepository repository,
      ImmatricolatiPerClasseProcessor processor) {
    return new ImmatricolatiPerClasseWriter(repository, processor);
  }

  @Bean
  public Step immatricolatiPerClasseStep(
      JobRepository jobRepository,
      ImmatricolatiPerClasseReader reader,
      ImmatricolatiPerClasseProcessor processor,
      ImmatricolatiPerClasseWriter writer) {
    return new ChunkOrientedStepBuilder<ImmatricolatoClasseRecord, StatisticaImmatricolatiClasse>(
        "immatricolatiPerClasseStep", jobRepository, CHUNK_SIZE)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .listener(new StepExecutionListener() {
          @Override
          public ExitStatus afterStep(StepExecution stepExecution) {
            log.info("immatricolatiPerClasseStep: afterStep — flushing");
            writer.flush();
            return stepExecution.getExitStatus();
          }
        })
        .build();
  }

  // ================================
  // Step 2: per ateneo
  // ================================

  @Bean
  public ImmatricolatiPerAteneoReader immatricolatiPerAteneoReader(
      CkanClient ckanClient,
      MurCsvReader murCsvReader,
      @Value("${fetcher.mur.ckan-base-url}") String ckanBaseUrl,
      @Value("${fetcher.mur.immatricolati-dataset-id}") String datasetId) {
    return new ImmatricolatiPerAteneoReader(ckanClient, murCsvReader, ckanBaseUrl, datasetId);
  }

  @Bean
  @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
  public ImmatricolatiPerAteneoProcessor immatricolatiPerAteneoProcessor() {
    return new ImmatricolatiPerAteneoProcessor();
  }

  @Bean
  @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
  public ImmatricolatiPerAteneoWriter immatricolatiPerAteneoWriter(
      StatisticaImmatricolatiAteneoRepository repository,
      ImmatricolatiPerAteneoProcessor processor) {
    return new ImmatricolatiPerAteneoWriter(repository, processor);
  }

  @Bean
  public Step immatricolatiPerAteneoStep(
      JobRepository jobRepository,
      ImmatricolatiPerAteneoReader reader,
      ImmatricolatiPerAteneoProcessor processor,
      ImmatricolatiPerAteneoWriter writer) {
    return new ChunkOrientedStepBuilder<ImmatricolatoAteneoRecord, StatisticaImmatricolatiAteneo>(
        "immatricolatiPerAteneoStep", jobRepository, CHUNK_SIZE)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .listener(new StepExecutionListener() {
          @Override
          public ExitStatus afterStep(StepExecution stepExecution) {
            log.info("immatricolatiPerAteneoStep: afterStep — flushing");
            writer.flush();
            return stepExecution.getExitStatus();
          }
        })
        .build();
  }

  // ================================
  // Job
  // ================================

  @Bean(name = JOB_NAME)
  public Job immatricolatiJob(
      JobRepository jobRepository,
      Step immatricolatiPerClasseStep,
      Step immatricolatiPerAteneoStep) {
    return new JobBuilder(JOB_NAME, jobRepository)
        .start(immatricolatiPerClasseStep)
        .next(immatricolatiPerAteneoStep)
        .build();
  }
}