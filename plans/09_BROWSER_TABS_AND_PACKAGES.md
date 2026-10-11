# Plan: ASMABrowser tabs, entity subpackages and reusable list dialogs

Written 2026-10-11. Goal, as stated by the user:

- The ASMABrowser stays usable as a **standalone application**.
- Its main window shows the entities in a **tabbed pane**: "Files",
  "Composers" and later "Groups".
- The entity lists are **also available as separate dialogs** that other
  programs can reuse, explicitly the RMT Java port.
- Since there are several classes per entity, **subpackages per entity**.

**Status: P1-P8 confirmed 2026-10-11 as recommended; steps 1-6 executed
2026-10-11.** Deviations from the proposal, decided during implementation:

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
