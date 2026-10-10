# Plan: JavaScript cleanup in public_html/asmadb

Written 2026-10-10, based on a review of the player application. The structural
analysis lives in [public_html/README.md](../public_html/README.md); this plan
holds the actionable findings. Status: open.

## 1. Bug fixes (small, isolated, verify via index.bat)

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

## 2. Modernization (optional, no urgency)

- `asapweb.js` uses the deprecated `ScriptProcessorNode`; the replacement is
  an `AudioWorklet`. This is upstream ASAP code - take the fix from upstream
  rather than forking it here.
- `asmadb.js` (2+ MB) is parsed synchronously in `<head>`. Since `asmadb.json`
  now exists, the page could fetch it asynchronously for faster first paint -
  but the script-tag approach is what makes `file://` mode work, so a change
  would need a fallback. Questionable trade-off; current approach is
  defensible.
- `asmadb.js` has no `?version=` cache buster and relies on the 1-hour
  `.htaccess` expiry. The exporter could append a content hash or date to the
  script URL in the generated pages if staleness ever becomes an issue.

## 3. Validation consolidation (direction, needs decision)

Date validation exists only in JS (`file.js`, partly broken - see above);
path/name validation exists only in Java (`FileInfoList.checkFileInfos`,
SAP-1xx codes). Validation belongs on the exporter side, where errors actually
block a release. Proposal: port the date checks into the Java
`FileInfoList.checkFileInfos` (new SAP-1xx code), then reduce the JS side to
display-only (or drop `FileInfoList.check` there entirely).

## 4. Keep-in-sync list

Documented in public_html/README.md: the export default values, the FileInfo
model, and the URL file path scheme are mirrored between the JS app and the
Java asma-lib. Any change to one side must be applied to the other; the
ASMAClientTest in asma-lib catches default-value drift on the Java side.
