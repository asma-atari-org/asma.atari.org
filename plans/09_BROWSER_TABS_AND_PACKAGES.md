# Plan: ASMABrowser tabs, entity subpackages and reusable list dialogs

Written 2026-10-11. Goal, as stated by the user:

- The ASMABrowser stays usable as a **standalone application**.
- Its main window shows the entities in a **tabbed pane**: "Files",
  "Composers" and later "Groups".
- The entity lists are **also available as separate dialogs** that other
  programs can reuse, explicitly RMT.
- Since there are several classes per entity, **subpackages per entity**.

**Status: proposal, not started. All decisions below are to be confirmed
first (working rule of plans 06-08).**

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
                                  FileInfoListDialog (new), ReplayControls, ASAPPlayer
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

- Tab titles from `Texts` with mnemonics ("&Files", "&Composers",
  "&Groups"), no colons.
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

### 4. Reuse in RMT

RMT (Raster Music Tracker) is a Windows C++/MFC program, so it cannot
call the Java dialogs directly. A process boundary is needed. Proposal:

- A command-line mode of `ASMABrowser.jar`, e.g.
  `java -jar ASMABrowser.jar --choose composer` (and `--choose file`):
  opens only the chosen dialog, loads the database from the website (or
  `--database <file>`), and on OK prints the chosen entity as one JSON
  object (the gson form of `Composer` / `FileInfo`) to standard output,
  exit code 0; on Cancel no output, exit code 1.
- RMT starts the process, waits, and parses the JSON - e.g. to prefill
  the author and the composer folder name for an ASMA submission (see
  plan 08, "Reuse").
- Without arguments the jar starts the standalone browser as today.

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
- **P6 - RMT integration**: the command-line `--choose` mode with JSON on
  standard output (recommended), or another mechanism (e.g. a file
  written next to the module, or RMT reading `asmadb.json` itself with its
  own C++ dialog).
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
5. Command-line `--choose` mode (P6) with a test of the argument parsing
   and the JSON output.
6. Verify: `mvn install`, launch the browser (both tabs, loading, replay,
   Escape), run `--choose composer` and `--choose file` from a shell and
   check the JSON and exit codes; update plans 07/08 cross references.
7. Commit with explicit paths.
