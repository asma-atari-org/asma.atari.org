# Plan: Settings persistence for the ASMABrowser

Written 2026-10-11. Goal: the ASMABrowser keeps its user settings across
runs, starting with the table layouts. This is the open item "preferences
persistence via ApplicationSettings" from plan 07 (`TODO plans/07` in
`ASMABrowser`).

**Status: D1-D5 confirmed 2026-10-11 as recommended. Steps 1-4 executed
2026-10-11. wudsn-base commits 5daa1d0 and 3f579ab are local and not yet
pushed; ASMA's CI cannot build until they are. Implementation notes:**

- D1 revised 2026-10-11: instead of the public class
  `SettingsAttributeTablePreferences` with `save()` (described below),
  WUDSN Base has `AttributeTablePreferencesFactory` with `createInMemory()`
  and `createPersistent(section)`.
  - Both return the plain `AttributeTablePreferences` interface; the
    implementations are private.
  - The persistent map reads the section on creation and writes every
    `put`, `remove` and `clear` through to it. Its key and entry views are
    read-only.
  - There is no `save()`: `ApplicationSettings.flush()` on exit stores
    everything.
  - Base also gained `ApplicationSettingsSection.getKeys()` and
    `remove(key)`, and `ApplicationSettings.flush()`.
- The six `getTable()` methods of the selection, browse and player panels
  now return `AttributeTable`, so `saveLayout()` is reachable.
- The window geometry and the selected tab are kept by the package-private
  `ASMABrowserWindowPreferences`, in the `MainWindow` section. The
  coordinates are stored as strings, because they can be negative.
- asma-tools gets its first tests: JUnit was added as a test dependency.
- Verified: closing the browser stores 72 layout values (24 columns times 3
  keys) and the 6 window values under
  `HKCU\Software\JavaSoft\Prefs\org\atari\asma\browser`.

**Working rule (inherited from plans 06-10): the open decisions below are
confirmed with the user before the non-mechanical steps are executed.**

## Current state

- `ASMABrowser` passes an `InMemoryAttributeTablePreferences` (a `HashMap`)
  to the panels of all three tabs. The table prefixes are `fileInfoList`,
  `composerList` and `groupList`.
- WUDSN Base `AttributeTable` behavior:
  - It **reads** the layout once, in its constructor (`loadLayout()`).
  - It **writes** the layout only when `saveLayout()` is called explicitly.
    Nothing in ASMA calls it, so even the in-memory map stays empty and
    column changes are lost on every restart.
  - The layout stores visibility, view index and width per column, under
    `<prefix>.columns.<attribute>.visible`, `.index` and `.width`.
  - The **sort order is not part of the layout**.
  - The!Cart Studio is the reference: on exit it calls
    `mainWindow.setPreferencesFromWindow(...)` and `getTable().saveLayout()`
    per table, then writes its preferences.
- The main window is a plain `JFrame`: size 1000x800 and
  `setLocationByPlatform`, nothing is restored. WUDSN Base offers
  `MainWindow` plus `MainWindowPreferences`.
  - `MainWindow` tracks the normal bounds even while maximized.
  - The RMT port uses it through `RmtWindowPreferences` on
    `java.util.prefs.Preferences`.
- WUDSN Base `ApplicationSettings` / `ApplicationSettingsSection` (used by
  DIS6502):
  - They are named sections under an application root `Preferences` node.
  - A section offers `getString`/`writeString`, `getUnsignedInt`/
    `writeUnsignedInt` and `clear()`.
  - **There is no way to list a section's keys**, which loading a layout
    map needs.
  - `TestSettings` (test scope) provides throw-away nodes.
- The dialogs (`FileInfoListDialog`, `ComposerListDialog` and
  `GroupListDialog`) take an `AttributeTablePreferences` from the caller
  and use the same panel classes, and therefore the same prefixes, as the
  tabs. The RMT port does not call them yet.
- WUDSN Base is consumed as `1.0.0-SNAPSHOT`, which CI builds from
  wudsn/wudsn-base. A change in Base must therefore be pushed there before
  ASMA's CI can build against it.

## Proposal

1. **Settings root:** `new ApplicationSettings(Preferences.userNodeForPackage(ASMABrowser.class))`.
   - On Windows this is
     `HKCU\Software\JavaSoft\Prefs\org\atari\asma\browser`.
   - It is created in the `ASMABrowser` constructor and handed down; there
     is no static singleton.
