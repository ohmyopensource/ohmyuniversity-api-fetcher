package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.offerta;

import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.CorsoLaureaNazionale;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.CorsoLaureaNazionaleRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common.CkanClient;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common.MurCsvReader;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common.MurXlsxReader;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch configuration for the "offerta formativa" job.
 *
 * Single step, no cross-record aggregation — each row is already a complete
 * entity, unlike iscritti/immatricolati. No {@code StepExecutionListener}
 * flush needed as a consequence.
 */
@Configuration
public class OffertaFormativaJobConfig {

  private static final int CHUNK_SIZE = 500;
  public static final String JOB_NAME = "offertaFormativaJob";

  @Bean
  public OffertaFormativaReader offertaFormativaReader(
      CkanClient ckanClient,
      MurCsvReader murCsvReader,
      @Value("${fetcher.mur.ckan-base-url}") String ckanBaseUrl,
      @Value("${fetcher.mur.offerta-dataset-id}") String datasetId) {
    return new OffertaFormativaReader(ckanClient, murCsvReader, ckanBaseUrl, datasetId);
  }

  @Bean
  public OffertaFormativaProcessor offertaFormativaProcessor() {
    return new OffertaFormativaProcessor();
  }

  @Bean
  public OffertaFormativaWriter offertaFormativaWriter(
      CorsoLaureaNazionaleRepository repository) {
    return new OffertaFormativaWriter(repository);
  }

  @Bean
  public Step offertaFormativaStep(
      JobRepository jobRepository,
      OffertaFormativaReader reader,
      OffertaFormativaProcessor processor,
      OffertaFormativaWriter writer) {
    return new ChunkOrientedStepBuilder<OffertaFormativaRecord, CorsoLaureaNazionale>(
        "offertaFormativaStep", jobRepository, CHUNK_SIZE)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
  }

  @Bean(name = JOB_NAME)
  public Job offertaFormativaJob(JobRepository jobRepository, Step offertaFormativaStep) {
    return new JobBuilder(JOB_NAME, jobRepository)
        .start(offertaFormativaStep)
        .build();
  }
}