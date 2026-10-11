# Plan: GitHub releases for the editor and the library

Written 2026-10-10. **Executed 2026-10-10**: steps 1-4 are done (assembly zip,
both workflows, launch4j removed, release ritual in the README). The workflows
run on the next push; the first actual release happens with the first `v*` tag.
Step 5 (website download from the release) stays optional/later. Automate tagged releases on
`github.com/asma-atari-org/asma.atari.org`: a GitHub Release carrying the
SAPFileEditor binaries, and the same tag feeding JitPack for `asma-lib`
(the open publishing item of [01_MAVEN_MIGRATION.md](01_MAVEN_MIGRATION.md)).
Decision from [02_ECLIPSE_INTEGRATION.md](02_ECLIPSE_INTEGRATION.md):
the launch4j `.exe` is not needed in the current process, so the release
ships the fat jar; launch4j is dropped.

## Preconditions (already met)

- The build is self-contained on a clean machine: the patched ASAP resolves
  from the committed `maven-repo/`, gson from Maven Central.
- `mvn -B clean verify` runs the tests in CI too: `ASMAClientTest` reads the
  committed `public_html/asmadb/asmadb.js`.
- `asma-tools/target/SAPFileEditor.jar` is produced by the shade plugin.

## Steps

1. **Zip assembly.** Make the build produce
   `asma-tools/target/SAPFileEditor.zip` containing `SAPFileEditor.jar` plus a
   one-line `SAPFileEditor.bat` starter (`javaw -jar SAPFileEditor.jar`),
   mirroring the current `public_html/bin/SAPFileEditor.zip` download
   (without the launch4j exe). Use `maven-assembly-plugin` in `asma-tools`.

2. **Release workflow.** Add `.github/workflows/release.yml`:
   - Trigger: push of a tag `v*`.
   - Steps: checkout, `actions/setup-java` (Temurin 21, Maven cache),
     `mvn -B clean verify`, then create the GitHub Release with
     `SAPFileEditor.jar` and `SAPFileEditor.zip` attached
     (`softprops/action-gh-release` or `gh release create`).
   - Permissions: `contents: write`; only the automatic `GITHUB_TOKEN`.
   - Optionally a plain `build.yml` running `mvn -B verify` on pushes to
     `main`, so breakage is caught before tagging.

3. **Remove launch4j.** Delete `asma-tools/build/SAPFileEditor.xml` (and the
   then-empty `build` folder). The old exe remains available in
   `asma-tools/out/` locally and inside the previously uploaded
   `public_html/bin/SAPFileEditor.zip` history.

4. **Release ritual** (manual, documented in README):
   - Set the version: `mvn versions:set -DnewVersion=1.0.0` (or edit the three
     POMs), commit.
   - Tag and push: `git tag v1.0.0 && git push origin main v1.0.0`.
   - The workflow publishes the release. (JitPack serving `asma-lib` from
     the same tag is postponed, see plan 12: WUDSN Base, which asma-lib
     depends on since plan 06, is not published.)
   - Back to the next snapshot: `mvn versions:set -DnewVersion=1.1.0-SNAPSHOT`,
     commit.

5. **Website download (optional, later).** `public_html/bin/SAPFileEditor.zip`
   can then either be refreshed from the release artifact or replaced by a
   link to the GitHub release page, removing the binary from the repo.

## Later extension (explicitly out of scope now)

A no-Java-required Windows package via `jpackage` on a `windows-latest`
runner job (app-image zip, or `.msi` - WiX is preinstalled on GitHub
runners). Download size ~50-80 MB. Add only when end users without a Java
runtime actually ask for it.
