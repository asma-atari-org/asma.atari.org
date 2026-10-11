# Plan: Value sets for hardware and channels

Written 2026-10-12. Goal: the display texts of a file's hardware and
channels come from WUDSN Base `ValueSet`s with texts in a `.properties`
file, instead of being hard-coded in several Java places. This is the
"Check where WUDSN Base `ValueSet`s can be applied" TODO of plan 07,
narrowed to the two clear candidates.

**Status: V1-V5 confirmed 2026-10-12 as recommended.** On V2 the user
added: WUDSN Base should get a `ValueSetSelectionField` next to
`ValueSetField`. It is a multi-selection field that takes and returns an
array of ValueSet instances and has an implicit "All" entry. This is
recorded as a TODO in Base's `ValueSetField`. Once it exists, the browser
filters switch to it, and `FileInfo.Selection` then takes arrays of values
instead of single ones.

**Working rule (inherited): the open decisions below are confirmed with the
user before the non-mechanical steps are executed.**

## What a ValueSet is

- A WUDSN Base enumeration class (`extends ValueSet`).
- **Each value** has an ID, a display text loaded by NLS from a
  `.properties` file (key `<Class>_<ID>`), and a sort key. Values are equal
  only if they are the same instance.
- **The pattern in The!Cart Studio** (e.g. `DisplayMode`): static
  instances, a map by ID, `getValues()` and `getInstance(id)`. The
  `initializeClass(<class>, ValueSets.class)` call loads the texts from one
  shared `ValueSets.properties` of a container class.
- **Base GUI support:** `ValueSetField` is a `JComboBox` of the values,
  without an "All" entry. `AttributeTable` supports ValueSet columns, and
  a ValueSet renders as its text (`toString()`).

## Current state

- **`FileInfo`** (asma-lib) stores two values:
  - `hardware` is a `String`, `"ATARI800"` or `"ATARI2600"` (constants
    `FileInfo.ATARI800`/`ATARI2600`);
  - `channels` is an `int` (1 or 2).

  Both are part of the JSON contract of `asmadb.js`/`asmadb.json`, where
  defaults are omitted. They must not change.
- **The texts are hard-coded in four places:**
  - `FileInfo.getChannelsText()`: "Mono (2)" for the Atari 2600, otherwise
    "Mono (4)" or "Stereo (8)";
  - `FileInfoFilterPanel`: the arrays `HARDWARE_TEXTS` ("All", "Atari 800",
    "Atari 2600") and `CHANNELS_TEXTS` ("All", "Mono (4)", "Stereo (8)"),
    index-aligned with value arrays;
  - `FileInfoListPanel`: the hardware column shows the raw `"ATARI800"`,
    and the channels column uses a custom text renderer on
    `getChannelsText()`;
  - `FileInfoDetailsPanel`: the hardware field shows the raw `"ATARI800"`.
- **`FileInfo.Selection`** filters by `String hardware` (`null` = all) and
  `int channels` (0 = all). A channels filter of 1 also matches the Atari
  2600 files, because they have 1 channel as well.
- **The website** (`file.js`, `asma.js`) has its own copies of these
  texts. It stays as it is.

## Proposal

1. **`org.atari.asma.Hardware extends ValueSet`** (asma-lib):
   - values `ATARI800` and `ATARI2600`, with the IDs equal to the stored
     strings;
   - texts "Atari 800" and "Atari 2600";
   - `getInstance(String)`, where `null` or empty means `ATARI800`, as in
     the JSON defaults.
2. **`org.atari.asma.Channels extends ValueSet`:**
   - values `MONO_2` (TIA), `MONO_4` and `STEREO_8` (POKEY), with the
     texts of `getChannelsText()`;
   - `Channels.of(Hardware, int channels)` maps the stored values to one of
     these.
3. **`FileInfo`** keeps its fields and gets derived getters:
   `getHardware()` returning `Hardware` and `getChannels()` returning
   `Channels`. `getChannelsText()` becomes `getChannels().getText()`.
4. **The texts** live in one `ValueSets.properties` with a container class
   `org.atari.asma.ValueSets`, as in The!Cart Studio, next to
   `DataTypes.properties`. Their keys are `Hardware_ATARI800`,
   `Channels_MONO_4` and so on.
5. **The browser:**
   - the filter combo boxes, the table columns and the details panel show
     the ValueSet texts;
   - the parallel text and value arrays in `FileInfoFilterPanel` disappear.

## Decisions

- **V1 - The model fields.**
  - *(recommended)* `hardware` and `channels` stay a `String` and an `int`,
    so gson and the JSON contract are untouched. The ValueSets are derived
    by getters.
  - The alternative is fields of the ValueSet types with a gson type
    adapter. That is more invasive and puts the contract at risk.
- **V2 - The "All" entry of the filters.** Base's `ValueSetField` has none.
  - *(recommended)* The `FilterPanel` combo box holds "All" (a `Texts`
    entry) followed by the ValueSet values.
  - The alternative is an optional "All" entry in Base's `ValueSetField`.
    That is a Base change for one use.
- **V3 - The channels filter.**
  - *(recommended)* It offers all three values: "Mono (2)", "Mono (4)" and
    "Stereo (8)". `FileInfo.Selection` then takes a `Hardware` and a
    `Channels` (`null` = all) instead of a `String` and an `int`.
  - "Mono (4)" then no longer matches the Atari 2600 files. Today it does,
    because they also store 1 channel.
  - This changes asma-lib's public API (`Selection`). It is not published
    anywhere, so only the browser is affected; `FileInfoSelectionTest` is
    adapted.
  - The alternative keeps the `Selection` as it is and changes only the
    texts.
- **V4 - The table columns.**
  - *(recommended)* The hardware and channels columns return the ValueSets.
    They render as text, and sort by sort key: Atari 800 before 2600, and
    Mono (2), Mono (4), Stereo (8).
  - The custom channels renderer disappears.
  - The data types `FileInfo_Hardware` and `FileInfo_Channels` change to
    the ValueSet classes, as in The!Cart Studio
    (`new DataType(DisplayMode.class)`).
- **V5 - Scope.**
  - *(recommended)* Hardware and channels only.
  - **Not now: the format** (`originalModuleExt`). ASAP already provides
    the descriptions, and a fixed list would need a code change for every
    new ASAP format.
  - **Not now: the composers' country.** It is free text with variants
    such as "United Kingdom (?)" and needs normalized data on the SVN side
    first (plan 08 D2).

## Steps

1. Add `Hardware`, `Channels`, `ValueSets` and `ValueSets.properties`, and
   the `FileInfo` getters, with tests: IDs equal the stored strings, the
   mapping `Channels.of`, the texts and the sort order. *(mechanical once
   decided)*
2. Adapt `FileInfo.Selection` (V3), the filter panel (V2), the list panel
   and the details panel (V4).
3. Regression:
   - `mvn install` and the tests.
   - An export into the scratchpad, where `asmadb.js` and `asmadb.json`
     must be byte-identical to the last export.
   - Check the Files tab: the filters, the columns and their sorting, and
     the details.
4. Commit, and mark the TODO in plan 07 as done.
