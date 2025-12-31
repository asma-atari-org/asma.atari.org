"use strict";

/* Structure of Demozoo production.

	{
		"id": 62401,
		"title": "Łąńóś",
		"authorIDs": [
			31134
		],
		"filePath": "Composers/Karwacki_Jakub/Lanos.sap"
	}
*/

class Fetcher {

	constructor() {
		this.demozoo = null;
	}

	sleep(milliseconds) {
		return new Promise(resolve => setTimeout(resolve, milliseconds));
	}

	fetchPages(url, demozoo) {
		this.demozoo = demozoo;
		this.startTime = new Date();
		this.fetchPage(url, 0);
	}

	fetchPage(url, productionsIndex) {
		Logger.log("Fetching page " + url);
		fetch(url)
			.then((response) => response.json())
			.then((data) => this.fetchProductions(data, 0, productionsIndex));
	}

	// data = data returned from HTTP request
	// resultIndex = index in the complete query resöt
	// productionIndex = index where to insert in the demozoo productions
	fetchProductions(data, resultIndex, productionsIndex) {
		if (resultIndex < data.results.length) {
			this.fetchProduction(data, resultIndex, productionsIndex);
		} else {
			if (data.next != undefined) {
				this.fetchPage(data.next, productionsIndex);
			} else {
				Logger.log("demozooProductions = ");
				Logger.log(this.demozoo.productions);
			}
		}
	}

	async fetchNextProduction(productionsData, resultIndex, productionsIndex) {
		resultIndex++;
		productionsIndex++;
		await this.sleep(100);
		this.fetchProductions(productionsData, resultIndex, productionsIndex, productionsIndex);

	}

	fetchProduction(productionsData, resultIndex, productionsIndex) {
		let production = productionsData.results.at(resultIndex);
		let url = production.url;

		let now = new Date().getTime();
		let milliSecondsSinceStart = now - this.startTime.getTime();
		let milliSecondsToGo = (milliSecondsSinceStart / (productionsIndex) * (productionsData.count - productionsIndex));

		Logger.log("Fetching production " + (productionsIndex + 1) + " of " + productionsData.count
			+ " from " + url + " (" + Util.getDurationString(milliSecondsSinceStart) + " until now, "
			+ Util.getDurationString(milliSecondsToGo) + " to go)");
		fetch(url)
			.then((response) => response.json())
			.then((data) => this.demozoo.addProduction(productionsData, resultIndex, data, productionsIndex))
			.then(() => this.fetchNextProduction(productionsData, resultIndex, productionsIndex))
			.catch((error) => {
				console.error("Fetch Error:", error);
			});

	}
}

class Demozoo {
	constructor(productions) {
		this.productions = productions;
		this.productionsByFilePathAndSongIndexMap = new Map();
	}

