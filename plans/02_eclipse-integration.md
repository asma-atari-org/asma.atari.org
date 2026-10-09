# Proposal: Eclipse integration after the Maven migration

Written 2026-10-09. Item 2 was implemented the same day: the `astil` profile
exists in `asma-lib/pom.xml`, verified to regenerate `ASTIL.java` byte-identical
to the committed version, and the orphaned `Fusion.launch`/`.externalToolBuilders`
were removed. Items 1 and 3 remain open (item 1 needs a run from Eclipse,
item 3 is a decision). Handles open item 3 of [01_maven-migration.md](01_maven-migration.md):
the launch configurations and the Fusion/ASTIL external builder, which lost their
home when the hand-maintained Eclipse project was replaced by the m2e import.

## Current state

- The modules are imported via m2e; `.project`/`.classpath`/`.settings` are
  generated and gitignored since commit 923d06b.
- `asma-tools/launch` holds five `.launch` files. The two Java ones
  (`ASMAExporter.launch`, `DemozooImporter.launch`) were rewired to project
  `asma-tools`, module `org.atari.asma.tools` and the new class names, but have
  not been run from Eclipse yet. The three `.bat` ones use absolute paths and
  are unaffected by the migration.
- `asma-lib/.externalToolBuilders/Fusion.launch` (regenerates `ASTIL.java` from
  `ASTIL.fu` with `fut.exe`) is orphaned: external builders are registered in
  `.project`, and that file is now m2e-generated and not committed, so the
  builder attachment cannot be versioned anymore.

## Proposal

### 1. Launch configurations: keep the .launch files, verify once (no code change)

Committed `.launch` files remain the right mechanism - they show up in the Run
menu automatically when the containing project is imported. To verify:

1. Run `ASMAExporter.launch` once (it writes `asmadb.js/.json/.html` into
   `public_html/asmadb` from the SVN checkout - the normal refresh, no risk).
2. Run `DemozooImporter.launch` once (slow scraper - optional, verify on the
   next regular Demozoo refresh instead).

If Eclipse complains about the module path, the likely fix is removing the
`MODULE_NAME` attribute so the launcher derives it from the project.

### 2. ASTIL generation: move it into Maven behind a profile (recommended)

Replace the external builder with `exec-maven-plugin` in `asma-lib`, activated
only by an explicit profile so normal builds never need `fut.exe`:

```
mvn -Pastil generate-sources
```

runs, with `fut.executable` defaulting to `C:\jac\system\Windows\Tools\FUT\Fusion\fut.exe`
(overridable via `-Dfut.executable=...`), in `src/main/java/org/atari/asma/stil`:

```
fut.exe -o ASTIL.java -n org.atari.asma.stil ASTIL.fu
```

Benefits: works identically from the command line, Eclipse (Run As > Maven
build) and any future CI; no dependency on workspace state; the recipe is
versioned in the POM. `Fusion.launch` and the `.externalToolBuilders` folder
are then deleted.

Alternative considered: re-attaching the builder manually in Eclipse
(Project Properties > Builders > Import). Rejected because the attachment
lives only in the generated `.project` and silently disappears on the next
clean import - exactly the failure mode that orphaned it now. ASTIL changes
are rare; an explicit on-demand profile fits better than an incremental
builder anyway.

### 3. Housekeeping

- Move `asma-tools/launch/checkout.bat.launch`, `upload.bat.launch` and
  `index.bat.launch` unchanged; they are workspace conveniences for the
  website operation, not build steps. Optionally delete them - both scripts
  run fine from a normal terminal.
- Delete `asma-tools/build/SAPFileEditor.xml` only after deciding whether the
  launch4j exe is still wanted; if it is, a later step could wrap launch4j in
  a Maven profile too (`launch4j-maven-plugin`).

## Effort

Item 1 is a five-minute verification. Item 2 is ~20 lines of POM plus one
test run of the profile. Item 3 is a decision, not work.
