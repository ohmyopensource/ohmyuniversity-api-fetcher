package org.ohmyopensource.ohmyuniversity.fetcher.job.ordini;

import java.time.Instant;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.OrdineProfessionale;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.OrdineProfessionaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

/**
 * Persists {@link OrdineProfessionale} entities using upsert logic.
 *
 * If a record with the same codice already exists, its fields are
 * updated in place. Otherwise, a new record is created.
 * This makes the job fully idempotent — running it multiple times
 * produces the same result.
 */
public class OrdiniWriter implements ItemWriter<OrdineProfessionale> {

  private static final Logger log = LoggerFactory.getLogger(OrdiniWriter.class);

  private final OrdineProfessionaleRepository repository;

  public OrdiniWriter(OrdineProfessionaleRepository repository) {
    this.repository = repository;
  }

  @Override
  public void write(Chunk<? extends OrdineProfessionale> chunk) {
    for (OrdineProfessionale incoming : chunk.getItems()) {
      repository.findByCodice(incoming.getCodice())
          .ifPresentOrElse(
              existing -> {
                existing.setNome(incoming.getNome());
                existing.setCategoria(incoming.getCategoria());
                existing.setOrganismoNazionale(incoming.getOrganismoNazionale());
                existing.setSitoUfficiale(incoming.getSitoUfficiale());
                existing.setRequisitiLaurea(incoming.getRequisitiLaurea());
                existing.setRequisitiNote(incoming.getRequisitiNote());
                existing.setMinisteroVigilanza(incoming.getMinisteroVigilanza());
                existing.setAggiornatoAl(incoming.getAggiornatoAl());
                existing.setFetchedAt(Instant.now());
                repository.save(existing);
                log.debug("OrdiniWriter: updated codice={}", incoming.getCodice());
              },
              () -> {
                repository.save(incoming);
                log.debug("OrdiniWriter: created codice={}", incoming.getCodice());
              }
          );
    }
    log.info("OrdiniWriter: processed {} items", chunk.size());
  }
}