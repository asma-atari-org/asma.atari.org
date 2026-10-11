# Plan: Composer selection structure and filter fields

Written 2026-10-11. Goal: the composer list dialog of the ASMABrowser gets
filter fields above its table, backed by a `Composer.Selection` record in
asma-lib - the same structure the file info list already has with
`FileInfo.Selection` and `FileInfoFilterPanel` (plan 07).

**Status: decisions D1-D5 confirmed 2026-10-11 as recommended (combined
Name field, Country as text field, no file count filter, shared
`FilterPanel<S>`, filters reset on reopening); D6 rejected. R2-R3
confirmed as proposed, R1 revised (one package). Steps 2-6 executed 2026-10-11; the choose mode has no
caller yet (it is for the future submission tool).**

**Working rule (inherited from plans 06/07): the open decisions below are
confirmed with the user before the non-mechanical steps are executed.**

## Current state

- `FileInfo.Selection` (asma-lib): immutable record of filter values,
  normalized to lower case in the compact constructor, with
  `matches(FileInfo)` and the constant `ALL`. Tested by
  `FileInfoSelectionTest`.
- `FileInfoFilterPanel` (asma-tools): a `FlowLayout` row of labelled text
  fields and combo boxes, created via `ElementFactory.createLabel(DataType,
  field)`; every change fires the current selection to a
  `Consumer<FileInfo.Selection>`; a count label shows "n of m files".
- `ComposerListDialog`: OK-only `ModalDialog`, `ComposerListPanel` in
  `CENTER`, 900x550, receives the full composer list and the file counts per
  folder name. No filtering.
- Composer data: 314 composers; fields `lastName`, `firstName`,
  `lastNameUnicode`, `firstNameUnicode`, `handles` (comma separated),
  `country` (26 distinct free-text values, including variants such as
  "United Kingdom", "United Kingdom (?)", "United Kingdom (England)"),
  `demozooID`, `groups`, `folderName`. The file count is derived.

## Design

1. **`Composer.Selection`** (asma-lib, nested record in `Composer`, like
   `FileInfo.Selection`):

   ```java
   public static record Selection(String keyword, String name, String handles,
           String country, String groups) {
       public static final Selection ALL = new Selection("", "", "", "", "");
       public boolean matches(Composer composer) { ... }
   }
   ```

   - All values normalized to lower case (`null` -> `""`), matched as
     case-insensitive "contains", like the file info selection.
   - `name` matches the first and last name, each in its plain and its
     Unicode spelling, so "Badkowski" and "Bądkowski" both find the composer
     (67 composers have a Unicode last name).
   - `keyword` matches any of name (all spellings), handles, country, groups
     and folder name.
   - The file count is not part of the record, since it is not a composer
     field (see decision D3).
2. **DataTypes**: one new entry `ComposerSelection_Keyword` ("&Keyword"),
   analogous to `FileInfoSelection_Keyword`, plus `ComposerSelection_Name`
   ("&Name") if decision D1 picks the combined name field. The other labels
   reuse the existing `Composer_*` data types. `DataTypesTest` count is
   adjusted (21 -> 22 or 23).
3. **`ComposerFilterPanel`** (asma-tools, `org.atari.asma.browser`), shaped
   like `FileInfoFilterPanel`: fields Keyword, Name, Handles, Country,
   Groups, then the count label "n of m composers".
4. **`ComposerListDialog`**: filter panel in the dialog's top area, table in
   `CENTER`; the dialog keeps the full list and passes the filtered list to
   `ComposerListPanel.getTableModel().setComposers(...)` on every change.
   The initial focus goes to the keyword field instead of the table.
5. **Tests**: `ComposerSelectionTest` in asma-lib, covering `ALL`, each field,
   the Unicode spelling, case insensitivity, `null` handling and the
   multi-handle field.

## Open decisions (to be confirmed first)

- **D1 - Name fields**: one combined **Name** field matching first and
  last name (recommended; the table shows them as two columns, but users
  search a person), or separate **Last Name** / **First Name** fields
  mirroring the columns.
- **D2 - Country as a combo box**: a text field (recommended for now; the
  free-text variants like "United Kingdom (?)" make a fixed list
  misleading, and "contains" on "united kingdom" covers all three), or a
  combo box filled with the distinct values of the loaded database. A clean
  combo would want normalized country values, which is a data change on
  the SVN side and fits the ValueSet TODO of plan 07.
- **D3 - File count filter**: none (recommended), or a check box "With
  files only" / a minimum file count. It would need the selection to take
  the count as a parameter of `matches`, or a second predicate in the
  dialog.
- **D4 - Shared filter panel code**: `FileInfoFilterPanel` and the new
  panel would duplicate `addTextField`/`addComboBox`/document listener.
  Options: extract a small package-private abstract `FilterPanel<S>`
  (recommended, mechanical once decided), or accept the duplication, or
  move such a helper to WUDSN Base (later, when a second tool needs it).
- **D5 - Filter state across openings**: the dialog is recreated on each
  opening, so filters reset (recommended, simplest), or the main window
  keeps the last `Composer.Selection` and restores it.
- **D6 - Rejected**: double-clicking a composer does not set the main
  window's Author filter.

## Reuse outside the ASMABrowser (requirement added 2026-10-11)

The composer list will also be used by a future submission tool: composers
submit their files to ASMA, and the submission should carry their composer
details, above all the `folderName`, which places the file in the archive
(`asma/Composers/<folderName>/`). So the list must work without the browser
and must be able to return a chosen composer.

Proposal (to be confirmed):

- **R1 - Package**: revised 2026-10-11 by the user - the file info list
  and the composer list, with their panels and dialogs, both stay in
  `org.atari.asma.browser`; no separate packages. Reuse happens through the
  public classes of that (exported) package. The shared helpers
  `FilterPanel<S>` and `PlainNumberCellRenderer` and the dialog titles in
  `Texts` live there too. (A first version used separate packages
  `org.atari.asma.composer` and `org.atari.asma.gui`; it was reverted.)
- **R2 - Composite panel**: `ComposerSelectionPanel` = filter panel on top,
  list panel below, with `setComposers(List<Composer>, Map<String,Integer>
  fileCounts)` (file counts optional, column shows 0/empty without them)
  and `getSelectedComposer()`.
- **R3 - Dialog modes**: `ComposerListDialog` gets a browse mode (OK only,
  as today, used by the browser) and a choose mode (OK/Cancel, OK enabled
  only with a selected row, double-click confirms, returns the `Composer`
  or `null`), used by the submission tool.
- **Out of scope here**: the submission tool itself and its data format.
  To be recorded there: the submission carries the chosen `Composer`
  (folder name, names in both spellings, handles, country, Demozoo ID), and
  a new composer who is not yet in the list needs a way to enter their
  details instead of choosing.

## Steps

1. Confirm D1-D5 with the user (done) and R1-R3.
2. `Composer.Selection` + `ComposerSelectionTest` (asma-lib).
3. DataTypes entries + properties, `DataTypesTest` count.
4. If D4 = extract: introduce `FilterPanel<S>`, move `FileInfoFilterPanel`
   onto it without behavior change.
5. `ComposerFilterPanel` and the `ComposerListDialog` wiring.
6. Verify: `mvn install` (all tests), launch ASMABrowser, open
   Database > Composers, check each field, the count label, sorting on a
   filtered list, and that labels follow the global UI label rules (no
   colons).
7. Commit with explicit paths; update the status line of this plan.
