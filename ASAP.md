# ASAP

Which build of ASAP (Another Slight Atari Player) this project uses, where it
comes from, and how that differs from the two copies in RASTER Music Tracker.
Written 2026-10-04.

## The chain for this project

The edited `asap.fu` and `pokey.fu` in the `asap-code` clone
(`C:\jac\system\Fusion\Programming\Repositories\Tools\asap-code`) were
compiled with `make java/asap.jar`. That produced
`java/src/net/sf/asap/ASAP.java` and the classes beside it, including
`PokeyStates` and `PokeyStatesList`, and packed them into `java/asap.jar` on
2026-04-14 at 00:25. That is five minutes after `pokey.fu` was last saved, so
the jar is the direct product of those edits.

The consumer is `org.atari.asma.sap.SAPRFileLogic`, which imports the player
and the states list from the generated package and calls the frame dump into
the file's own list. The states type also appears in the renderer and in the
SAP-R file class. The dump API exists only for this project.

`java/lib/asap.txt` records the same arrangement: the release jar and source
archive came from Fox, the clone points at the current development version for
testing changes, and the jar and the generated source and class folders are
produced by that make command in an MSYS2 shell.

## The three copies, side by side

| Project | ASAP it uses | Where it comes from |
|---|---|---|
| asma.atari.org | locally built jar with the dump API | `make java/asap.jar` over the edited portable sources |
| RMT Java port | 8.0.0 release source, patched | the archive Fox provided, adapted in place |
| RMT C++ | 7.0.0 generated C | raw download from the upstream tree |

This also explains the shape of the two adaptations. The tracker already has
the register stream from its own driver running on the emulated processor, so
it only needed a way to read the shadow byte, which its own patch adds. This
project starts from a finished module file, so it needs the player itself to
hand out the registers frame by frame, which is what the addition does.

## Status in this repository

Since the Maven migration on 2026-10-09, the patched jar is committed in this
repository as `maven-repo/net/sf/asap/asap/8.0.0-asma/asap-8.0.0-asma.jar` and
consumed by the `asma-tools` module as the Maven dependency
`net.sf.asap:asap:8.0.0-asma`.

## Status in the asap-code clone

The patch is no longer uncommitted working-tree state. The clone
(`C:\jac\system\Fusion\Programming\Repositories\Tools\asap-code`) now has two
branches, both carrying the identical `FillPokeyState / DoFrameDump` commit
(+281 lines in `asap.fu`/`pokey.fu`; upstream never touched those two files
between the two base revisions, so the patch is the same on either base):

- `asma-8.0.0` - the patch pinned at `44765da` (2026-03-19, the exact base of
  the jar built on 2026-04-14), plus a Maven build (`pom.xml`) over the
  committed fut-generated Java sources and the embedded 6502 player binaries.
  `mvn install` there produces `net.sf.asap:asap:8.0.0-asma` with content
  identical to the jar committed here - no MSYS2/fut/make needed.
- `sapr-dump` - the same patch on top of upstream master of 2026-09-22, the
  starting point for a future rebuild against current ASAP.

The clone has a personal SourceForge fork remote
(`ssh://git.code.sf.net/u/peterdell/asap`). Both branches were pushed there on 2026-10-09.

## One practical consequence (historical)

When this document was written, those edits were the only copy of that work:
uncommitted in a clone that was fast-forwarded on 2026-10-04 by six months,
from 2026-04-13 to the upstream state of 2026-09-22, so the jar was older than
the sources in the tree and a rebuild would have mixed the April additions
with everything upstream changed since. This is resolved: the patch is
committed on the branches above and pushed to the fork, `asma-8.0.0` pins the
reproducible state of the jar, and `sapr-dump` is the deliberate starting
point for a rebuild against current ASAP.

## The second clone

`C:\jac\system\Fusion\Programming\Repositories\Tools\asap` is a clone of the
personal SourceForge fork itself (`origin` = `u/peterdell/asap`, `upstream` =
the official `p/asap/code`). It has the branch `pokey-register-shadow` checked
out - the RMT-side patch that exposes the POKEY audio registers and
single-frame stepping on top of upstream master of 2026-09-22. The fork is the
hub holding all personal branches (`asma-8.0.0`, `sapr-dump`,
`pokey-register-shadow`, `android-background`); `asap-code` pushes to it via
the `fork` remote, `asap` via `origin`.
