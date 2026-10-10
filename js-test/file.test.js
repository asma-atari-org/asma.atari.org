import { test } from "node:test";
import assert from "node:assert/strict";
import { loadScripts } from "./loader.js";

// The date validation moved to the Java exporter (org.atari.asma.FileInfoList,
// tested in asma-lib's FileInfoListTest); this file tests the remaining
// client-side logic.

const { FileInfo } = loadScripts(["ui.js", "util.js", "file.js"], ["FileInfo"]);

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
