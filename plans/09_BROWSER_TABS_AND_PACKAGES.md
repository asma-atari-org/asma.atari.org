# Plan: ASMABrowser tabs, entity subpackages and reusable list dialogs

Written 2026-10-11. Goal, as stated by the user:

- The ASMABrowser stays usable as a **standalone application**.
- Its main window shows the entities in a **tabbed pane**: "Files",
  "Composers" and later "Groups".
- The entity lists are **also available as separate dialogs** that other
  programs can reuse, explicitly the RMT Java port.
- Since there are several classes per entity, **subpackages per entity**.

**Status: P1-P8 confirmed 2026-10-11 as recommended; steps 1-6 executed
2026-10-11.** **Section 5 (details panels for composers and groups,
STIL) added 2026-10-11; decided and phase A executed 2026-10-11, see there.** Deviations from the proposal, decided during implementation:

- Tabs have no mnemonics: Alt+F belongs to the File menu, and the filter
  labels of the tabs already use most other letters (e.g. C for Channels
  and Country). Ctrl+Tab / Ctrl+PageDown switch tabs.
- The Files tab and the file dialog share one more class,
  `FileInfoPlayerPanel` (details + replay controls + `FileInfoSelectionPanel`),
  with `ContentLoader` and `ErrorHandler` callbacks: the browser loads from
  the local archive or the website and reports to the status bar, the
  dialog downloads via its `ASMAClient` and reports in a message box.
- `ReplayControls.dispose()` stops playing and the refresh timer when a
  file dialog closes.
- Verified: `mvn install` (all tests), the browser with both tabs, and a
  classpath-only check like RMT's (`ComposerSelectionPanel`/
  `FileInfoPlayerPanel` filled from the local `asmadb.json`: 305 composers,
  6407 files). The `choose` dialogs were compiled against but not clicked
  through.

