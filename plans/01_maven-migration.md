# Plan: Maven migration and reusable ASMA library

Written 2026-10-09. **Executed 2026-10-09** - see "Execution notes" at the end
for the deviations that the actual dependency graph forced. Goal: turn the `java` project into a Maven-based build that
also emits a reusable library for consuming the ASMA website content (JSON,
JavaScript, SAP files) from Java, for use in the Raster Music Tracker project.

## Decision: one repository, two Maven modules

Keep this repository; split the Java code into a Maven multi-module build.
A separate repository for the library buys nothing: once the library is a
Maven artifact, RMT consumes the artifact, not the repository, and a second
repo only adds coordination overhead (two clones, two release processes,
cross-repo version bumps). The module split inside one repo provides the real
benefit: an enforced boundary between "reusable library" and "site machinery".

The one exception is the patched ASAP build — see below.

## Target structure

```
asma.atari.org/                    (this repo)
├─ pom.xml                         parent POM (packaging: pom), org.atari.asma:asma-parent
├─ asma-lib/                       org.atari.asma:asma-lib — THE reusable artifact
│  ├─ pom.xml                      deps: gson only
│  └─ src/main/java/org/atari/asma/...
├─ asma-tools/                     org.atari.asma:asma-tools — everything else
│  ├─ pom.xml                      deps: asma-lib, asap, java.desktop
│  └─ src/main/java/...
├─ public_html/                    unchanged
├─ bat/                            unchanged
└─ ASAP.md, README.md
```

### asma-lib — strictly the content model and the file formats

What RMT needs to consume the website:

- `Composer(List)`, `Group(List)`, `FileInfo(List)`, `ASMADatabase`, `STIL`,
  `ASMALink`, `ASMAPaths`
- the `demozoo.model` package (plain data classes)
- SAP format logic without rendering: `SAPTags`, `Segment(List)`,
  `SegmentListLogic`, `ASAPFileLogic`, `SAPFileList`, the `sap.player`
  definitions, `RMTFile`, `SAPRFile`, `TTTFile`
- `util` minus `FileDrop` (Swing)
- NEW: a client class (`ASMAClient` or similar) that loads the database from
  `https://asma.atari.org/asmadb/asmadb.js` or a local checkout, strips the
  `const asma = ` prefix, and deserializes into the model. Today
  `ASMADatabase` only writes JSON (`JSONWriter`); the read side is the actual
  new work, and gson provides it nearly for free if the model classes stay
  plain.

### asma-tools — site machinery and GUI

Optionally split further into `asma-exporter` and `asma-editor` later; do not
start there.

- `ASMAExporter`, `ASMAHTMLExporter`, `HTMLWriter` — the site generators
- the Demozoo scraper (`DemozooImporter`, `RestClient`, `ZipUtility`, ...)
- the Swing side: `SAPFileEditor`, `ASAPPanel`, `FilePanel`, `SAPFileDialog`,
  `ASAPRenderer`, `FileDrop`
- `SAPRFileLogic` — imports the patched ASAP's dump API. Keeping it out of
  asma-lib keeps the custom `asap.jar` out of RMT's dependency tree. RMT has
  its own ASAP adaptation (see `ASAP.md`) and must not get this one
  transitively.

Each module gets its own `module-info.java`. The current single one requires
`java.desktop` and `asap` transitively, which is exactly what the library
module should shed.

## Parent POM

Two jobs, no code, produces no jar (`<packaging>pom</packaging>`):

1. **Aggregator**: lists the modules; `mvn install` at the root builds
   asma-lib first, then asma-tools against it. Same for test, release, CI,
   and Eclipse import via m2e.
2. **Shared configuration**: Java release, encoding, `dependencyManagement`
   (pins gson etc. in one place so the modules cannot drift to different
   versions), `pluginManagement`, shared groupId and project version.
   Later also the release mechanics: publishing configuration, source- and
   javadoc-jar plugins, license and SCM metadata.

## The ASAP dependency

The real structural problem, already documented in `ASAP.md`: the custom
`asap.jar` is built from uncommitted edits in a clone that has since been
fast-forwarded six months. For Maven the jar is also awkward — not in any
repository, and `.classpath` reaches outside the project into
`lib/asap-code/java/asap.jar`.

Plan: give the ASAP fork its own repository (e.g. `asma-atari-org/asap-fork`)
containing the edited `.fu` sources pinned at the upstream revision they were
based on, plus the generated Java sources and a minimal `pom.xml`, so it
builds as `org.atari.asma:asap` with plain `mvn install` — no MSYS2/make for
consumers. That rescues the at-risk work, gives asma-tools a proper Maven
dependency, and decouples "rebuilding ASAP" from "building the website".

Stopgap until then: commit `asap.jar` into this repo and install it via
`maven-install-plugin` in an early build phase. Workable, but leaves the
source-loss risk open.

## How RMT consumes the library

