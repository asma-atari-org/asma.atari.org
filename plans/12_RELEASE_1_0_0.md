# Plan: Release 1.0.0 of the programs

Written 2026-10-12, reduced the same day. Goal: the first tagged release of
this repository, a GitHub release with the two programs, the SAPFileEditor
and the ASMABrowser. Plan 04 prepared the workflow; plan 06 phase 2 had to
come first and is done.

**Status: R1-R3 confirmed 2026-10-12 as recommended; steps 1-2 executed.
The version resource is `src/main/resources-filtered/.../ASMABrowser.version`,
the only filtered resource folder; its timestamp is UTC.**

**Working rule (inherited): the open decisions below are confirmed with the
user before the non-mechanical steps are executed. Tags and pushes are
asked for separately.**

## JitPack: postponed

Plans 01 and 04 intended to publish `asma-lib` through JitPack with the
same tag. That is postponed, because it would not help anybody today:

- `asma-lib` depends on WUDSN Base since plan 06. WUDSN Base is not
  published anywhere, so a JitPack consumer of `asma-lib` could not resolve
  it.
- A `jitpack.yml` that installs WUDSN Base before the build would let
  JitPack *build* ASMA. The published POM would still point to the
  unpublished WUDSN Base, so consumers would still have to install it
  themselves.
- The only consumer, the RMT port, uses `asma-tools`. That module needs the
  patched ASAP from `maven-repo/`, which JitPack does not serve, so RMT
  builds ASMA locally anyway.
- **Revisit** when somebody needs `asma-lib` without building it. WUDSN
  Base then needs public coordinates that equal its own groupId, e.g.
  through JitPack with a custom domain (`git.wudsn.com`), and its consumers'
  POMs change accordingly. The first version of this plan worked that out.
- The README sentence advertising
  `com.github.asma-atari-org.asma.atari.org:asma-lib:<tag>` is removed,
  because that coordinate does not work.

## Current state

- `release.yml` (tag `v*`):
  - checks out and installs WUDSN Base;
  - runs `mvn -B clean verify`;
  - attaches `SAPFileEditor.jar` and `SAPFileEditor.zip` (jar plus `.bat`
    starter, `src/assembly/SAPFileEditor-zip.xml`) to the GitHub release.
- The build also produces `ASMABrowser.jar` (shade execution
  `asma-browser`), but it is not attached.
- **ASMABrowser version:** WUDSN Base's `Application` reads the version from
  the resource `org/atari/asma/browser/ASMABrowser.version` (format
  "YYYY-MM-DD HH:MM:SS"). No build creates it, so About shows
  "????-??-?? ??:??:??".
- **Update check:** the URL passed to `Application.createInstance`
  (`https://asma.atari.org/bin/ASMABrowser.zip`) is only used by
  `checkForUpdate`. The ASMABrowser never calls it.
- The website offers `public_html/bin/SAPFileEditor.zip` as a download.
- The README's release ritual is: `versions:set 1.0.0`, commit, tag
  `v1.0.0`, push, `versions:set 1.1.0-SNAPSHOT`, push.

## Decisions

- **R1 - The ASMABrowser in the release.**
  - *(recommended)* Add `ASMABrowser.zip`: the jar plus an
    `ASMABrowser.bat` starter, built like the SAPFileEditor's.
  - `release.yml` attaches `ASMABrowser.jar` and `ASMABrowser.zip` next to
    the SAPFileEditor files.
- **R2 - The ASMABrowser version.**
  - *(recommended)* The build writes `ASMABrowser.version` with the build
    timestamp in the format WUDSN Base expects ("YYYY-MM-DD HH:MM:SS",
    UTC), using a filtered resource and `maven.build.timestamp`. About then
    shows the build date.
  - The Maven version (1.0.0) is not usable there, because WUDSN Base
    compares versions as strings in date format.
- **R3 - Website download and update check.**
  - *(recommended)* Copy `ASMABrowser.zip` to `public_html/bin/` with the
    release, like `SAPFileEditor.zip`, so the website offers it.
  - Leave the update check off for now. It would need a `checkForUpdate`
    call at startup and is a feature of its own.

## Steps

1. Add the `ASMABrowser.zip` assembly and starter, and the version resource
   (R1, R2). Check the About dialog and the zip content locally.
   *(mechanical once decided)*
2. Add the ASMABrowser files to `release.yml`. Remove the JitPack sentence
   from the README, mention the ASMABrowser in the Build and Release
   sections, and note in plans 01 and 04 that JitPack is postponed.
3. Commit and push. The build workflow checks it.
4. Release *(asks before tagging and pushing)*:
   - `mvn versions:set -DnewVersion=1.0.0`, commit "Release 1.0.0".
   - Tag `v1.0.0` and push `main` and the tag.
   - Check the GitHub release and its four files.
   - `mvn versions:set -DnewVersion=1.1.0-SNAPSHOT`, commit, push.
5. Copy `SAPFileEditor.zip` and `ASMABrowser.zip` from the release build to
   `public_html/bin/` (R3), commit, and upload with the usual website
   upload.

## Out of scope

- JitPack and Maven Central (see above and plan 01).
- A published ASAP build (plan 01).
- The update check of the ASMABrowser (R3).
- The website's own download page for the programs (plan 04, step 5).
