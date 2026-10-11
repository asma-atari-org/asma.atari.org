# Plan: ASMABrowser application with the FileInfoList table panel

Written 2026-10-10. Goal: a standalone application **ASMABrowser** on WUDSN
Base whose main window displays the ASMA `FileInfoList` in a
column-configurable, sortable table built on base's `AttributeTable`
machinery - the same pattern The!Cart Studio uses for its workbook entries
list. The table lives in a reusable panel, which can additionally be shown
in a dialog from the SAPFileEditor. This doubles as the pilot for plan 06
phase 3 (editor GUI onto base).

**Status: steps 1-4 executed 2026-10-10.** Decisions taken: DataTypes and
FileInfo.Attributes live in asma-lib next to the model (family pattern);
a details panel like the web player sits above the table and follows the
selection; no German translation; the browser's message area is `ASB`.
Still open: step 5 (SAPFileEditor dialog variant), preferences persistence
via ApplicationSettings (currently in-memory, marked TODO), and the
optional polish of step 6.

**Working rule (inherited from plan 06): non-mechanical steps and the open
decisions below are proposed and confirmed with the user first.**

## The reference pattern (from The!Cart Studio / WUDSN Base)

- `DataTypes extends NLS`: `public static DataType X = new DataType(String.class)`
  etc., labels loaded fail-fast from `DataTypes.properties` next to the class.
- The entity declares a nested `Attributes` class:
  `public static final Attribute TITLE = new Attribute("title", DataTypes.WorkbookEntry_Title);`
  binding the property name to its data type (see
  `WorkbookEntry.Attributes`).
- The panel extends `AttributeTablePanel` with a nested
  `TableModel extends AttributeTableModel` that declares the columns:
  `addColumn(Attributes.TITLE, Column.FIXED | Column.SORTABLE, StringUtility.CASE_INSENSITIVE_COMPARATOR, ...)`
  (see `WorkbookEntriesPanel`). Flags: `VISIBLE`, `HIDDEN`, `FIXED`,
  `SORTABLE`; users can show/hide columns via
  `AttributeTableColumnChooser`, layout persisted via
  `AttributeTablePreferences`.

## Design for ASMA

1. **`org.atari.asma.DataTypes extends NLS`** (asma-lib, beside the model;
   the base dependency is already wired by plan 06 phase 1) with one
   `DataType` per displayed `FileInfo` property:
   `FileInfo_FilePath`, `_Title`, `_Author`, `_Date` (String),
   `_Hardware`, `_OriginalModuleExt` (String), `_Channels`, `_Songs`,
   `_DefaultSongIndex`, `_DemozooID` (Integer), `_FileSize`
   (`com.wudsn.tools.base.common.MemorySize`). Labels in
   `src/main/resources/org/atari/asma/DataTypes.properties` (first
   resources folder in asma-lib).
2. **`FileInfo.Attributes`** nested class, following the
   `WorkbookEntry.Attributes` pattern: one `Attribute` per column bound to
   the gson field names (`"filePath"`, `"title"`, ...).
3. **`org.atari.asma.editor.FileInfoListPanel extends AttributeTablePanel`**
   (asma-tools) with nested `TableModel extends AttributeTableModel`:
   - Visible: Title (FIXED), Author, Date, Format, Channels, Songs.
   - Hidden but choosable: FilePath, FileSize, Hardware, Default Song,
     Demozoo ID.
   - Case-insensitive comparators for the string columns, like TCS.
   - Row source: `java.util.List<FileInfo>`.
4. **The ASMABrowser application** (`org.atari.asma.browser`, asma-tools),
   shaped like DIS6502's main class (a plain final class, not a subclass -
   base's `Application` is a singleton):
   - `ASMABrowser` main class: `Application.createInstance(url, jarFileName,
     mainClass)` with the asma.atari.org URL for the version/update check,
     `ApplicationSettings` for window state and the table preferences,
     a main window (JFrame) hosting the `FileInfoListPanel`, a `StatusBar`
     with the base `MessageQueue` display.
   - `Actions extends NLS` + `Actions.properties` for the main menu per the
     RULES doc pattern (`ElementFactory`); initially: File > Exit,
     Database > Reload / Load from Website, Help > About.
   - `AREA = "BRW"` messages repository once the application has own
     messages (plan 06 phase 2 conventions).
   - Packaging: a second shade execution in asma-tools producing
     `target/ASMABrowser.jar` beside `SAPFileEditor.jar`; both attach to
     the GitHub release (plan 04 workflow).
5. **`org.atari.asma.editor.FileInfoListDialog`** on base's `ModalDialog`
   (the OK-only variant exists since 2026-10), hosting the same panel,
   opened from a new SAPFileEditor button/menu entry
   "Browse ASMA Database...".
6. **Data source**: load via `ASMAClient` - default the local generated
   database (`public_html/asmadb/asmadb.js` relative to the repository),
   with fallback/option `https://asma.atari.org/asmadb/asmadb.json`.
   Loading errors go to the message queue display.

## Open decisions (ask before implementing)

- Placement of `DataTypes`/`FileInfo.Attributes` in asma-lib (family
  pattern, entity-near; makes asma-lib's NLS resources mandatory) versus
  keeping asma-lib free of repository classes and putting both into
  asma-tools next to the panel.
- Row interaction: selection/double-click action (play the file? copy the
  URL? open in the browser player at `#/<filePath>`?) or display-only for
  the first draft.
- German `DataTypes_de.properties`/`Actions_de.properties` now or later.
- (Resolved by the application decision: `AttributeTablePreferences`
  persists via ASMABrowser's `ApplicationSettings`; the SAPFileEditor
  dialog variant uses defaults until plan 06 phase 3 wires it too.)

## Steps

1. DataTypes + properties + Attributes (mechanical once placement is
   decided), unit-testable via NLS load in asma-lib tests.
2. Panel + table model (asma-tools).
3. ASMABrowser application: main class, Actions/menu, settings, status
   bar, shaded `ASMABrowser.jar`.
4. Wire data source via ASMAClient; manual test against the local
   database (6405 rows - also a size sanity check for the table).
5. SAPFileEditor dialog variant reusing the panel.
6. Optional polish: column chooser defaults, row action, release upload.

## TODO

- **Check where WUDSN Base `ValueSet`s can be applied** (recorded
  2026-10-11). ValueSets are the base library's enumerations with texts
  from the `.properties` files (The!Cart Studio uses them, e.g. for its
  content types). Candidates in the FileInfo model, today plain strings or
  numbers with hand-written texts:
  - the file format (`originalModuleExt`: `rmt`, `cmc`, `tmc`, `ttt`, ...) -
    the example named when recording; its descriptions currently come from
    ASAP's `getExtDescription` or are missing (TIATracker),
  - the hardware (`ATARI800`/`ATARI2600`), whose display texts are
    hard-coded in the filter panel,
  - the channels (1/2), whose texts "Mono (4)"/"Stereo (8)"/"Mono (2)" are
    hard-coded in `FileInfo.getChannelsText()` and the filter panel.
  A ValueSet could replace the hard-coded combo box arrays and texts and
  give the table a proper display per value. Check against the JSON
  contract: the stored values in `asmadb.js` must not change.
