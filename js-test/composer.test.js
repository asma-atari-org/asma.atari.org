import { test } from "node:test";
import assert from "node:assert/strict";
import { loadScripts } from "./loader.js";

const { ComposerProxy, ComposerList, ComposerStatus } = loadScripts(
	["ui.js", "util.js", "composer.js"],
	["ComposerProxy", "ComposerList", "ComposerStatus"]);

test("full name and sort key derive from name parts", () => {
	const proxy = new ComposerProxy(0, ComposerStatus.VERIFIED,
		{ lastName: "Hummel", firstName: "Jakub", handles: "Fel", country: "Poland", demozooID: "56466" });
	assert.equal(proxy.getFullName(), "Jakub Hummel");
	assert.equal(proxy.getLastNameSortKey(), "Hummel_Jakub_Fel");
	assert.equal(proxy.getFolderName(), "Hummel_Jakub");
	assert.equal(proxy.getFolderPath(), "Composers/Hummel_Jakub");
});

test("folder name falls back to the first handle for unknown last names", () => {
	const proxy = new ComposerProxy(0, ComposerStatus.UNVERIFIED,
		{ lastName: "<?>", firstName: "", handles: "Fel, SecondHandle" });
	assert.equal(proxy.getFolderName(), "Fel");
});

test("handles array is split and trimmed", () => {
	const proxy = new ComposerProxy(0, ComposerStatus.VERIFIED,
		{ lastName: "X", firstName: "Y", handles: "One, Two,Three" });
	// Array.from converts the VM-realm array for the prototype-sensitive deep comparison.
	assert.deepEqual(Array.from(proxy.getHandlesArray()), ["One", "Two", "Three"]);
});

test("missing country maps to Unknown", () => {
	const proxy = new ComposerProxy(0, ComposerStatus.VERIFIED, { lastName: "X", firstName: "Y" });
	assert.equal(proxy.getCountry(), "Unknown");
});

test("ComposerList indexes composers and aggregates countries", () => {
	const composerList = new ComposerList([
		{ lastName: "Hummel", firstName: "Jakub", handles: "Fel", country: "Poland", demozooID: "56466" },
		{ lastName: "Sterba", firstName: "Radek", lastNameUnicode: "Štěrba", handles: "Raster", country: "Czech Republic", demozooID: "25944" },
		{ lastName: "Second", firstName: "Pole", handles: "", country: "Poland", demozooID: "1" }
	]);
	assert.equal(composerList.getComposerProxy(0).getFullName(), "Jakub Hummel");
	assert.equal(composerList.getComposerProxy(1).getLastNameUnicode(), "Štěrba");
	assert.equal(composerList.countryInfoMap.get("Poland").composerCount, 2);
	assert.equal(composerList.countryInfoMap.get("Czech Republic").composerCount, 1);
});
