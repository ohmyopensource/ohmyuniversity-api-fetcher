"""
OhMyUniversity! — UNIMOL Timetable Crawler
===========================================
Scrapes timetable PDF links from the UNIMOL Angular website.

Reads ``unimol_sources.json`` to obtain the list of stable timetable page
URLs, visits each one using a headless Chrome browser (required because the
UNIMOL site is a single-page Angular application), and extracts the real PDF
URLs hidden inside ReadSpeaker docreader anchor tags.

Produces ``unimol_timetables_raw.json`` — a raw snapshot of all discovered
PDF links, one entry per timetable page.  The raw file is then processed by
``unimol_timetable_cleaner.py`` to remove duplicates and irrelevant PDFs.

Prerequisites
-------------
    pip install selenium webdriver-manager

Usage
-----
    python unimol_timetable_crawler.py

Input
-----
    unimol_sources.json   — must be in the same directory as this script

Output
------
    unimol_timetables_raw.json   — written to the same directory

Notes
-----
- The UNIMOL site wraps every PDF link in a ReadSpeaker docreader URL of the
  form ``https://docreader.readspeaker.com/docreader/?...&url=<real_pdf>``.
  This script extracts the ``url`` query parameter to obtain the real PDF URL.
- Two courses in the Economia department (``direct_pdf: true`` in the sources
  file) link directly to a PDF from the course home page without an
  intermediate timetable page.  These entries are recorded in the output with
  an empty ``pdfs`` list and must be updated manually each semester.
- The Angular router changes the browser URL when navigating between pages, so
  Selenium is the only viable approach — static HTTP clients such as
  ``requests`` + ``BeautifulSoup`` see an empty shell with no rendered content.
"""

import json
import time
from urllib.parse import parse_qs, urlparse

from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.common.exceptions import StaleElementReferenceException, WebDriverException
from webdriver_manager.chrome import ChromeDriverManager

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

SOURCES_FILE = "unimol_sources.json"
OUTPUT_FILE = "unimol_timetables_raw.json"

# Seconds to wait after page load for Angular to finish rendering.
JS_WAIT = 3.0

# Set to False to open a visible browser window (useful for debugging).
HEADLESS = True


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def extract_real_pdf_url(url: str) -> str:
    """Return the actual PDF URL unwrapped from a ReadSpeaker docreader link.

    ReadSpeaker wraps every PDF in a URL of the form::

        https://docreader.readspeaker.com/docreader/?cid=...&url=<real_pdf>

    This function extracts the ``url`` query parameter.  If the input URL is
    not a docreader wrapper it is returned unchanged.

    Args:
        url: The raw href value from the anchor tag.

    Returns:
        The direct PDF URL, or the original URL if no wrapping was detected.
    """
    if "docreader" in url or "readspeaker" in url:
        params = parse_qs(urlparse(url).query)
        if "url" in params:
            return params["url"][0]
    return url


def build_driver() -> webdriver.Chrome:
    """Initialise and return a Chrome WebDriver instance.

    Applies common flags required for stable headless operation in CI and
    local environments.

    Returns:
        A configured ``webdriver.Chrome`` instance ready for use.
    """
    options = Options()
    if HEADLESS:
        options.add_argument("--headless=new")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--disable-gpu")
    options.add_argument("--window-size=1920,1080")
    service = Service(ChromeDriverManager().install())
    return webdriver.Chrome(service=service, options=options)