	initProductions(fileInfoList, summary) {
		this.productionsByFilePathAndSongIndexMap.clear();

		// For all productions on Demozoo...
		let result = "";
		let linkCount = 0;
		let missingLinkCount = 0;
		for (let production of this.productions) {
			if (production.filePath == null) {
				missingLinkCount++;
				let candidatesHTML = "";
				for (let fileIndex = 0; fileIndex < fileInfoList.length; fileIndex++) {
					let fileInfo = fileInfoList[fileIndex];
					if (fileInfo.title.includes(production.title)) {
						if (candidatesHTML != "") {
							candidatesHTML += ", ";
						}
						candidatesHTML += "https://asma.atari.org/asma/" + fileInfo.getFilePath();
					}
				}
				if (candidatesHTML != "") {
					candidatesHTML += " or ";
				}
				result += this.getMusicHTML(production) + " with file extensions '" + production.fileExtensions + "' has no ASMA link.";

				result += " Try " + candidatesHTML + this.getFindInASMAByTitleHTML(production) + "<br>\n";
			} else {
				let key = this.getFilePathAndSongIndexKey(production.filePath, production.songIndex);
				this.productionsByFilePathAndSongIndexMap.set(key, production);
				linkCount++;
			}
		}
		result = linkCount + " Demozoo productions with ASMA link found.<br>\n" + missingLinkCount +
			" Demozoo productions with missing ASMA link found.<br>\n<br>\n" +
			"<a href=\"javascript:asmaInstance.fetchDemozoo()\">Fetch all Demozoo music productions for Atari 8-bit and Atari VCS.</a><br>\n<br>\n" + result;
		summary.demozoo = result;


		// For all productions on ASMA...
		result = "";
		linkCount = 0;
		missingLinkCount = 0;

		for (let fileIndex = 0; fileIndex < fileInfoList.length; fileIndex++) {
			let fileInfo = fileInfoList[fileIndex];
			let production = this.getProductionByFilePathAndSongIndex(fileInfo.filePath, fileInfo.defaultSongIndex);
			if (production != undefined) {
				linkCount += 1;
			} else {
				missingLinkCount += 1;
				result += this.getASMAMusicHTML(fileInfo) + " of type '" + this.getFileExtension(fileInfo.filePath) + "' has no Demozoo link.";
				result += (" Try " + this.getFindByTitleHTML(fileInfo.title) + "<br>\n");
			}

		}

		result = linkCount + " ASMA productions with Demozoo link found.<br>\n" + missingLinkCount +
			" ASMA productions with missing Demozoo link found.<br>\n<br>\n" + result;
		summary.asma = result;
	}

	getProductionsByFilePathAndSongIndexMap() {
		return this.productionsByFilePathAndSongIndexMap;
	}

	getFilePathAndSongIndexKey(filePath, songIndex) {
		let key = filePath;
		if (songIndex >= 0) {
			key += "?songIndex=" + songIndex;
		}
		return key;
	}

	getProductionByFilePathAndSongIndex(filePath, songIndex) {
		let production = this.productionsByFilePathAndSongIndexMap.get(this.getFilePathAndSongIndexKey(filePath, songIndex));
		if (production == undefined) {
			production = this.productionsByFilePathAndSongIndexMap.get(this.getFilePathAndSongIndexKey(filePath, -1));
		}
		return production;
	}

	getMusicHTMLForFilePath(filePath, songIndex) {
		let production = this.getProductionByFilePathAndSongIndex(filePath, songIndex);
		if (production != undefined) {
			return "<a href=\"https://demozoo.org/music/" + production.id + "\" target=\"blank\">" + production.id + "</a>";
		}
		return "None";
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


	fetchDemozoo() {
		this.productions = new Array();
		let fetcher = new Fetcher();
		// Retrieve all music for Atari 8-bit and Atari VCS.
		fetcher.fetchPages("https://demozoo.org/api/v1/productions/?supertype=music&platform=54&platform=16", this);
	}

	getFileExtension(fileName) {
		return fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length) || "";
	}

	// Callback for Fetcher.fetchPages().
	addProduction(productionsData, resultIndex, data, productionsIndex) {

		let authorIDs = [];
		for (let author_nick of data.author_nicks) {
			authorIDs[authorIDs.length] = author_nick.releaser.id;
		}

		const prefix = "https://asma.atari.org/asma/";
		let filePath = null;
		let fileExtensions = new Set;
		for (let downloadLink of data.download_links) {
			if (downloadLink.link_class == "BaseUrl") {

				if (filePath == "" && downloadLink.url.startsWith(prefix)) {
					filePath = downloadLink.url.substring(prefix.length);
				}

				// Collect file extensions from all download links.
				let fileExtension = this.getFileExtension(downloadLink.url);
				if (fileExtension != "") {
					fileExtensions.add(fileExtension);
				}

			}
		}
		fileExtensions = Array.from(fileExtensions).join(',');
		let production = { id: data.id, title: data.title, authorIDs: authorIDs, filePath: filePath, fileExtensions: fileExtensions };
		this.productions[productionsIndex] = production;
	}
};
