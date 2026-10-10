# Plan: ASAP fork branch cleanup after the PR is merged

Written 2026-10-10. To be executed when Fox merges the pull request for
`pokey-register-shadow` into the official ASAP repository. Background in
[02_ECLIPSE_INTEGRATION.md](02_ECLIPSE_INTEGRATION.md)'s sibling discussions
and `ASAP.md`; current fork state (`u/peterdell/asap`): `master`,
`pokey-register-shadow` (the open PR), `sapr-dump` (ASMA POKEY dump patch),
`asma-8.0.0` (pinned, reproducible source of the shipped
`net.sf.asap:asap:8.0.0-asma` jar). `android-background` was already deleted
on 2026-10-10.

## Trigger

The PR is merged upstream (check with:
`git fetch upstream` in `C:\jac\system\Fusion\Programming\Repositories\Tools\asap`,
then `git log upstream/master --oneline | head` for the register-shadow commit,
or `git merge-base --is-ancestor pokey-register-shadow upstream/master`).

## Steps

1. **Sync the fork's master.** In the `asap` clone:
   `git fetch upstream`, `git checkout master`,
   `git merge --ff-only upstream/master`, `git push origin master`.
   (Equivalent in `asap-code`: `git pull` from `origin` = the official repo,
   then `git push fork master`.)

2. **Delete the merged PR branch.** Once
   `git merge-base --is-ancestor pokey-register-shadow master` confirms the
   content is contained: `git branch -d pokey-register-shadow` and
   `git push origin --delete pokey-register-shadow`.
   If Fox squashed or amended instead of merging the commit as-is,
   `merge-base --is-ancestor` will say "not contained" even though the feature
   landed - verify by diff (`git diff pokey-register-shadow upstream/master --
   asap.fu asap.h asap.c pokey.fu`) before using `git branch -D`.

3. **Rebase the ASMA patch onto the new master.** In `asap-code`:
   `git checkout sapr-dump`, `git rebase master` (or onto `origin/master`),
   resolve anything POKEY-related that the merged API changed, then
   `git push fork --force-with-lease sapr-dump`. Force-pushing is fine here:
   the branch is a personal topic branch, not a PR target.

4. **Rebuild and verify the ASMA jar from the rebased branch** (this replaces
   the frozen 8.0.0 build and finally unifies "what RMT uses" and "what ASMA
   uses" on one upstream state):
   - Regenerate the Java sources with fut/make (`make java/asap.jar` in MSYS2)
     or transplant the Maven build commit from `asma-8.0.0` and regenerate
     `java/src` with fut directly.
   - Pick a new version from the upstream state, e.g. `8.1.0-asma` - do NOT
     reuse `8.0.0-asma`.
   - Verify in asma.atari.org: install the new artifact, bump the version in
     `asma-tools/pom.xml`, run `mvn clean install`, run the `ASMAExporter`
     and confirm `asmadb.js` is byte-identical (the exporter does not use the
     dump API, so it must not change), and render at least one SAP file to
     SAP-R with `ASAPRenderer`, comparing against the same render made with
     `8.0.0-asma`.
   - Commit the new jar to `maven-repo/net/sf/asap/asap/<new version>/` and
     remove the old `8.0.0-asma` folder in the same commit as the pom bump.

5. **Retire the pinned branch.** Only after step 4 is shipped:
   in `asap-code`, `git tag asap-8.0.0-asma asma-8.0.0`,
   `git push fork asap-8.0.0-asma`, then `git branch -d asma-8.0.0` and
   `git push fork --delete asma-8.0.0`. The reproducible state stays
   reachable via the tag.

6. **Update the documentation**: rewrite the branch inventory in `ASAP.md`
   (end state: `master` + `sapr-dump` + tag `asap-8.0.0-asma`) and tick this
   plan off.

## Not part of this cleanup

- New RMT changes keep going to their own topic branches off `master`,
  one per submittable feature - they are the next PRs, not cleanup.
- A disposable integration branch (merge of the open topics, never PR'd) can
  be created any time a combined build is needed before the merge happens;
  it is rebuilt at will and needs no cleanup tracking.
