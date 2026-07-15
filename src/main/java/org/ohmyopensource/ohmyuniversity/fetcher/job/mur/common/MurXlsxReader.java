package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility for downloading and parsing XLSX files from dati-ustat.mur.gov.it.
 * <p>
 * Several current MUR resources (Offerta formativa, Classi di Laurea, Atenei) are published only as
 * XLSX, with no CSV/Datastore alternative available — verified against the CKAN package_show
 * response ("Data Explorer" disabled on those resources).
 * <p>
 * Byte-level downloading and classpath fallback are delegated to {@link MurResourceDownloader},
 * same as {@link MurCsvReader}; this class only owns XLSX-specific parsing (first sheet, header row
 * skipping, cell-to-string conversion).
 */
@Component
public class MurXlsxReader {

  private static final Logger log = LoggerFactory.getLogger(MurXlsxReader.class);

  private final MurResourceDownloader downloader;

  public MurXlsxReader(MurResourceDownloader downloader) {
    this.downloader = downloader;
  }

  /**
   * Downloads the XLSX from the given URL and returns all data rows from the first sheet, already
   * split into string arrays, excluding the header row. Falls back to the classpath file if the
   * download fails.
   *
   * @param xlsxUrl           direct XLSX download URL (obtained from CkanClient)
   * @param classpathFallback classpath-relative path, e.g. "data/mur/offerta_formativa.xlsx"
   * @return list of String[] arrays, one per data row (header excluded)
   */
  public List<String[]> downloadAndParse(String xlsxUrl, String classpathFallback) {
    byte[] bytes = downloader.downloadWithFallback(xlsxUrl, classpathFallback);
    List<String[]> rows = parseBytes(bytes);
    log.info("MurXlsxReader: parsed {} data rows", rows.size());
    return rows;
  }

  // ================================
  // Private helpers
  // ================================

  private List<String[]> parseBytes(byte[] bytes) {
    List<String[]> rows = new ArrayList<>();

    try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
      Sheet sheet = workbook.getSheetAt(0);
      boolean headerSkipped = false;

      for (Row row : sheet) {
        if (!headerSkipped) {
          headerSkipped = true;
          continue;
        }

        int lastCol = row.getLastCellNum();
        if (lastCol < 0) {
          continue;
        }

        String[] values = new String[lastCol];
        for (int col = 0; col < lastCol; col++) {
          values[col] = cellToString(row.getCell(col));
        }
        rows.add(values);
      }
    } catch (Exception e) {
      throw new RuntimeException("MurXlsxReader: cannot parse XLSX bytes", e);
    }

    return rows;
  }

  private String cellToString(Cell cell) {
    if (cell == null) {
      return "";
    }
    if (cell.getCellType() == CellType.NUMERIC) {
      double value = cell.getNumericCellValue();
      if (value == Math.floor(value)) {
        return String.valueOf((long) value);
      }
      return String.valueOf(value);
    }
    return cell.toString().trim();
  }
}