- Path of least resistance: **JitPack**. RMT adds the JitPack repository and
  depends on `com.github.asma-atari-org.asma.atari.org:asma-lib:<tag>` —
  no publishing infrastructure, just git tags.
- GitHub Packages works but forces authentication on every consumer.
- Maven Central is the cleanest end state but requires a verified groupId
  (`io.github.asma-atari-org`, since `atari.org` is not controlled) and
  signing setup — defer until the library API has settled.
- During development, plain `mvn install` into the local repository is enough.

## Additional steps

- Have the exporter additionally write a plain `asmadb.json` next to
  `asmadb.js`. The website keeps its JS file; Java (and other) consumers get
  clean JSON without prefix-stripping.
- Adopt the standard Maven layout (`src/main/java`, `src/test/java`; the
  existing `java/test` content moves there). Let Eclipse import the Maven
  modules via m2e instead of hand-maintained `.classpath`/`.project` files.
  The `launch/` configurations move into the tools module.
- The build must keep producing the same `SAPFileEditor` deliverable as the
  current Eclipse setup.

## Execution notes (2026-10-09)

The migration is done: parent POM at the repository root, modules `asma-lib`
and `asma-tools`, `mvn install` is green including tests, and
`asma-tools/target/SAPFileEditor.jar` is the runnable fat jar (maven-shade
replaces the Eclipse jar-in-jar export; the launch4j config was updated to
pick it up from there). Deviations from the plan above, all forced by the
actual dependency graph:

- **JPMS forbids split packages**, so the tools classes could not stay in
  `org.atari.asma`/`org.atari.asma.sap`. New packages in asma-tools:
  `org.atari.asma.exporter` (ASMAExporter, ASMAHTMLExporter, HTMLWriter,
  DemozooImporter, new FileInfoScanner), `org.atari.asma.asap` (ASAPFile,
  ASAPFileLogic, SAPRFile, SAPRFileLogic, ASAPRenderer),
  `org.atari.asma.asap.player` (the player package) and
  `org.atari.asma.editor` (the Swing GUI plus FileDrop).
- **The `sap.player` package and `SAPRFile` are ASAP-coupled** (ASAPFile,
  PokeyStatesList), so they went to asma-tools, not asma-lib. The lib keeps
  the ASAP-free SAP format logic: SAPTags, Segment(List), SegmentListLogic,
  ByteUtility, SAPFileProcessor, ASAPFileUtility.
- **`FileInfo.readFromSAPFile` and `FileInfoList.scanFolder`** were the only
  ASAP tentacles into the model; both moved into the new
  `org.atari.asma.exporter.FileInfoScanner`. FileInfoList is now a plain list
  with `add`/`getEntries`/`exportToJSON`/`checkFileInfos`.
- **The whole `demozoo` package (scraper included) stayed in asma-lib** - it
  is dependency-clean (JDK + Gson only) and `demozoo.model.Production` is
  coupled to `Demozoo.PlatformDefinition`. Only the `DemozooImporter` main
  class moved to the exporter package.
- **`FileExtension`** was extracted from ASMAExporter into
  `org.atari.asma.FileExtension` (used by lib and tools).
- **The jar in `java/lib` was the plain 8.0.0 release jar**; the patched jar
  with the dump API only existed inside the asap-code clone. The clone's jar
  is now committed as `maven-repo/net/sf/asap/asap/8.0.0-asma/` (the plan's
  stopgap). The fork repository remains an open item, see ASAP.md.
- **New in asma-lib**: `ASMAClient` (loads/parses asmadb.js or asmadb.json
  from URL, file or string, applies the omitted defaults like asma.js does,
  resolves module file URLs) plus `ASMAClientTest`, which also parses the
  real `public_html/asmadb/asmadb.js`.
- **ASMAExporter now also writes `asmadb.json`** next to `asmadb.js`
  (`ASMADatabase.write` split into `writeJS`/`writeJSON`).
- Visibility fixes required by the package split: `STIL.load`,
  `ASMADatabase.getComposerOrGroup`, `SAPTags`, `ByteUtility`, and the
  `XEXFileResult` fields are now public.

Open items:

- ~~Give the ASAP fork its own repository with a Maven build (`ASAP.md`).~~
  Done 2026-10-09, differently than planned: the asap-code clone already was
  a fork (SourceForge remote `u/peterdell/asap`) and the patch had been
  committed there as branch `sapr-dump`. Added branch `asma-8.0.0` (patch
  pinned at the jar's exact base revision plus a Maven build that reproduces
  `net.sf.asap:asap:8.0.0-asma` from committed sources). See the updated
  `ASAP.md`. Both branches are pushed to the SourceForge fork.
- Publish asma-lib via JitPack (tag a release) once the API has settled.
- Eclipse launch configurations and the Fusion/ASTIL builder: proposal in
  [02_eclipse-integration.md](02_eclipse-integration.md).
