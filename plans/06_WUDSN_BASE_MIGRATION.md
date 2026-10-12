# Plan: Migrate ASMA onto the WUDSN Base library

Written 2026-10-10. Goal: streamline the ASMA Java code by building on
`com.wudsn.tools.base` (GitHub `wudsn/wudsn-base`), the shared foundation of
DIS6502, RASTER Music Tracker and The!Cart Studio (see its
`plans/RULES_WUDSN_BASE.md`), and thereby make the ASMA code directly
reusable in the RMT context. RMT already depends on
`com.wudsn.tools.base(.atari)` 1.0.0-SNAPSHOT and renders the base
`MessageQueue` in its GUI, so this migration unifies the tool family.

**Working rule: every non-mechanical step is proposed and confirmed with
the user first. Only mechanical 1:1 swaps with identical semantics are
executed directly; anything involving API mismatches, paradigm changes,
public API changes of asma-lib, or data contract decisions stops and asks.**

## Key findings from the analysis

- Overlapping utilities: `FileUtility`, `StringUtility`, `MemoryUtility`
  (base: `MemorySize`), `ByteUtility` (base: `ByteArrayUtility`/`HexUtility`)
  map to base equivalents; `JSONWriter`/`Serializer` (gson) have NO base
  equivalent and stay in asma-lib.
- The `MessageQueue` paradigms differ fundamentally: ASMA uses ad-hoc string
  codes (`sendError("SAP-110", text)`); base uses NLS `Message` repository
  classes with `.properties`, parameters, fail-fast load-time checks and a
  GUI renderer. Migrating is the main work and the main win (ASMA validation
  results become displayable in RMT's message UI).
- `base.atari` holds `SAPFile` (62 lines, header-level), `ExecutableFile`
  (XEX), `AtrFile`, `Platform` - overlap with ASMA's `SAPTags`, `Segment*`,
  XEX handling and `Hardware` strings. Long-term convergence candidate:
  grow `base.atari` from ASMA's deeper format logic (also relevant for RMT's
  SAP-R recording work).
- CI: wudsn-base is not on Maven Central; RMT's workflows solve this by
  checking out `wudsn/wudsn-base` and running `mvn install` first - copy
  that step. JitPack publishing of asma-lib (plan 01) then also needs
  wudsn-base resolvable for JitPack; decide before tagging.
- The `asmadb.js`/`asmadb.json` data contract does not change (e.g. the
  `"ATARI800"` hardware strings stay), and the byte-identical export is the
  regression oracle after every phase.
- Sequencing: migrate BEFORE tagging v1.0.0, because asma-lib's public API
  changes (`MessageQueue` in `FileInfo`/`checkFileInfos` signatures).

## Phases

