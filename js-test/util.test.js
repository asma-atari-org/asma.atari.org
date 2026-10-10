import { test } from "node:test";
import assert from "node:assert/strict";
import { loadScripts } from "./loader.js";

const { Util, UI } = loadScripts(["ui.js", "util.js"], ["Util", "UI"]);

test("getDurationString formats milliseconds", () => {
	assert.equal(Util.getDurationString(0), "0:00:00");
	assert.equal(Util.getDurationString(61000), "0:01:01");
	assert.equal(Util.getDurationString(3661000), "1:01:01");
	assert.equal(Util.getDurationString(-1), "");
	assert.equal(Util.getDurationString(undefined), "");
	assert.equal(Util.getDurationString(Infinity), "Infinity");
});

test("getAddressHexString formats addresses", () => {
	assert.equal(Util.getAddressHexString(-1), "-");
	assert.equal(Util.getAddressHexString(0x2000), "$2000");
});

test("getNormalizedCountry strips qualifiers", () => {
	assert.equal(Util.getNormalizedCountry("Poland"), "Poland");
	assert.equal(Util.getNormalizedCountry("Poland (?)"), "Poland");
	assert.equal(Util.getNormalizedCountry("Czech Republic (now Slovakia)"), "Czech Republic");
	assert.equal(Util.getNormalizedCountry(""), "Unknown");
});

test("encodeHTML escapes markup, quotes and non-ASCII", () => {
	assert.equal(UI.encodeHTML("plain"), "plain");
	assert.equal(UI.encodeHTML("<b>"), "&#60;b&#62;");
	assert.equal(UI.encodeHTML("a & b"), "a &#38; b");
	// Regression: quotes must not break HTML attribute values.
	assert.equal(UI.encodeHTML("\""), "&#34;");
	assert.equal(UI.encodeHTML("'"), "&#39;");
	assert.equal(UI.encodeHTML("Štěrba"), "&#352;t&#283;rba");
});
