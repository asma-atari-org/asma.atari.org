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

## One practical consequence

Those edits are the only copy of that work. They are uncommitted in a clone
that was fast-forwarded on 2026-10-04 by six months, from 2026-04-13 to the
upstream state of 2026-09-22. The jar this project depends on is therefore
older than the sources now in the tree, and rebuilding it would mix the April
additions with everything upstream changed since.