1. **Groundwork** - DONE 2026-10-10, with a scope correction: the API
   comparison showed the utility classes are name-twins but API-strangers
   (base `FileUtility.readBytes` throws the NLS-coupled `CoreException`;
   base `StringUtility` has none of ASMA's methods; hex formats differ and
   partly feed artifacts). Per the working rule this was escalated; decision:
   **keep all ASMA utility classes untouched until phase 2**, where
   `CoreException`/`Message` arrive as one coherent design. Executed:
   - `com.wudsn.tools:com.wudsn.tools.base:1.0.0-SNAPSHOT` in the parent
     dependencyManagement and in asma-lib/asma-tools.
   - `requires (transitive) com.wudsn.tools.base;` in both module-infos
     (automatic module; an explicit open module since 2026-10-12).
   - CI: checkout `wudsn/wudsn-base` + `mvn install` before the build in
     `build.yml` and `release.yml` (RMT's proven pattern).
   - `mvn clean install` green; the shaded SAPFileEditor.jar now bundles the
     base classes (unused until phase 2).

2. **MessageQueue and NLS messages** (NON-MECHANICAL - propose design,
   ask first): replace the ASMA MessageQueue world with the base one; a
   `Messages` repository class per the RULES doc for all SAP/COM/DMZ/EXP
   codes with `.properties`; adapt all senders and the GUI/console output.
   Base conventions decided in WUDSN Base plan 01 (2026-10-10): the
   repository classes declare `public static final String AREA`, exactly
   3 upper-case letters; the display accessor is `getIdentifier()`
   (`<AREA>-<number>`). The Demozoo area is `DMZ`, so the visible Demozoo
   codes change from today's `DMO-xxx` to `DMZ-xxx`; all other ASMA codes
   (`SAP`, `COM`, `EXP`) stay identical.

3. **Editor GUI onto base** (NON-MECHANICAL - ask first, optional):
   `ElementFactory`/`Actions`/`ModalDialog`/`AttributeTablePanel` for
   SAPFileEditor, aligning it with DIS6502/RMT conventions.
   DONE 2026-10-12, with the goal of less code: base `FileDrop` replaces
   the 822-line copy; `ASAPPanel` extends the browser's `DetailsPanel` with
   the `FileInfo_Author/Title/Date` data types; new editor `Actions` and
   `Texts` repositories with `ElementFactory.createButton`; base `UIManager`
   (also in ASMABrowser) and `Desktop.openFile`; dead code removed.
   `ModalDialog`/`AttributeTablePanel` did not apply (the editor has no
   modal dialogs and no tables); the window structure (file list window
   plus one frame per file) stays as it is.

4. **Format logic convergence into base.atari** (NON-MECHANICAL - ask
   first, separate decision, likely driven from the RMT side): SAP/SAP-R/
   XEX/segment logic shared between ASMA and RMT.

## Phase 2 design proposal (2026-10-11)

**Status: decided 2026-10-12. M4: three areas SAP, ASA and DMZ, and one
`Messages` class per package. M1-M3, M5 and M6 as recommended.**

**Executed 2026-10-12** (wudsn-base 873c26a, then this repository).

Regression against a baseline export made before the change:
- `asmadb.js` and `asmadb.json` are byte-identical.
- The issues reports differ only in the new codes, the M5 renumbering, the
  removed stray "Edit Download Links" links and the M6 typos.
- The summary is unchanged: 0 errors, 7 warnings, 17 infos.

Details decided during the implementation:
- Messages whose whole text was a bare exception message got context:
  - SAP-021 "Cannot write file. {0}";
  - DMZ-105 "Cannot save to '{0}'. {1}";
  - DMZ-108 "Fetch error for '{0}': {1}";
  - ASA-401 "Cannot process {0}. {1}".
- SAP-002 now includes the index that its text announced.
- The usage lines (formerly also `DMO-100`) are plain console output.
- ASA-002 (formerly SAP-102) stays reserved for the dropped file name
  length check.
- The SAPFileDialog shows messages as "ERROR : SAP-007 - text", like the
  issues report.
- `org.atari.asma.util.MessageQueueUtility` holds ASMA's report policies:
  the console queue, the summary, the queue comparison and the severity
  text.
- The cross-class identifier check is
  `asma-tools/src/test/java/org/atari/asma/exporter/MessagesTest.java`.
  The test cannot live in `org.atari.asma`, because asma-lib owns that
  package.

### Current state (inventory)

- **ASMA's own queue** (`org.atari.asma.util`, asma-lib):
  - `MessageQueue` holds entries of type INFO/WARNING/ERROR with a free
    string ID and a ready-made text.
  - It echoes every message immediately to `System.out` or `System.err`,
    when bound via `MessageQueueFactory`.
  - It counts per type and has `printSummary()`, `containsMessage(id)`,
    `addEntries(queue)` and `compareTo` (by error, then warning, then info
    count).
  - `BufferedMessageQueue` collects the console output of a parallel
    worker and hands it to the parent as one text, under the ID `GEN-001`.
- **118 send calls in 15 files.** By area:

  | Area | Codes | Senders | Module |
  |---|---|---|---|
  | SAP | 001-023 | `ASAPFileLogic` | tools |
  | SAP | 101-110 | `FileInfoList` | lib |
  | SAP | 111 | `FileInfoScanner` | tools |
  | COM | 001-008 | `ComposerList` | lib |
  | GRP | 001-006 | `GroupList` | lib |
  | SEG | 001-003 | `SegmentListLogic` | lib |
  | DMO | 001-009 | `ASMAProductionList` | lib |
  | DMO | 100-112 | `Demozoo`, `ZipUtility` | lib |
  | DMO | 100-103 | `DemozooImporter` | tools |
  | EXP | 001 | `ASMAExporter` | tools |
  | EDI | 001 | `SAPFileEditor` | tools |
  | DIA | 001-002 | `SAPFileDialog` | tools |
  | GEN | 001 | `BufferedMessageQueue` | lib |

  The table has more areas than phase 2 above names (GRP, SEG, EDI, DIA
  and GEN).
- **About 30 infos have no ID** (`sendInfo(text)`). They are progress
  lines and user feedback, such as "Scanning ...", "305 composers loaded"
  and "SAP file '...' saved". In the SAPFileEditor, the dialog lists them.
- **Duplicate codes with different meanings:**
  - `SAP-006` means both "Header does not start with 'SAP' line" and
    "Invalid SAP file. ...".
  - `SAP-103` means both an invalid character in the folder path and one in
    the file name.
  - `DMO-006` means both the warning "tag sap without .sap" and the error
    "download URL contains non-existing file path".
  - **The `DMO-006` duplicate is a bug:** `ASMAHTMLExporter` adds the "Edit
    Download Links" link via `containsMessage("DMO-006")`, so it also
    appears for the tag warning.
  - `SAP-020` (three times) and `SAP-021` (twice) reuse one meaning, which
    is fine.
- **Consumers:**
  - The console tools (`ASMAExporter`, `DemozooImporter`, `ZipUtility`)
    print live output and a summary.
  - `ASMAHTMLExporter` builds the issues report from the per-file and
    per-production queues. It uses counts, `containsMessage` and
    `compareTo`, and `hasIssues` means errors or warnings.
  - `SAPFileDialog` lists the entries.
  - `asmadb.js`/`asmadb.json` contain no messages, so the data contract and
    the regression oracle are unaffected.
  - The RMT port does not use ASMA's queue.
- **WUDSN Base today:**
  - `Message` has STATUS, INFO and ERROR, but **no WARNING**. NLS derives
    the severity from the field name's first letter (`S`, `I`, `E`).
  - `MessageQueue` is `final`. It has `containsError()`, the first error
    and info entries, and `getEntries()`, but no counts, no lookup by
    message and no merge.
  - A renderer receives only STATUS messages immediately; everything else
    is rendered when the caller asks.
  - `console.Console` renders as "INFO:  " or "ERROR: " plus
    `<AREA>-<number> - <text>`.
  - **Bug:** `MessageQueueEntry.toString()` switches on a `severity` field
    that is never set, so it always throws.

### Decisions

- **M1 - Warning severity.** ASMA has 12 warnings, for example
  `SAP-009..011` "strongly recommended tag missing", and the issues report
  counts them separately.
  - *(a, recommended)* **Add `Message.WARNING` to Base:**
    - field prefix `W`;
    - `Console` prints "WARNING:" on stderr;
    - `StatusBar` shows the warning icon.
  - *(b)* Map warnings to INFO. This loses them from the report.
  - *(c)* Map warnings to ERROR. This turns hints into errors.
- **M2 - What ASMA needs from the queue: counts, lookup, merge and live
  echo.**
  - *(a, recommended)* **Small generic additions to Base `MessageQueue`:**
    - `getCount(severity)`;
    - `contains(Message)`;
    - `addAll(MessageQueue)`;
    - an optional `MessageQueueListener` that is called on every send.
    `Console` implements the listener, so the console tools keep their live
    output.
  - Comparing queues and printing the summary stay in ASMA as small static
    helpers, because they are report policy.
  - *(b)* An ASMA-local wrapper around Base's final queue. Every tool would
    need it, and RMT could not render ASMA results directly.
- **M3 - The infos without an ID.**
  - *(recommended)* They become real messages: INFO in the area of their
    sender, for example `EXP-002` "{0} composers loaded.". The GUI and
    console then show them uniformly.
  - The usage lines of the console tools stay plain `println`, because they
    are help text, not messages.
- **M4 - Areas.** Decided 2026-10-11/12: three areas by function. Each
  area has room for 1000 numbers and holds only a few messages per topic,
  so topics get number ranges instead of areas of their own. The visible
  codes may change, because only the plans refer to them, never
  documentation or website text.
  - **SAP** - the SAP file format: content, tags, segments, conversion and
    saving.
    - It keeps SAP-001..023.
    - SEG-001..003 become **SAP-024..026**.
  - **ASA** - ASMA itself: the archive checks and the ASMA programs, one
    number range per topic:
    - files **ASA-001..099** (from SAP-101..111, as ASA-001..011);
    - composers **ASA-101..199** (from COM-001..008, as ASA-101..108);
    - groups **ASA-201..299** (from GRP-001..006, as ASA-201..206);
    - ASMAExporter **ASA-301..399** (from EXP-001, plus the progress
      infos of M3);
    - SAPFileEditor **ASA-401..499** (from EDI-001 and DIA-001..002, plus
      the progress infos of M3);
    - ASMABrowser **ASA-501..599** (from ASB, which plan 07 had decided):
      `I001`, `E002`, `E004`, `S005` and `E006` become `I501`, `E502`,
      `E504`, `S505` and `E506`.
  - **DMZ** - Demozoo checks and import, with unchanged numbers (from DMO).
  - `GEN-001` disappears together with `BufferedMessageQueue`. The
    SAPFileEditor's parallel workers each get their own queue, merged into
    the main queue via `addAll`.
  - **Classes:** one repository class named `Messages` per package, like
    the existing `org.atari.asma.browser.Messages`. NLS checks the `AREA`
    per class only, so several classes may share an area within their own
    number ranges:

    | Class | Module | Area | Numbers |
    |---|---|---|---|
    | `org.atari.asma.sap.Messages` | lib | SAP | all (sent by lib and tools) |
    | `org.atari.asma.demozoo.Messages` | lib | DMZ | all (sent by lib and tools) |
    | `org.atari.asma.Messages` | lib | ASA | 001-299 (also sent by tools, e.g. the scanner's ASA-011) |
    | `org.atari.asma.exporter.Messages` | tools | ASA | 301-399 |
    | `org.atari.asma.editor.Messages` | tools | ASA | 401-499 |
    | `org.atari.asma.browser.Messages` | tools | ASA | 501-599 |

    - Each class gets a `.properties` file.
    - `module-info` must `opens` the packages for NLS: `sap` and `demozoo`
      in asma-lib (`org.atari.asma` is already open), `exporter` and
      `editor` in asma-tools (`browser` is already open).
    - A class that sends from two of these classes qualifies one of them,
      e.g. `ASMAHTMLExporter` (ASA and DMZ).
    - Base does not guarantee that the ASA ranges stay apart, so an ASMA
      test loads all `Messages` classes and fails on a duplicate
      identifier.
- **M5 - Resolving the duplicates.** They are resolved within the M4
  numbering, and the HTML exporter checks Message constants instead of
  strings:
  - "Invalid SAP file. {0}" (the second SAP-006) becomes **SAP-027**;
  - "invalid character in the file name" (the second SAP-103) becomes
    **ASA-012**;
  - "download URL contains non-existing file path" (the second DMO-006)
    becomes **DMZ-010**. This fixes the stray "Edit Download Links" link.
- **M6 - The texts.** Texts are kept verbatim, with the dynamic parts as
  `{0}` parameters, so the issues report reads the same.
  - *(recommended)* The obvious typos are fixed on the way:
    "Stronly" becomes "Strongly", "Convertered" becomes "Converted", and the
    missing space in "of group\"" is added.

### Consequences

- asma-lib public API (this is why the phase must come before v1.0.0):
  - `FileInfo.getMessageQueue()` and `Production.getMessageQueue()` return
    the Base `MessageQueue`.
  - The `check...` methods of `FileInfoList`, `ComposerList`, `GroupList`
    and `ASMAProductionList` take it as a parameter.
  - `org.atari.asma.util.MessageQueue`, `MessageQueueFactory` and
    `BufferedMessageQueue` are deleted.
- The console format changes slightly: "ERROR:   SAP-107 - ..." becomes
  "ERROR: SAP-107 - ...", Base `Console` style.
- About 100 messages to write, and 118 call sites to adapt. Mechanical once
  M1-M6 are decided, but large.

### Steps

1. **WUDSN Base** (M1, M2):
   - Add `WARNING` with the `W` prefix, and support it in `Console` and
     `StatusBar`.
   - Add to `MessageQueue`: `getCount`, `contains`, `addAll` and the
     listener.
   - Fix `MessageQueueEntry.toString()`.
   - Tests. Push before ASMA. *(asks before the push)*
2. **asma-lib:**
   - The three lib message classes (SAP, ASA, DMZ) with their properties.
   - Switch the lib senders, and delete the util queue classes.
   - Tests that load every repository class and check that the identifiers
     are unique.
3. **asma-tools:**
   - The `Messages` classes of `exporter` (ASA 3xx) and `editor` (ASA 4xx).
     Renumber `browser.Messages` from ASB to ASA 5xx.
   - Switch the tools senders: SAP in `ASAPFileLogic`, ASA in the
     exporter and the editor, and DMZ in the importer.
   - The console tools with the `Console` listener.
   - `ASMAHTMLExporter` with `contains(Message)`.
   - SAPFileEditor workers with `addAll`, and the `SAPFileDialog` list.
4. **Regression:**
   - `asmadb.js`/`asmadb.json` must stay byte-identical.
   - The issues report may differ only in the date, the M5 renumbering and
     the M6 typos; check with a diff.
   - Review the console output of `ASMAExporter`.
   - SAPFileEditor manual check. Build the RMT port.
5. Commit, and mark phase 2 done. This unblocks plan 04.

## Out of scope

The JSON layer (gson, `ASMAClient`, `JSONWriter`), the website/JS side,
the ASAP dependency structure, and the published data contract.
