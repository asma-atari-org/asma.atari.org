import { test } from "node:test";
import assert from "node:assert/strict";
import { loadScripts } from "./loader.js";

const { FileInfo, FileInfoList } = loadScripts(
	["ui.js", "util.js", "file.js"],
	["FileInfo", "FileInfoList"]);

function assertDateValid(date) {
	FileInfoList.checkDate(date);
}

function assertDateInvalid(date) {
	assert.throws(() => FileInfoList.checkDate(date), undefined, `"${date}" must be invalid`);
}

test("checkDate accepts the documented patterns", () => {
	assertDateValid("");
	assertDateValid("01/01/2000");    // DD/MM/YYYY
	assertDateValid("15/06/1995");
	assertDateValid("31/12/1999");
	assertDateValid("29/02/2001");    // Leap years are deliberately ignored
	assertDateValid("31/12/1999 <?>");
	assertDateValid("01-05/03/1999"); // DD-DD/MM/YYYY
	assertDateValid("06/1999");       // MM/YYYY
	assertDateValid("03-07/1999");    // MM-MM/YYYY
	assertDateValid("1985");          // YYYY
	assertDateValid("1985-1987");     // YYYY-YYYY
});

test("checkDate rejects invalid days", () => {
	assertDateInvalid("00/01/2000");
	assertDateInvalid("32/01/2000");
	assertDateInvalid("30/02/2000");
	assertDateInvalid("31/02/2000");
	assertDateInvalid("31/04/2000");
	assertDateInvalid("31/06/1999"); // June has 30 days
	assertDateInvalid("31/09/1999");
	assertDateInvalid("31/11/1999");
});

test("checkDate accepts day 31 in 31-day months", () => {
	assertDateValid("31/01/2000");
	assertDateValid("31/03/2000");
	assertDateValid("31/05/2000");
	assertDateValid("31/07/2000");
	assertDateValid("31/08/2000");
	assertDateValid("31/10/2000");
	assertDateValid("31/12/2000");
});

test("checkDate rejects invalid months and future years", () => {
	assertDateInvalid("15/13/2000");
	assertDateInvalid("15/00/2000");
	assertDateInvalid("01/01/2990");
});

test("checkDate rejects inverted ranges", () => {
	assertDateInvalid("05-03/06/1999"); // day-from after day-to
	assertDateInvalid("09-03/1999");    // month-from after month-to
	assertDateInvalid("1999-1995");     // year-from after year-to
});

test("checkDate rejects unknown patterns", () => {
	assertDateInvalid("1.1.2000");
	assertDateInvalid("January 2000");
});

test("getURLFilePath appends the song number only for non-default songs", () => {
	const fileInfo = new FileInfo();
	fileInfo.filePath = "Composers/Fel/Test.sap";
	fileInfo.songs = 3;
	fileInfo.defaultSongIndex = 0;
	assert.equal(fileInfo.getURLFilePath(1), "Composers/Fel/Test.sap");
	assert.equal(fileInfo.getURLFilePath(2), "Composers/Fel/Test.sap#2");
	assert.equal(fileInfo.getURLFilePath(3), "Composers/Fel/Test.sap#3");
});

test("getAuthorsArray splits cooperations", () => {
	const fileInfo = new FileInfo();
	fileInfo.author = "First Author & Second Author";
	// Array.from converts the VM-realm array for the prototype-sensitive deep comparison.
	assert.deepEqual(Array.from(fileInfo.getAuthorsArray()), ["First Author", "Second Author"]);
});
