"use strict";

class FileInfo {
	constructor() {
		this.composerProxies = new Array();
	}
	getFileIndex() {
		return this.fileIndex;
	}
	getFilePath() {
		return this.filePath;
	}
	getFilePathWithBreaks() {
		return this.getFilePath().replace("/", "/ "); // zero-width space
	}
	getFileSizeText() {
		return this.fileSize + " (" + Util.getBytesString(this.fileSize) + ") Bytes";
	}
	getOriginalModuleFormatText() {
		return this.originalModuleExtDescription + " (" + this.originalModuleExt.toUpperCase() + ")";
	}
	getChannelsText() {
		switch (this.hardware) {
			case Hardware.ATARI800:
				return ((this.channels == 1) ? "Mono (4)" : "Stereo (8)");
			case Hardware.ATARI2600:
				return "Mono (2)";
		}
		throw "Unsupported hardware " + this.hardware;
	}
	getDefaultSongIndex() {
		return this.defaultSongIndex;
	}
	getSongsText() {
		if (this.songs == 1) {
			return "1";
		}
		return this.songs + "/" + (this.getDefaultSongIndex());
	}
	getAuthorsArray() {
		let authors = this.author.split("&");
		for (let i = 0; i < authors.length; i++) {
			authors[i] = authors[i].trim();
		}
		return authors;
	}
	addComposerProxy(composerProxy) {
		if (!this.composerProxies.includes(composerProxy)) {
			this.composerProxies.push(composerProxy);
		}
	}
	getComposerProxies() {
		return this.composerProxies;
	}
}





class FileInfoList {
	constructor() {
	}
	static init(fileInfos) {
		for (let i = 0; i < fileInfos.length; i++) {
			let fileInfo = Object.assign(new FileInfo(), fileInfos[i]);

			// It is possible to replace last digits with question marks
			fileInfos[i] = fileInfo;
			try {
				FileInfoList.check(fileInfo);
			} catch (ex) {
				Logger.logError("In " + fileInfo.getFilePath() + ": " + ex);
			}
		}
	}

	static check(fileInfo) {
		try {
			FileInfoList.checkDate(fileInfo.date);
		} catch (ex) {
			throw `Date "${fileInfo.date}" is invalid. ${ex}`;
		};
	}

	static checkDate(date) {

		const UNSURE = " <?>";
		if (date == "") {
			return;
		}
		if (date.endsWith(UNSURE)) {
			date = date.substring(0, date.lastIndexOf(UNSURE))
		}

		let pattern = date.replaceAll("0", "?").replaceAll("1", "?").replaceAll("2", "?").replaceAll("3", "?").replaceAll("4", "?").replaceAll("5", "?").replaceAll("6", "?").replaceAll("7", "?").replaceAll("8", "?").replaceAll("9", "?");

		let dayFrom = "";
		let monthFrom = "";
		let yearFrom = "";
		let dayTo = "";
		let monthTo = "";
		let yearTo = "";

		switch (pattern) {
			case '??/??/????':    // DD/MM/YYYY (preferred)
				dayFrom = date.substring(0, 2);
				monthFrom = date.substring(3, 5);
				yearFrom = date.substring(6, 10);
				FileInfoList.checkDayOfMonth(dayFrom, monthFrom, yearFrom);
				break;
			case '??-??/??/????': // DD-DD/MM/YYYY
				dayFrom = date.substring(0, 2);
				dayTo = date.substring(3, 5);
				if (dayFrom > dayTo) {
					throw `Day-from ${dayFrom} after day-to ${dayTo}`;
				}
				monthFrom = date.substring(6, 8);
				yearFrom = date.substring(9, 13);
				break;
			case '??/????':       // MM/YYYY
				monthFrom = date.substring(0, 2);
				yearFrom = date.substring(3, 5);
				break;
			case '??-??/????':    // MM-MM/YYYY
				monthFrom = date.substring(0, 2);
				monthTo = date.substring(3, 5);
				if (monthFrom > monthTo) {
					throw `Month-from ${monthFrom} after month-to ${monthTo}`;
				}
				yearFrom = date.substring(6, 10);
				break;
			case '????':          // YYYY
				yearFrom = date.substring(0, 4);
				break;
			case '????-????':     // YYYY-YYYY
				yearFrom = date.substring(0, 4);
				yearTo = date.substring(5, 9);
				if (yearFrom > yearTo) {
					throw `Year-from ${yearFrom} after year-to ${yearTo}`;
				}
				break;
			default:
				throw `Invalid pattern "${pattern}"`;
		}
	}

	static checkDayOfMonth(day, month, year) {
		FileInfoList.checkMonthOfYear(month, year);
		switch (month) {
			case "01":
			case "02":
			case "03":
			case "04":
			case "05":
			case "06":
			case "07":
			case "08":
			case "09":
			case "10":
			case "11":
			case "12":
			case "13":
			case "14":
			case "15":
			case "16":
			case "17":
			case "18":
			case "19":
			case "20":
			case "21":
			case "22":
			case "23":
			case "24":
			case "25":
			case "26":
			case "27":
			case "28":
			case "29": // Ignore leap years
				break;
			case "30":
				if (month == '02') {
					throw `Day ${day} is invalid`
				};
				break;
			case "31":
				if (month == '02' || month == '04' || month == '09' || month == '11') {
					throw `Day ${day} is invalid`
				};
				break;
			default:
				throw `Day ${day} is invalid`;
		}
	}

	static checkMonthOfYear(month, year) {
		FileInfoList.checkYear(year);
		switch (month) {
			case "01":
			case "02":
			case "03":
			case "04":
			case "05":
			case "06":
			case "07":
			case "08":
			case "09":
			case "10":
			case "11":
			case "12":
				break;
			default:
				throw `Month ${month} is invalid`;
		}
	}

	static checkYear(year) {
		year = year.replaceAll("?", "0");
		if (year > new Date().getFullYear()) {
			throw `Year ${year} is in the future`;
		}
	}
}


