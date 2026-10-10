# Plan: JavaScript cleanup in public_html/asmadb

Written 2026-10-10, based on a review of the player application. The structural
analysis lives in [public_html/README.md](../public_html/README.md); this plan
holds the actionable findings. Status: section 1 done (2026-10-10), sections
2-3 open.

## 1. Bug fixes (small, isolated, verify via index.bat) - DONE 2026-10-10

All six fixes below are applied; while fixing `checkDayOfMonth`, a seventh
latent bug was found and fixed: the 31-day check omitted June (`'06'`).
Script cache busters in `asmadb/index.html` were bumped for `ui.js`,
`file.js` and `asma.js`. Regression tests exist since the same day in
`js-test/` (Node.js LTS, built-in test runner, `node --test
"js-test/*.test.js"`; the loader runs the classic scripts in a VM context
like browser script tags). They cover the date validation, URL file path
scheme, author splitting, composer proxies and encodeHTML quoting, and run
in CI as the `javascript` job of `build.yml`. One manual smoke test via
`index.bat` - search, play, author dialog, details toggle - is still
advisable, since `asma.js` itself is DOM-coupled and only syntax-checked.

- **`file.js` `FileInfoList.checkDayOfMonth`**: `switch (month)` must be
  `switch (day)` - the cases are day values (`"01"`-`"31"`). As written, days
  are never validated and the inner `if (month == '02')` branches are
  unreachable in a meaningful way. Note: the Java exporter does not validate
  dates at all, so this bug currently has no safety net.
- **`file.js` `FileInfoList.checkDate`**, `MM/YYYY` case: `yearFrom =
  date.substring(3, 5)` takes two characters; must be `substring(3, 7)`.
  Currently harmless because that branch performs no further checks.
- **`asma.js` `setSearchField(id, ...)`**: hardcodes `"searchAuthor"` in its
  body instead of using the `id` parameter. Works only because it is called
  exactly once, for that field.
- **`asma.js`**: four occurrences of `replayFrequency.innerhtml = ""` /
  `addresses.innerhtml = ""` (lowercase) are silent no-ops - stale values
  survive when switching to a file without ASAPInfo. Must be `innerHTML`.
- **`asma.js` `displayAuthorDetails`**: `composerIndex >= composerProxies.size`
  - Arrays have `.length`, not `.size`; the guard never triggers.
- **`ui.js` `UI.encodeHTML`**: does not escape quotes, while some attribute
  values are interpolated unescaped (`title="...filePath..."`,
  `sorttable_customkey="...title..."`). With curated archive data this is a
  robustness issue, not an XSS exposure - a title containing `"` breaks the
  row. Fix by escaping `"` and `'` in `encodeHTML` and routing the attribute
  values through it.

## 1b. Playing time slider (found and fixed 2026-10-10)

Reported as "dragging the slider no longer works". Diagnosis: the engine seek
path was healthy (verified with Node against the real ASAP module); the played
file simply had no TIME tag, so `getDuration()` returned -1, the slider's
`max` stayed 0 and a range input with `min=max=0` is visible but frozen. A
sweep of 4000 archive files found only 14 without a duration. Fixes: the
slider is hidden while playing a file without a duration, and the 500 ms
refresh no longer overwrites the thumb position while the user is dragging
(tracked via pointerdown/up/cancel). Verified manually in the browser.

## 2. Modernization (optional, no urgency)

- `asapweb.js` uses the deprecated `ScriptProcessorNode`; the replacement is
  an `AudioWorklet`. This is upstream ASAP code - take the fix from upstream
  rather than forking it here.
- `asmadb.js` (2+ MB) is parsed synchronously in `<head>`. Since `asmadb.json`
  now exists, the page could fetch it asynchronously for faster first paint -
  but the script-tag approach is what makes `file://` mode work, so a change
  would need a fallback. Questionable trade-off; current approach is
  defensible.
- ~~`asmadb.js` has no `?version=` cache buster and relies on the 1-hour
  `.htaccess` expiry.~~ Done 2026-10-10, for all scripts: a new
  `asmadb/.htaccess` sets `Cache-Control: no-cache` (cache but revalidate,
  cheap 304s) for `*.js`/`*.css`/`*.json`, and the hand-maintained
  `?version=` parameters were removed from `asmadb/index.html` - that
  convention had already failed silently (`file.js`/`ui.js` carried no
  parameter at all). Heavy rarely-changing assets (images, fonts, the
  `/asma/` files) keep the 1-hour default. Verify the response headers
  once after the next FTP upload (local python http.server ignores
  `.htaccess`).

## 3. Validation consolidation - DONE 2026-10-10

Executed as scoped below. Results of the first exporter run with the new
checks over the full archive: 0 errors (every date is valid under the
stricter ported checks, SAP-110) and 24 SAP-111 warnings (files without a
TIME tag for the default song) - the data-quality list for the team. The
database outputs stayed byte-identical.

Date validation exists only in JS (`file.js`, partly broken - see above);
path/name validation exists only in Java (`FileInfoList.checkFileInfos`,
SAP-1xx codes). Validation belongs on the exporter side, where errors actually
block a release and reach the team, instead of running on every page load and
reporting into a console nobody watches. Scope:

1. Port the date checks (including the section-1 fixes) into the Java
   `FileInfoList.checkFileInfos` in asma-lib as `SAP-110`, with JUnit tests
   ported from `js-test/file.test.js`.
2. New check from the slider analysis (section 1b): warn in
   `FileInfoScanner` when a SAP file has no TIME tag for the default song
   (`SAP-111`, warning not error - endless loops are legitimate). Would have
   surfaced the 14 duration-less files as a data-quality list long ago.
3. Drop `FileInfoList.check`/`checkDate` from `file.js` entirely and migrate
   its js-test cases to the Java side. Display logic (encodeHTML, proxies,
   URL scheme) stays in JS with its tests.

## 4. Keep-in-sync list

Documented in public_html/README.md: the export default values, the FileInfo
model, and the URL file path scheme are mirrored between the JS app and the
Java asma-lib. Any change to one side must be applied to the other; the
ASMAClientTest in asma-lib catches default-value drift on the Java side.
