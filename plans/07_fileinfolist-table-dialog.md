# Plan: FileInfoList table panel in a dialog (The!Cart Studio style)

Written 2026-10-10. Goal: a dialog in the SAPFileEditor that displays the
ASMA `FileInfoList` in a column-configurable, sortable table built on WUDSN
Base's `AttributeTable` machinery - the same pattern The!Cart Studio uses
for its workbook entries list. This doubles as the pilot for plan 06
phase 3 (editor GUI onto base). Status: open.

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
4. **`org.atari.asma.editor.FileInfoListDialog`** on base's `ModalDialog`
   (the OK-only variant exists since 2026-10), hosting the panel, opened
   from a new SAPFileEditor button/menu entry "Browse ASMA Database...".
5. **Data source**: load via `ASMAClient` - default the local generated
   database (`public_html/asmadb/asmadb.js` relative to the repository),
   with fallback/option `https://asma.atari.org/asmadb/asmadb.json`.
   Loading errors go to the existing message queue display.

## Open decisions (ask before implementing)

- Placement of `DataTypes`/`FileInfo.Attributes` in asma-lib (family
  pattern, entity-near; makes asma-lib's NLS resources mandatory) versus
  keeping asma-lib free of repository classes and putting both into
  asma-tools next to the panel.
- `AttributeTablePreferences` wants an `ApplicationSettings`; the
  SAPFileEditor has no base `Application` wiring yet. Minimal settings
  wiring now, or defaults without persistence until plan 06 phase 3?
- Row interaction: selection/double-click action (play the file? copy the
  URL? open in browser player?) or display-only for the first draft.
- German `DataTypes_de.properties` now or later.

## Steps

1. DataTypes + properties + Attributes (mechanical once placement is
   decided), unit-testable via NLS load in asma-lib tests.
2. Panel + table model + dialog + editor menu entry (asma-tools).
3. Wire data source via ASMAClient; manual test against the local
   database (6405 rows - also a size sanity check for the table).
4. Optional polish: column chooser defaults, preferences persistence,
   row action.
