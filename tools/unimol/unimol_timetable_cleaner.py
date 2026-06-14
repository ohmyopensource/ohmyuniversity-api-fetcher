"""
OhMyUniversity! — UNIMOL Timetable Cleaner
===========================================
Post-processes the raw scraper output produced by ``unimol_timetable_crawler.py``
and writes a clean, deduplicated JSON file ready for import into the fetcher
database.

The cleaner performs three operations in order:

1. **Deduplication** — removes duplicate PDF URLs within the same timetable
   page entry.  Duplicates arise because Angular renders the same PDF card
   in both the first-semester and second-semester sections of a page, causing
   the scraper to encounter the same anchor twice.

2. **Label sanitisation** — if a label is longer than 120 characters it means
   the scraper captured the full descriptive text of the page rather than the
   short card title (this happens on pages such as Scienze della Formazione
   Primaria that contain long prose introductions).  Such labels are replaced
   with an empty string.

3. **Non-timetable filtering** — removes PDF entries that are not lesson
   timetables: academic calendars, attendance certificates, CLA language lab
   schedules, elective-credit guides, and similar administrative documents.
   Filtering is applied by matching against known label keywords and URL
   fragments.

Prerequisites
-------------
    No additional dependencies beyond the Python standard library.

Usage
-----
    python unimol_timetable_cleaner.py

Input
-----
    unimol_timetables_raw.json   — produced by unimol_timetable_crawler.py,
                                   must be in the same directory as this script

Output
------
    unimol_timetables.json   — written to the same directory;
                               this is the file consumed by the Java fetcher job
"""

import json

INPUT_FILE = "unimol_timetables_raw.json"
OUTPUT_FILE = "unimol_timetables.json"

# ---------------------------------------------------------------------------
# Filter configuration
# ---------------------------------------------------------------------------

# PDF card labels (case-insensitive substrings) that identify non-timetable
# documents and should be excluded from the clean output.
EXCLUDE_LABEL_KEYWORDS = [
    "attestato",
    "calendario attività didattiche",
    "calendario delle attività didattiche",
    "calendario attivita' didattica",
    "calendario attivita' didattiche",
    "distribuzione insegnamenti",
    "cla",
    "lingua inglese",
    "laboratori lingua",
    "prova finale",
]

# PDF URL substrings that identify non-timetable documents regardless of label.
# Used as a fallback for entries whose label extraction failed (empty string).
EXCLUDE_URL_FRAGMENTS = [
    "Attestato",
    "attestato",
    "Calendario_attivita_didattiche",
    "calendario_attivita_didattiche",
    "Distribuzione_insegnamenti",
    "CLA_Laboratori",
    "SUSeF-giugno-2023",
]

# Maximum label length in characters.  Labels exceeding this threshold are
# assumed to be full-page text dumps rather than card titles and are cleared.
MAX_LABEL_LENGTH = 120


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def sanitise_label(label: str) -> str:
    """Return the label unchanged if it is short, or an empty string if not.

    A label longer than ``MAX_LABEL_LENGTH`` characters indicates that the
    Selenium scraper captured the entire page description instead of the card
    title — this happens on Angular pages that mix prose text with card
    components in the same DOM ancestor chain.

    Args:
        label: The raw label string extracted by the crawler.

    Returns:
        The original label stripped of leading/trailing whitespace, or an
        empty string if the label exceeds the length threshold.
    """
    if len(label) > MAX_LABEL_LENGTH:
        return ""
    return label.strip()


def is_excluded(pdf: dict) -> bool:
    """Return True if the PDF entry should be removed from the clean output.

    An entry is excluded when its label matches a known non-timetable keyword
    or its URL contains a known non-timetable fragment.

    Args:
        pdf: A dict with keys ``pdf_url`` and ``label``.

    Returns:
        True if the entry should be excluded, False otherwise.
    """
    label = pdf.get("label", "").lower()
    url = pdf.get("pdf_url", "")

    if any(keyword in label for keyword in EXCLUDE_LABEL_KEYWORDS):
        return True
    if any(fragment in url for fragment in EXCLUDE_URL_FRAGMENTS):
        return True
    return False


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def clean(data: dict) -> dict:
    """Apply deduplication, label sanitisation and filtering to ``data``.

    Mutates ``data`` in place and updates the ``total_pdfs`` counter.

    Args:
        data: The parsed contents of ``unimol_timetables_raw.json``.

    Returns:
        The same ``data`` dict after cleaning.
    """
    total_before = sum(len(t["pdfs"]) for t in data["timetables"])
    total_dupes = 0
    total_filtered = 0

    for entry in data["timetables"]:
        seen_urls: set[str] = set()
        clean_pdfs = []

        for pdf in entry["pdfs"]:
            url = pdf["pdf_url"]
            label = sanitise_label(pdf.get("label", ""))
            pdf_clean = {"pdf_url": url, "label": label}

            # Skip duplicates.
            if url in seen_urls:
                total_dupes += 1
                continue
            seen_urls.add(url)

            # Skip non-timetable documents.
            if is_excluded(pdf_clean):
                total_filtered += 1
                continue

            clean_pdfs.append(pdf_clean)

        entry["pdfs"] = clean_pdfs

    total_after = sum(len(t["pdfs"]) for t in data["timetables"])
    data["total_pdfs"] = total_after

    print(f"PDFs before cleaning : {total_before}")
    print(f"Duplicates removed   : {total_dupes}")
    print(f"Non-timetable removed: {total_filtered}")
    print(f"PDFs after cleaning  : {total_after}")

    return data


def main() -> None:
    """Entry point — load raw file, clean it, write clean output."""
    print("=" * 60)
    print("OhMyUniversity! — UNIMOL Timetable Cleaner")
    print("=" * 60)

    with open(INPUT_FILE, encoding="utf-8") as f:
        data = json.load(f)

    print(f"Input  : {INPUT_FILE}")
    data = clean(data)

    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"Output : {OUTPUT_FILE}")


if __name__ == "__main__":
    main()
