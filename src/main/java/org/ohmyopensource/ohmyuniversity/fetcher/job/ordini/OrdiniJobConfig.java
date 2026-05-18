package org.ohmyopensource.ohmyuniversity.fetcher.job.ordini;

import tools.jackson.databind.json.JsonMapper;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.OrdineProfessionale;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.OrdineProfessionaleRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch configuration for the ordini-professionali job.
 */
@Configuration
public class OrdiniJobConfig {

  private static final int CHUNK_SIZE = 10;
  public static final String JOB_NAME = "ordiniJob";

  @Bean
  public OrdiniReader ordiniReader(JsonMapper jsonMapper) {
    return new OrdiniReader(jsonMapper);
  }

  @Bean
  public OrdiniProcessor ordiniProcessor() {
    return new OrdiniProcessor();
  }

  @Bean
  public OrdiniWriter ordiniWriter(OrdineProfessionaleRepository repository) {
    return new OrdiniWriter(repository);
  }

  @Bean
  public Step ordiniStep(
      JobRepository jobRepository,
      OrdiniReader ordiniReader,
      OrdiniProcessor ordiniProcessor,
      OrdiniWriter ordiniWriter) {
    return new ChunkOrientedStepBuilder<OrdineRecord, OrdineProfessionale>(
        "ordiniStep", jobRepository, CHUNK_SIZE)
        .reader(ordiniReader)
        .processor(ordiniProcessor)
        .writer(ordiniWriter)
        .build();
  }

  @Bean(name = JOB_NAME)
  public Job ordiniJob(JobRepository jobRepository, Step ordiniStep) {
    return new JobBuilder(JOB_NAME, jobRepository)
        .start(ordiniStep)
        .build();
  }
}