**Groups (P7 follow-up, executed 2026-10-11 on request):** subpackage
`browser.group` with `GroupFilterPanel` (Keyword, Name),
`GroupListPanel` (Folder Name, Name, Composers, File Count, Demozoo ID),
`GroupSelectionPanel` and `GroupListDialog` (`browse`/`choose`), plus the
third tab "Groups". asma-lib: `Group.Selection`, `Group.Attributes`, six
`Group*` data types, `Database.getFileCountByGroupFolderName()` and
`getComposersByGroupFolderName()` (from the composers' `groups` field),
tests. Fixed on the way: `Group.handle` never matched the JSON attribute
`name` of `Docs/ASMA-Groups.json`, so group names were never read (and the
GRP-002 check never ran); the field is now `name`. The exporter writes the
groups JSON verbatim, so its output is unaffected. Data note: 4 of the 9
groups have no composer whose `groups` field names them.

## Current state (after plan 08)

All 15 classes are flat in `org.atari.asma.browser`:

| Entity / role | Classes |
|---|---|
| Application | `ASMABrowser`, `Actions`, `Messages`, `Texts` (+ icons, properties) |
| Shared helpers | `FilterPanel<S>`, `PlainNumberCellRenderer` |
| Files | `FileInfoFilterPanel`, `FileInfoListPanel`, `FileInfoDetailsPanel`, `ReplayControls`, `ASAPPlayer` |
| Composers | `ComposerFilterPanel`, `ComposerListPanel`, `ComposerSelectionPanel`, `ComposerListDialog` |

- The main window: details panel with replay controls and the file filter
  on top, the file table in the center, the status bar at the bottom.
  Composers open as a dialog from Database > Composers.
- `ASMABrowser.getFileCountByComposerFolderName()` computes the composer
  file counts from the file list.
- The database (`ASMAClient.Database`) already contains `groupInfos`, so a
  Groups tab has its data.

## Proposal

### 1. Package structure

```
org.atari.asma.browser            ASMABrowser (main window), Actions, Messages,
                                  Texts, FilterPanel<S>, PlainNumberCellRenderer
org.atari.asma.browser.file       FileInfoFilterPanel, FileInfoListPanel,
                                  FileInfoDetailsPanel, FileInfoSelectionPanel (new),
                                  FileInfoPlayerPanel (new), FileInfoListDialog (new),
                                  ReplayControls, ASAPPlayer
org.atari.asma.browser.composer   ComposerFilterPanel, ComposerListPanel,
                                  ComposerSelectionPanel, ComposerListDialog
org.atari.asma.browser.group      (later) GroupFilterPanel, GroupListPanel,
                                  GroupSelectionPanel, GroupListDialog
```

- The pattern per entity is the same: `<Entity>FilterPanel`,
  `<Entity>ListPanel` (attribute table), `<Entity>SelectionPanel` (filter +
  list, the reusable unit used by both the tab and the dialog),
  `<Entity>ListDialog` (`browse` and `choose` modes).
- The shared base classes stay in the parent package.
- The NLS repositories `Actions`, `Messages` (area `ASB`), `Texts` stay
  in the parent package, one per kind, and the subpackages import them.
  This keeps a single message area and a single place for labels.
- `module-info`: export each subpackage (for reuse). `opens` is needed
  only where NLS classes live, i.e. the parent package, as today.

### 2. Main window with a tabbed pane

```
+-----------------------------------------------------------------+
| File  Database  Help                                            |
+-----------------------------------------------------------------+
| [ Files ] [ Composers ] [ Groups (later) ]                      |
| +-------------------------------------------------------------+ |
| | Files tab:                                                  | |
| |   details panel + replay controls                           | |
| |   filter row ............................ n of m files      | |
| |   file table                                                | |
| | Composers tab:                                              | |
| |   filter row ............................ n of m composers  | |
| |   composer table                                            | |
| +-------------------------------------------------------------+ |
| status bar                                                      |
+-----------------------------------------------------------------+
```

- Tab titles from `Texts` ("Files", "Composers", "Groups"), no colons,
  no mnemonics (see the status above).
- Each tab hosts the entity's `SelectionPanel`; the Files tab adds the
  details panel and replay controls above it.
- Menu Database > Composers is removed: the tab replaces it.
- The menu entries Load from Website / Load from Local File... and the
  status bar stay global; loading fills all tabs.
- Escape still stops playing, window-wide.

### 3. Reusable dialogs

- `FileInfoListDialog.browse(...)` / `choose(...)` (new) and the existing
  `ComposerListDialog.browse(...)` / `choose(...)`; `choose` returns the
  chosen entity or `null`.
- The dialogs take the loaded `ASMAClient.Database` (instead of separate
  lists and count maps), so a caller loads once via `ASMAClient` and
  passes it to any dialog.
- The composer file count moves from `ASMABrowser` to asma-lib, e.g.
  `ASMAClient.Database.getFileCountByComposerFolderName()`, with a test.
- The file dialog contains the details panel and the replay controls as
  well, so a user can listen before choosing.

### 4. Reuse in RMT (the Java port)

"RMT" means the Java port only (clarified by the user 2026-10-11):
`C:\jac\system\Java\Programming\Repositories\Tools\RASTER-Music-Tracker`,
Maven `org.atari.raster:org.atari.raster.rmt:1.0.0-SNAPSHOT`, Java 21, on
WUDSN Base (`com.wudsn.tools.base`, `.atari`), classpath (no
`module-info`), main class `org.atari.raster.rmt.ui.RmtApplication`. It
has Tools > Open ASMA today, which only opens the website.

So the dialogs are called in-process - no command line, no JSON:

- RMT adds a Maven dependency on `org.atari.asma:asma-tools` (which brings
  `asma-lib`, gson and ASAP); both projects already share WUDSN Base and
  Java 21.
- RMT loads the database once with `new ASMAClient().loadDatabase()` and
  opens e.g. `ComposerListDialog.choose(frame, preferences, database)`,
  getting a `Composer` (with `folderName`) back, or
  `FileInfoListDialog.choose(...)` for a module, whose file it gets via
  `ASMAClient.downloadFile`.
- RMT running on the classpath is fine: the `opens` in `module-info` only
  matter on the module path.
- The RMT side (menu entries, what it does with the result) is a plan in
  the RMT repository, not here; this plan only provides a stable API.

Points to watch:

- **Shaded jar**: RMT's shade plugin merges its dependencies, so
  `asma-tools`, `asma-lib`, gson and ASAP end up in `Rmt.jar`. The
  asma-tools module jar must not itself be a shaded jar (it is not: the
  shaded `ASMABrowser.jar`/`SAPFileEditor.jar` are separate outputs).
- **ASAP**: RMT vendors `lib/java/asap.jar` (8.0.0, not in its POM). If
  it adds ASAP via Maven later, both projects should use the same
  artifact (`net.sf.asap:asap:8.0.0-asma`), otherwise the classes clash.
- **Repository**: asma-tools must be resolvable for RMT - `mvn install`
  locally for now; JitPack/GitHub releases per plan 04 later.
- **API scope**: the reusable surface is the `browser.file` and
  `browser.composer` dialogs and selection panels, plus `ASMAClient`;
  `ASMABrowser` itself stays the standalone application.

### 5. Details panels for composers and groups (added 2026-10-11)

Requirement (user, 2026-10-11): the Composers and Groups tabs also get a
details panel, like the Files tab, where the display and later the editing
of STIL information will be added.

**Decisions (user, 2026-10-11):**

- STIL reading and parsing will later become part of **ASAP**, ideally
  written in **Fusion** so it can be reused - a separate plan. Until
  then the browser only has a stub: `STILSource` (browser package) with
  `STILSource.NONE`; the existing `org.atari.asma.STIL` stays the
  exporter's parser for now. This replaces S3 and phase B below.
- S1: a separate composite panel per entity, as for files:
  `ComposerBrowsePanel` / `GroupBrowsePanel` = details panel + selection
  panel, used by the tab and the dialog; the selection panels stay
  details-free.
- S2: the Demozoo ID gets its own data type class and GUI field, used in
  all details panels (files too), opening the browser with the URLs of
  the website's JS.

**Executed 2026-10-11 (phase A):**

- asma-lib: `org.atari.asma.demozoo.DemozooID` (kind SCENER/GROUP/MUSIC +
  ID, `parse`, `getURL()`, `Kind.getFindURL()`/`getCreateURL()`), built on
  `DemozooLink` (new: `getGroup`, `getFind`), `DemozooIDTest`;
  `Composer.getDisplayName()` with test. The table columns keep their
  `Integer` data types (numeric sorting).
- browser: `DetailsPanel` base class (extracted from
  `FileInfoDetailsPanel`, plus labeled full-width rows and the STIL area),
  `DemozooIDField` (link buttons, keyboard reachable; without an ID "Find
  .../Create ..." like the website's author details; errors as message
  ASB-006), `STILSource` stub.
- `ComposerDetailsPanel`, `GroupDetailsPanel` (with a read-only "STIL"
  area, empty for now), `ComposerBrowsePanel`, `GroupBrowsePanel`;
  selection listeners on the selection panels; tabs and dialogs use the
  browse panels; the file details use the `DemozooIDField` (kind MUSIC).
- URLs as in the JS: `https://demozoo.org/sceners/<id>`
  (composer.js), `https://demozoo.org/music/<id>` (demozoo.js). The JS
  has no group links; groups use `https://demozoo.org/groups/<id>`, to be
  checked manually (demozoo.org rejects scripted requests with 403).

**Background - STIL in ASMA.** `asma/Docs/STIL.txt` is the "SAP Tune
Information List" (format of the HVSC STIL, UTF-8): entries per file
(`/Composers/Aki/Robots.sap` with TITLE/ARTIST/COMMENT, also per song) and
per folder (`/Composers/Duesterhoeft_Stephan/` with a COMMENT about the
composer - 10 composer folders today, no group folders yet). asma-lib
already parses it (`org.atari.asma.STIL`, `getSTILEntry(path)`), but only
the exporter uses it (`ASMAExporter`, message EXP-001); `asmadb.json`
carries no STIL data, and `FileInfo` has TODOs for STIL comments and
per-song details.

**Phase A - details panels now (no STIL yet):**

- `ComposerDetailsPanel` (`browser.composer`) and `GroupDetailsPanel`
  (`browser.group`), built like `FileInfoDetailsPanel`: read-only fields
  in two label/field column pairs with fixed-width cells, following the
  table selection, empty without a selection.
  - Composer: First Name / Last Name, Handles / Folder Name, Country /
    Demozoo ID, Groups / File Count. Names in the Unicode spelling where
    there is one (as in the table).
  - Group: Name / Folder Name, Composers (full width), Demozoo ID / File
    Count.
- A full-width row at the bottom of each details panel is reserved for
  the STIL area (phase B), via the existing `addFullWidthRow` pattern.
- The details panel sits above the filter row, as in the Files tab, and
  is part of the reusable unit, so the dialogs show it too (see S1).

**Phase B - STIL display (later, separate step):**

- The browser needs STIL data at runtime. `ASMAClient` gets
  `loadSTIL()` / `loadSTIL(File)`, reading `asma/Docs/STIL.txt` from the
  website or from the local archive next to a locally loaded database,
  parsed with the existing `STIL` class (see S3).
- Details panels show the folder entry's COMMENT
  (`/Composers/<folderName>/`, `/Groups/<folderName>/`) in a multi-line,
  read-only text area. The same mechanism later serves the file details
  (the `FileInfo` TODOs: COMMENT, TITLE/ARTIST per song).

**Phase C - STIL editing (later, own plan):**

- Only when the database was loaded from a local file whose archive
  folder is an SVN working copy; editing on website data is not possible.
- Needs a STIL writer that keeps the file's order, the `###` section
  headers, the indentation and the line length limit (see
  `FileInfoList`'s STIL note: tag width 9, values up to 90 characters),
  plus validation; the user commits to SVN as usual.
- Whether editing happens in place in the details panel or in an input
  dialog ("Edit STIL..."), and for which entities, is decided in that
  plan.

## Open decisions

- **P1 - Subpackage names**: `browser.file` / `browser.composer` /
  `browser.group` (recommended, entity names as in the tabs), or
  `browser.fileinfo` to match the class prefix `FileInfo`.
- **P2 - NLS location**: one `Actions`/`Messages`/`Texts` in the parent
  package (recommended), or one set per subpackage (more files, separate
  message areas or shared area numbering).
- **P3 - Database > Composers menu entry**: remove it, the tab replaces it
  (recommended), or keep it as a shortcut that selects the tab.
- **P4 - Dialog input**: pass `ASMAClient.Database` (recommended), or keep
  separate lists and maps per dialog.
- **P5 - File dialog contents**: with details panel and replay controls
  (recommended), or the plain list only. In `choose` mode a double click
  chooses; in the tab a double click plays, as today.
- **P6 - RMT dependency**: RMT depends on `asma-tools` directly
  (recommended; one artifact), or asma-tools is split so that RMT gets
  only the reusable dialogs without the browser application and the
  SAPFileEditor (a further module, e.g. `asma-gui`).
- **P7 - Groups tab**: only reserve the package and the tab order now and
  build it in a separate plan (recommended), or include it here.
- **P8 - Cross-tab navigation** (e.g. a composer's files): not part of
  this plan (recommended; the analogous D6 of plan 08 was rejected).

- **S1 - Unit containing the details panel** (phase A): the
  `ComposerSelectionPanel` / `GroupSelectionPanel` get the details panel
  on top, so tab and dialog show it automatically (recommended, fewer
  classes), or a separate composite per entity like `FileInfoPlayerPanel`
  (keeps a details-free selection panel for compact dialogs).
- **S2 - Demozoo link** (phase A): a button "Open in Demozoo" in the
  composer and group details that opens the scener/group page in the
  browser (recommended; the IDs are there already), or none.
- **S3 - STIL source** (phase B): the client loads `Docs/STIL.txt` itself
  (recommended; no change of the `asmadb.json` contract), or the exporter
  adds the folder comments to `asmadb.json`.

## Steps (after confirmation)

1. Move the classes into the subpackages (mechanical), adjust
   `module-info`, build.
2. `Database.getFileCountByComposerFolderName()` in asma-lib + test.
3. `FileInfoSelectionPanel` (filter + table) and `FileInfoListDialog`;
   move the file-specific wiring out of `ASMABrowser` into it.
4. Main window with the tabbed pane; remove Database > Composers (P3).
5. API check from the RMT side: a small test or scratch class in asma-tools
   that uses only the public dialog API the way RMT would.
6. Verify: `mvn install`, launch the browser (both tabs, loading, replay,
   Escape), open both dialogs in `choose` mode; update plans 07/08 cross references.
7. Commit with explicit paths.

## Steps for section 5 (after confirmation of S1-S3)

1. Phase A: `ComposerDetailsPanel`, `GroupDetailsPanel`, wiring into the
   selection panels (S1), Demozoo button (S2); verify in tabs and dialogs.
2. Phase B: `ASMAClient.loadSTIL`, STIL area in the details panels,
   tests with the real `STIL.txt` (e.g. the Duesterhoeft_Stephan folder
   comment).
3. Phase C: separate plan for STIL editing.
