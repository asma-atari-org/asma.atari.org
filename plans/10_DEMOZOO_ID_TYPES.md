# Plan: Typed Demozoo IDs and fields

Written 2026-10-11. Goal: replace the generic `DemozooID` with its `Kind`
attribute (plan 09, section 5) by an abstract `DemozooID` with one concrete
subclass per Demozoo entity, plus one specialized display field per
subclass. Demozoo IDs of different types are never mixed, so the compiler
rather than a runtime check ensures that a composer's details panel only
gets a scener ID.

**Status: proposal. Revised 2026-10-11: specialized field classes instead of
a `Type` object or a class token (generics are erased, so the field needs
a runtime carrier of the kind-level behavior when there is no ID; the field
subclass is that carrier). D1 resolved by the revision; D2-D4 confirmed
2026-10-11 as recommended. Steps 1-5 executed 2026-10-11. The table columns
convert with the static `DemozooID.toInteger(DemozooID)`; IDs of 0 or below
now show as empty cells, also in the file list.**

**Working rule (inherited from plans 06-09): the open decisions below are
confirmed with the user before the non-mechanical steps are executed.**

## Current state

- `org.atari.asma.demozoo.DemozooID` (asma-lib): a final class with
  `Kind kind` and `int id`.
  - `Kind` (SCENER, GROUP, MUSIC) carries the kind-level behavior that is
    needed *without* an ID: `getFindURL(query)` and `getCreateURL()`.
  - `getURL()` and `equals` switch over or compare the kind.
  - `of(Kind, int)` and `parse(Kind, String)` return `null` for no ID.
- `DemozooIDField` (asma-tools, `browser`): one class constructed with a
  `Kind`.
  - `setDemozooID(DemozooID, searchText)` checks at runtime that the ID's
    kind is the field's kind.
  - It picks the find/create link texts with a `switch` over the kind.
- Callers:
  - `DetailsPanel.addDemozooIDField(DataType, Kind)`.
  - `ComposerDetailsPanel` (SCENER), `GroupDetailsPanel` (GROUP) and
    `FileInfoDetailsPanel` (MUSIC).
  - `DemozooIDTest`.
- The model classes keep the raw JSON values: `Composer.demozooID` and
  `Group.demozooID` are strings, and `FileInfo.getDemozooID()` is an int.
  `ComposerListPanel` and `GroupListPanel` each have their own
  `parseDemozooID(String)` that returns an `Integer` for the sortable
  table column.

## Proposal

### asma-lib: ID classes (`org.atari.asma.demozoo`)

1. **`DemozooID`, abstract and sealed:**

   ```java
   public abstract sealed class DemozooID
           permits ScenerDemozooID, GroupDemozooID, MusicDemozooID {
       private final int id;                  // always > 0
       protected DemozooID(int id) { ... }
       public final int getID();
       public abstract String getURL();
       equals: same class and same id; hashCode; toString(): the plain ID
   }
   ```

   There is no `Kind`, no `Type` and no `getKind()`.

2. **Concrete final subclasses**, which hold their own kind-level behavior
   as static members:

   ```java
   public final class ScenerDemozooID extends DemozooID {
       public static ScenerDemozooID of(int id);        // null if id <= 0
       public static ScenerDemozooID parse(String text); // null if blank/invalid
       public static String getFindURL(String query);   // DemozooLink.getFind(query, "scener")
       public static String getCreateURL();             // https://demozoo.org/sceners/new/
       public String getURL() { return DemozooLink.getScener(getID()); }
   }
   ```

   `GroupDemozooID` and `MusicDemozooID` follow the same pattern.
   `DemozooLink` stays the one place that builds the URLs.

### asma-tools: field classes (`org.atari.asma.browser`)

3. **`DemozooIDField<T extends DemozooID>`, abstract:** the shared part of
   today's field.
   - It renders the links, opens the browser and reports E006.
   - It provides `clear()` and `setDemozooID(T, searchText)`.
   - It declares the kind-level hooks without an ID:

     ```java
     protected abstract String getFindText();
     protected abstract String getFindURL(String searchText);
     protected abstract String getCreateText();
     protected abstract String getCreateURL();
     ```

   The runtime kind check and both `switch` statements are removed.

4. **Concrete final fields:**
   - `ScenerDemozooIDField extends DemozooIDField<ScenerDemozooID>`
   - `GroupDemozooIDField extends DemozooIDField<GroupDemozooID>`
   - `MusicDemozooIDField extends DemozooIDField<MusicDemozooID>`

   Each one is a few lines: its `Texts` constants and the static methods of
   its ID class. All three stay in `browser` next to the base class: the
   music field is used by `file`, and the others may be reused across the
   entity packages (e.g. groups listing composer links later).

5. **`DetailsPanel`:** `addDemozooIDField(DataType, Kind)` becomes generic,
   adding a field the caller created and returning it:

   ```java
   protected final <F extends DemozooIDField<?>> F addDemozooIDField(DataType dataType, F field)
   ```

   A details panel then reads:

   ```java
   private final ScenerDemozooIDField demozooIDField;
   demozooIDField = addDemozooIDField(DataTypes.Composer_DemozooID, new ScenerDemozooIDField());
   demozooIDField.setDemozooID(ScenerDemozooID.parse(composer.demozooID), composer.getDisplayName());
   ```

   Passing a `GroupDemozooID` there no longer compiles.

6. **Tests:** rewrite `DemozooIDTest` for the subclasses.
   - Keep the URL, find, create and null cases.
   - Add: `ScenerDemozooID.of(1)` is not equal to `GroupDemozooID.of(1)`.

## Decisions

- **D1 - Find/Create texts:** resolved. Each field subclass returns its own
  `Texts` constants.
- **D2 - Typed getters on the model classes:**
  - *(recommended)* Add `Composer.getDemozooID()` returning
    `ScenerDemozooID`, `Group.getDemozooID()` returning `GroupDemozooID`,
    and `FileInfo.getMusicDemozooID()` returning `MusicDemozooID`.
  - The JSON fields stay raw strings and ints, so Gson and the website
    format are unchanged.
  - The details and list panels use the getters, and the two duplicated
    `parseDemozooID` helpers disappear.
  - `FileInfo.getDemozooID()` stays an int, because the exporter and the
    scanner use it. The FileInfo TODO "one value per song" remains open.
- **D3 - Table columns:** *(recommended)* the Demozoo ID columns keep
  `Integer` as their data type and get the value via `getID()`, so sorting
  and `PlainNumberCellRenderer` stay as they are. Clickable link cells are
  a separate feature.
- **D4 - `ASMAHTMLExporter`:** *(recommended)* leave its direct
  `DemozooLink` calls unchanged in this plan.

## Steps

1. Create the sealed `DemozooID` and its three subclasses, delete `Kind`,
   and rewrite `DemozooIDTest`. *(mechanical)*
2. Turn `DemozooIDField` into the abstract generic base, create the three
   field subclasses, and adapt `DetailsPanel` and the three details panels.
   *(mechanical)*
3. If D2 is approved: add the typed getters and switch the details and list
   panels to them, with tests in `ComposerTest` and `GroupSelectionTest`.
4. Run `mvn -q install` and the tests.
   - Check the details panels with the scratch `DetailsCheck`:
     Duesterhoeft_Stephan (sceners/43590), Top_Odin (groups/52101), and a
     group without an ID, which offers only Create.
   - Build the RMT Java port against the new asma-tools.
5. Commit.
