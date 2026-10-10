// Loads the classic (non-module) scripts of public_html/asmadb into a
// shared VM context, the way a browser loads consecutive <script> tags,
// and returns the requested top-level classes/objects.
//
// Class declarations are lexical bindings, not globalThis properties, so
// they are extracted by evaluating an object literal in the same context.

import { readFileSync } from "node:fs";
import vm from "node:vm";

const baseURL = new URL("../public_html/asmadb/", import.meta.url);

export function loadScripts(scriptNames, exportNames) {
	const context = {
		console,
		// Defined by asma.js (an ES module, not loadable here).
		Hardware: { ATARI2600: "ATARI2600", ATARI800: "ATARI800" }
	};
	vm.createContext(context);
	for (const scriptName of scriptNames) {
		const code = readFileSync(new URL(scriptName, baseURL), "utf8");
		vm.runInContext(code, context, { filename: scriptName });
	}
	return vm.runInContext("({" + exportNames.join(",") + "})", context);
}