2. **Table layouts:** a section `AttributeTables` holds the layout map
   (D1 decides how).
   - It is loaded once at startup, before the panels are created, because
     `AttributeTable` reads the layout in its constructor.
   - On exit, `saveLayout()` is called on the three tables, and the map is
     written back and flushed.
3. **Main window:** switch from the plain `JFrame` to WUDSN Base
   `MainWindow`, with a small `MainWindowPreferences` implementation on a
   section `MainWindow`, like `RmtWindowPreferences`.
   - The first run keeps today's 1000x800 size, centered.
   - The saved location is clamped to the screen, like RMT.
4. **Save on exit:**
   - Replace `EXIT_ON_CLOSE` with a `windowClosing` listener: save, stop
     the replay, then dispose and exit.
   - File > Exit already goes through `WINDOW_CLOSING`.
   - A failing backing store is only logged to stderr; it never blocks the
     exit.
5. **Dialogs:**
   - `browse`/`choose` call `getTable().saveLayout()` when the dialog
     closes, so the caller's preferences receive the layout.
   - Writing those preferences to disk stays the caller's job: ASMABrowser
     on exit, RMT later.
   - Because the prefixes are shared, a dialog shows the same layout as the
     corresponding tab (D3).
6. **Tests (asma-tools):** a round trip on a throw-away `Preferences` node:
   - write a layout map, reload it in a new instance, and compare;
   - also check the window preferences, including the clamp.

## Decisions

- **D1 - Where the layout map is stored.** A section cannot list its keys.
  - *(a, recommended)* **Extend WUDSN Base.**
    - Add `ApplicationSettingsSection.getKeys()`.
    - Add a reusable `SettingsAttributeTablePreferences implements
      AttributeTablePreferences`, backed by a section, with `load()` and
      `save()`.
    - The RMT port, DIS6502 and The!Cart Studio could use it as well. It
      requires a commit and push in wudsn-base first.
  - *(b)* **Local to ASMA:** the same class in `org.atari.asma.browser`,
    working directly on `settings.getRoot().node("AttributeTables")`.
    There is no Base change, but every tool keeps its own copy.
  - *(c)* **One serialized string value** (`key=value;...`). This is
    rejected: a `Preferences` value is limited to 8192 characters, and three
    tables with about 25 columns of three keys each come close to it.
- **D2 - Scope of the first step.**
  - *(recommended)* Table layouts, main window bounds and state, and the
    selected tab.
  - Not included: filter values (plan 08 D5: filters reset on reopening),
    dialog sizes, and the sort order. WUDSN Base does not store the sort
    order; that would be a separate Base extension.
- **D3 - Shared or separate layouts for tab and dialog.**
  - *(recommended)* Shared: the same prefix, so a column the user hides in
    the Composers tab is also hidden in the composer dialog.
  - The alternative is separate prefixes such as `composerListDialog`.
    That needs a constructor parameter on the list panels.
- **D4 - Remember the database source.**
  - Today the browser always loads from the website at startup.
  - *(recommended)* Not now. The website is the one source that is always
    there, and this is a behavior change rather than persistence.
  - The option is: remember the last local file and load it at startup if
    it still exists. That would fit with an MRU list (Base `MRUList`) under
    Database later.
- **D5 - Reset.**
  - *(recommended)* No menu entry now. Deleting the registry node resets
    everything.
  - The option is a "Reset Layout" entry under a new View menu, using
    `ApplicationSettingsSection.clear()`. It needs a restart or a rebuild of
    the tables, because the layout is only read in the constructor.

## Steps

1. If D1(a): in wudsn-base, add `getKeys()` and
   `SettingsAttributeTablePreferences` with tests, then install, commit and
   push it there. *(asks before the push)*
2. `ASMABrowser`:
   - Add the settings root and load the layout before creating the panels.
   - Switch to `MainWindow` with `MainWindowPreferences`.
   - Restore the selected tab.
   - Save everything in `windowClosing`.
3. Dialogs: `saveLayout()` on close.
4. Tests, then `mvn -q install`. Manual check:
   - Hide and move columns, resize and move the window, switch the tab,
     exit, and restart. Everything must be restored.
   - Repeat while maximized.
   - Build the RMT port.
5. Commit, and note the result in plan 07 (the TODO is resolved).
