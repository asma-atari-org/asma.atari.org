"use strict";

/* Structure of ASMADemozoo production.

	{
		"id": 62401,
		"title": "Łąńóś",
		"authorIDs": [
			31134
		],
		"urlFilePath": "Composers/Karwacki_Jakub/Lanos.sap#2"
	}
*/


class Demozoo {
	constructor(productions) {
		this.productions = productions;
		this.productionsByIDMap = new Map();
		this.productionsByURLFilePathMap = new Map();
	}

	initProductions(fileInfoList, summary) {
		this.productionsByIDMap.clear();
		this.productionsByURLFilePathMap.clear();

		// For all productions on Demozoo...
		let result = "";
		let linkCount = 0;
		let missingLinkCount = 0;
		for (let production of this.productions) {
			this.productionsByIDMap.set(production.id, production);
			if (production.urlFilePath == null) {
				missingLinkCount++;
			} else {
				this.productionsByURLFilePathMap.set(production.urlFilePath, production);
				linkCount++;
			}
		}
		result = linkCount + " Demozoo productions with ASMA link found.<br>\n" + missingLinkCount +
			" Demozoo productions with missing ASMA link found.<br>\n<br>\n";
		summary.demozoo = result;


		// For all productions on ASMA...
		linkCount = 0;
		let brokenLinkCount = 0;
		missingLinkCount = 0;

		for (let fileIndex = 0; fileIndex < fileInfoList.length; fileIndex++) {
			let fileInfo = fileInfoList[fileIndex];
			if (fileInfo.getDemozooID() != undefined) {
				let production = this.getProductionByID(fileInfo.getDemozooID());
				if (production != undefined) {
					linkCount += 1;
				} else {
					brokenLinkCount += 1;
				}
			} else {
				missingLinkCount += 1;
			}

		}

		result = linkCount + " ASMA productions with Demozoo link found.<br>\n" +
			brokenLinkCount + " ASMA productions with broken Demozoo link found.<br>\n" + missingLinkCount +
			" ASMA productions with missing Demozoo link found.<br>\n<br>\n";
		summary.asma = result;
	}

	getProductionByID(id) {
		return this.productionsByIDMap.get(id);
	}

	getProductionByURLFilePath(urlFilePath) {
		return this.productionsByURLFilePathMap.get(urlFilePath);;
	}

	getMusicHTMLForDemozooID(demozooID, title) {
		if (demozooID) {
			return "<a href=\"https://demozoo.org/music/" + demozooID + "\" target=\"blank\">" + demozooID + "</a>";
		}
		return "<a href=\"https://demozoo.org/search/?q="+encodeURI(title)+"&category=music&platform%3A\"Atari+8+Bit\"\" target=\"blank\">Find Music</a>, <a href=\"https://demozoo.org/music/new/\" target=\"blank\">Create Music</a>";;
	}

	getASMAMusicHTML(fileInfo) {
		return "<a href=\"https://asma.atari.org/asmadb/#/" + fileInfo.filePath + "\" target=\"blank\">" + fileInfo.title + "</a>";
	}

	getFindInASMAByTitleHTML(production) {
		return "<a href=\"https://asma.atari.org/asmadb/?searchKeyword=" + encodeURIComponent(production.title) + "\" target=\"blank\">searching in ASMA.</a>";
	}

	getMusicHTML(production) {
		return "<a href=\"https://demozoo.org/music/" + production.id + "\" target=\"blank\">" + production.title + "</a>";
	}

	getFindByTitleHTML(title) {
		return "<a href=\"https://demozoo.org/search/?q=" + encodeURIComponent(title) + "&category=music\" target=\"blank\">searching in Demozoo.</a>";
	}

	getFileExtension(fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length) || "";
	}
};
