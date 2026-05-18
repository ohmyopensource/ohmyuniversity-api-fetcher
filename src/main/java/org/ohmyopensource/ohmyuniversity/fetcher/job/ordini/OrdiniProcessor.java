package org.ohmyopensource.ohmyuniversity.fetcher.job.ordini;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.CategoriaOrdine;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.OrdineProfessionale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;

/**
 * Transforms an {@link OrdineRecord} (raw JSON DTO) into an
 * {@link OrdineProfessionale} entity ready for persistence.
 *
 * Does not perform the upsert lookup — that is the responsibility
 * of {@link OrdiniWriter}. This processor only maps fields and validates
 * that the categoria value is a known enum constant.
 *
 * Records with unknown categoria are skipped (returns null),
 * which Spring Batch treats as a filtered item.
 */
public class OrdiniProcessor implements ItemProcessor<OrdineRecord, OrdineProfessionale> {

  private static final Logger log = LoggerFactory.getLogger(OrdiniProcessor.class);

  @Override
  public OrdineProfessionale process(OrdineRecord record) {
    CategoriaOrdine categoria;
    try {
      categoria = CategoriaOrdine.valueOf(record.getCategoria());
    } catch (IllegalArgumentException e) {
      log.warn("OrdiniProcessor: unknown categoria '{}' for codice '{}' — skipping",
          record.getCategoria(), record.getCodice());
      return null;
    }

    OrdineProfessionale entity = new OrdineProfessionale();
    entity.setCodice(record.getCodice());
    entity.setNome(record.getNome());
    entity.setCategoria(categoria);
    entity.setOrganismoNazionale(record.getOrganismoNazionale());
    entity.setSitoUfficiale(record.getSitoUfficiale());
    entity.setRequisitiNote(record.getRequisitiNote());
    entity.setMinisteroVigilanza(record.getMinisteroVigilanza());
    entity.setFetchedAt(Instant.now());

    List<String> requisiti = record.getRequisitiLaurea();
    entity.setRequisitiLaurea(
        requisiti != null ? requisiti.toArray(new String[0]) : new String[0]);

    if (record.getAggiornatoAl() != null) {
      entity.setAggiornatoAl(LocalDate.parse(record.getAggiornatoAl()));
    }

    return entity;
  }
}