def collect_pdfs_from_page(driver: webdriver.Chrome,
                            page_url: str) -> list[dict]:
    """Load a timetable page and collect all PDF entries from it.

    Navigates to ``page_url``, waits for Angular to render the content, then
    queries for all anchor tags whose ``href`` attribute contains either
    ``docreader`` or ``.pdf``.  For each matching anchor the real PDF URL is
    extracted and the label is taken from the nearest ``app-box-link`` or
    column container element.

    Args:
        driver:   An active Selenium Chrome WebDriver.
        page_url: The stable timetable page URL to scrape.

    Returns:
        A list of dicts, each with keys ``pdf_url`` and ``label``.
        Returns an empty list if the page cannot be loaded or yields no PDFs.
    """
    results = []

    try:
        driver.get(page_url)
        time.sleep(JS_WAIT)
    except WebDriverException as exc:
        print(f"    [WARN] Cannot load {page_url}: {exc}")
        return results

    try:
        anchors = driver.find_elements(
            By.CSS_SELECTOR, "a[href*='docreader'], a[href*='.pdf']")

        for anchor in anchors:
            try:
                href = anchor.get_attribute("href") or ""
                if not href:
                    continue

                real_pdf = extract_real_pdf_url(href)
                if not real_pdf.lower().endswith(".pdf"):
                    continue

                # Attempt to retrieve a human-readable label from the
                # surrounding card element.
                label = ""
                try:
                    card = anchor.find_element(
                        By.XPATH, "./ancestor::app-box-link")
                    label = card.text.strip()
                except Exception:
                    try:
                        label = anchor.find_element(
                            By.XPATH,
                            "./ancestor::div[contains(@class,'col-')]"
                        ).text.strip()
                    except Exception:
                        label = ""

                results.append({"pdf_url": real_pdf, "label": label})
                print(f"      [PDF] {label or '(no label)'}")
                print(f"            {real_pdf}")

            except StaleElementReferenceException:
                continue

    except Exception as exc:
        print(f"    [WARN] Error collecting PDFs from {page_url}: {exc}")

    return results


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main() -> None:
    """Entry point — read sources, scrape each page, write raw output."""
    print("=" * 60)
    print("OhMyUniversity! — UNIMOL Timetable Crawler")
    print("=" * 60)

    with open(SOURCES_FILE, encoding="utf-8") as f:
        sources = json.load(f)

    driver = build_driver()
    timetables = []

    try:
        for department in sources["departments"]:
            dept_id = department["id"]
            dept_name = department["name"]
            print(f"\n[DEPARTMENT] {dept_name}")

            for degree_type, courses in department["courses"].items():
                # Skip the optional human-readable ``note`` key.
                if degree_type == "note":
                    continue

                for course in courses:
                    # Course entries are either plain URL strings or dicts
                    # (used for anomalous cases such as direct_pdf courses).
                    if isinstance(course, str):
                        page_url = course
                        direct_pdf = False
                        note = ""
                    else:
                        page_url = course["url"]
                        direct_pdf = course.get("direct_pdf", False)
                        note = course.get("note", "")

                    print(f"  [{degree_type}] {page_url}")

                    if direct_pdf:
                        # Direct-PDF courses bypass the intermediate timetable
                        # page entirely — the PDF URL is not discoverable
                        # automatically because it changes every semester.
                        # Record the entry with an empty pdf list so the
                        # cleaner and the Java job are aware of its existence.
                        print(f"    [SKIP] direct_pdf=true — requires manual update each semester")
                        timetables.append({
                            "department_id": dept_id,
                            "department_name": dept_name,
                            "degree_type": degree_type,
                            "timetable_page_url": page_url,
                            "direct_pdf": True,
                            "note": note,
                            "pdfs": [],
                        })
                        continue

                    pdfs = collect_pdfs_from_page(driver, page_url)
                    timetables.append({
                        "department_id": dept_id,
                        "department_name": dept_name,
                        "degree_type": degree_type,
                        "timetable_page_url": page_url,
                        "direct_pdf": False,
                        "note": note,
                        "pdfs": pdfs,
                    })

    finally:
        driver.quit()

    output = {
        "university_id": sources["university_id"],
        "university_name": sources["university_name"],
        "scraped_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "total_pages": len(timetables),
        "total_pdfs": sum(len(t["pdfs"]) for t in timetables),
        "timetables": timetables,
    }

    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)

    print("\n" + "=" * 60)
    print(f"Pages visited : {output['total_pages']}")
    print(f"PDFs found    : {output['total_pdfs']}")
    print(f"Output        : {OUTPUT_FILE}")
    print()
    print("Next step: run unimol_timetable_cleaner.py")


if __name__ == "__main__":
    main()
