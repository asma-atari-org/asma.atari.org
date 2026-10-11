# asma.atari.org - Atari SAP Music Archive

This project contains the sources for the ASMA Website at https://asma.atari.org.<br>
The actual ASMA archive is original in the SVN repository of http://asma.scene.pl.<br>
To build the website, the "asma" folder in the SVN repository must be linked as "public_html/asma".<br>
The link is created by the "bat\checkout.bat" script if it is missing.

## Structure

- `asma-lib` - Maven artifact `org.atari.asma:asma-lib`. Reusable library to consume
  the content of the ASMA website from Java: the `ASMAClient` loads and parses
  "asmadb.js"/"asmadb.json", the model classes (`FileInfo`, `Composer`, `Group`,
  Demozoo productions) and the SAP/RMT/TTT/STIL format logic. Depends only on Gson.
  Intended for use in other projects, e.g. the Raster Music Tracker Java port.
- `asma-tools` - Maven artifact `org.atari.asma:asma-tools`. The site generation
  tools (`org.atari.asma.exporter.ASMAExporter`, `org.atari.asma.exporter.DemozooImporter`)
  and the SAPFileEditor GUI (`org.atari.asma.editor.SAPFileEditor`). Depends on
  asma-lib and the patched ASAP build (see `ASAP.md`).
- `maven-repo` - file-based Maven repository holding the patched `net.sf.asap:asap`
  jar until the ASAP fork gets its own repository and build.
- `public_html` - the website. `public_html/asma` is the link into the SVN checkout.
- `bat` - checkout and FTP upload scripts.
- `docs` - reference material (ASAP release sources and notes).
- `plans` - implementation plans.

## Build

```
mvn install
```

builds both modules, runs the tests, and produces the runnable fat jars of the two
programs in `asma-tools/target`: `SAPFileEditor.jar` (the SAP file editor) and
`ASMABrowser.jar` (the database browser and player). `SAPFileEditor.zip` and
`ASMABrowser.zip` contain the jar plus a Windows starter script and are the
distributable downloads. Building requires WUDSN Base in the local Maven repository:
clone https://github.com/wudsn/wudsn-base and run `mvn install` there first.

The `ASMAExporter` generates `public_html/asmadb/asmadb.js` (for the website),
`asmadb.json` (plain JSON for API consumers) and `asmadb.html` from the SVN checkout.

## Release

Pushing a tag `v*` triggers `.github/workflows/release.yml`, which builds, tests and
publishes a GitHub release with the jars and zips of both programs attached. After the
release, copy both zips to `public_html/bin/` for the website download.
The library `asma-lib` is not published to a Maven repository yet, see
`plans/12_RELEASE_1_0_0.md`.

Release ritual:

```
mvn versions:set -DnewVersion=1.0.0 && git commit -am "Release 1.0.0"
git tag v1.0.0 && git push origin main v1.0.0
mvn versions:set -DnewVersion=1.1.0-SNAPSHOT && git commit -am "Start 1.1.0-SNAPSHOT"
git push
```

Pushes to `main` are built and tested by `.github/workflows/build.yml`,
including the JavaScript regression tests in `js-test` (Node.js LTS,
built-in test runner): `node --test "js-test/*.test.js"`.

Folder "examples"
The example files from https://asap.sourceforge.net/examples.zip
