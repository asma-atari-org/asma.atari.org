# Plan: Migrate ASMA onto the WUDSN Base library

Written 2026-10-10. Goal: streamline the ASMA Java code by building on
`com.wudsn.tools.base` (GitHub `wudsn/wudsn-base`), the shared foundation of
DIS6502, RASTER Music Tracker and The!Cart Studio (see its
`plans/RULES_WUDSN_BASE.md`), and thereby make the ASMA code directly
reusable in the RMT context. RMT already depends on
`com.wudsn.tools.base(.atari)` 1.0.0-SNAPSHOT and renders the base
`MessageQueue` in its GUI, so this migration unifies the tool family.

**Working rule: every non-mechanical step is proposed and confirmed with
the user first. Only mechanical 1:1 swaps with identical semantics are
executed directly; anything involving API mismatches, paradigm changes,
public API changes of asma-lib, or data contract decisions stops and asks.**

## Key findings from the analysis

- Overlapping utilities: `FileUtility`, `StringUtility`, `MemoryUtility`
  (base: `MemorySize`), `ByteUtility` (base: `ByteArrayUtility`/`HexUtility`)
  map to base equivalents; `JSONWriter`/`Serializer` (gson) have NO base
  equivalent and stay in asma-lib.
- The `MessageQueue` paradigms differ fundamentally: ASMA uses ad-hoc string
  codes (`sendError("SAP-110", text)`); base uses NLS `Message` repository
  classes with `.properties`, parameters, fail-fast load-time checks and a
  GUI renderer. Migrating is the main work and the main win (ASMA validation
  results become displayable in RMT's message UI).
- `base.atari` holds `SAPFile` (62 lines, header-level), `ExecutableFile`
  (XEX), `AtrFile`, `Platform` - overlap with ASMA's `SAPTags`, `Segment*`,
  XEX handling and `Hardware` strings. Long-term convergence candidate:
  grow `base.atari` from ASMA's deeper format logic (also relevant for RMT's
  SAP-R recording work).
- CI: wudsn-base is not on Maven Central; RMT's workflows solve this by
  checking out `wudsn/wudsn-base` and running `mvn install` first - copy
  that step. JitPack publishing of asma-lib (plan 01) then also needs
  wudsn-base resolvable for JitPack; decide before tagging.
- The `asmadb.js`/`asmadb.json` data contract does not change (e.g. the
  `"ATARI800"` hardware strings stay), and the byte-identical export is the
  regression oracle after every phase.
- Sequencing: migrate BEFORE tagging v1.0.0, because asma-lib's public API
  changes (`MessageQueue` in `FileInfo`/`checkFileInfos` signatures).

## Phases

1. **Groundwork** - DONE 2026-10-10, with a scope correction: the API
   comparison showed the utility classes are name-twins but API-strangers
   (base `FileUtility.readBytes` throws the NLS-coupled `CoreException`;
   base `StringUtility` has none of ASMA's methods; hex formats differ and
   partly feed artifacts). Per the working rule this was escalated; decision:
   **keep all ASMA utility classes untouched until phase 2**, where
   `CoreException`/`Message` arrive as one coherent design. Executed:
   - `com.wudsn.tools:com.wudsn.tools.base:1.0.0-SNAPSHOT` in the parent
     dependencyManagement and in asma-lib/asma-tools.
   - `requires (transitive) com.wudsn.tools.base;` in both module-infos
     (automatic module).
   - CI: checkout `wudsn/wudsn-base` + `mvn install` before the build in
     `build.yml` and `release.yml` (RMT's proven pattern).
   - `mvn clean install` green; the shaded SAPFileEditor.jar now bundles the
     base classes (unused until phase 2).

2. **MessageQueue and NLS messages** (NON-MECHANICAL - propose design,
   ask first): replace the ASMA MessageQueue world with the base one; a
   `Messages` repository class per the RULES doc for all SAP/COM/DMO/EXP
   codes with `.properties`; adapt all senders and the GUI/console output.

3. **Editor GUI onto base** (NON-MECHANICAL - ask first, optional):
   `ElementFactory`/`Actions`/`ModalDialog`/`AttributeTablePanel` for
   SAPFileEditor, aligning it with DIS6502/RMT conventions.

4. **Format logic convergence into base.atari** (NON-MECHANICAL - ask
   first, separate decision, likely driven from the RMT side): SAP/SAP-R/
   XEX/segment logic shared between ASMA and RMT.

## Out of scope

The JSON layer (gson, `ASMAClient`, `JSONWriter`), the website/JS side,
the ASAP dependency structure, and the published data contract.
