import { ASAPInfo, ASAPWriter } from "./asap-with-asapwriter.js";
import { asapWeb } from "./asapweb.js";

window.Hardware = { ATARI2600: "ATARI2600", ATARI800: "ATARI800" };

const FileType = {
	CM3: ".cm3", CMC: ".cmc", CMR: ".cmr", CMS: ".cms", DLT: ".dlt", DMC: ".dmc", FC: ".fc",
	MPD: ".mpd", MPT: ".mpt", RMT: ".rmt", SAP: ".sap", TM2: ".tm2", TM8: ".tm8", TMC: ".tmc",
	TTT: ".ttt",
	XEX: ".xex"
};

const FileIndex = { NONE: -1 };
const SongState = { NONE: "NONE", PLAYING: "PLAYING", STOPPED: "STOPPED" };

// The internal song index used in the code is zero-based and has pseudo values.
// The external song index used in the URL hash is one-based and has no pseudo values.
// The DEFAULT is expressed by omitting the external song index in the URL.
const SongIndex = { FIRST: 0, NONE: -1, DEFAULT: -2, RANDOM: -3 };

const DEFAULT_WINDOW_TITLE = "Atari SAP Music Archive";

class ASMAWriter extends ASAPWriter {
	save(filename, buffer, offset, length) {
		const output = buffer.slice(offset, offset + length);
		const blob = new Blob([output], { type: "application/octet-stream" });
		saveAs(blob, filename);
	}
}

class ASMA {
	constructor() {
	}

	/*
	 * Edit song routine
	 */
	static getNTSCPALString(fileInfo) {
		return fileInfo.getASAPInfo().isNtsc() ? "NTSC" : "PAL";
	}

	onWindowPopState(state) {
		Logger.log("Execute onWindowPopState: " + JSON.stringify(window.location) + JSON.stringify(state));
		this.initFromWindowLocation();
	}

	initSearchField(inputID, defaultValue) {
		// Get the input element
		let inputElement = UI.getElementById(inputID);

		let index = this.searchFields.length;
		this.searchFields[index] = { "defaultValue": defaultValue, "element": inputElement, "fieldName": inputID };

		// Execute a function when the user presses a key on the keyboard
		inputElement.addEventListener("keypress", function(event) {
			// If the user presses the "Enter" key on the keyboard
			if (event.key === "Enter") {
				// Cancel the default action, if needed
				event.preventDefault();
				// Trigger the button element with a click
				UI.getElementById("searchButton").click();
			}
		});
	}

	initSearchParameter(params, searchField) {
		let searchValue = params.get(searchField.fieldName);
		if (searchValue != null && searchValue != "") {
			searchField.element.value = searchValue;
			return true;
		} else {
			searchField.element.value = searchField.defaultValue;
		}
		return false;
	}

	initFromWindowLocation() {

		let url = new URL(window.location);
		let hash = url.hash;
		let params = new URLSearchParams(url.search.slice(1)); // Skip question mark "?"

		let startSearch = false;
		for (let searchField of this.searchFields) {
			startSearch |= this.initSearchParameter(params, searchField);
		}

		let debug = params.get("debug");
		if (debug != undefined) {
			Logger.setLogElement("consoleLog");
		}

		let detailsMode = (window.innerWidth >= 1024);
		this.setDetailsMode(detailsMode);

		const hashMatches = /^#\/(.+?)(?:\/(-?\d+))?$/.exec(hash);
		if (hashMatches != null) {
			let foundFileInfo = null;
			let filePath = hashMatches[1];
			Logger.log("Starting file with path \"" + filePath + "\"");
			for (let fileInfo of this.fileInfos) {
				if (fileInfo.getFilePath() == filePath) {
					foundFileInfo = fileInfo;
					break;
				}
			}

			if (foundFileInfo == null) {
				window.alert("There is no matching file in the database for the path \"" + filePath + "\".");
			} else {
				let foundSongIndex = hashMatches[2];
				if (foundSongIndex == undefined || foundSongIndex < 1) {
					foundSongIndex = foundFileInfo.getDefaultSongIndex();
				} else {
					foundSongIndex--; // Convert external one-based index to internal zero-based
				}
				if (this.currentFileInfo == null || this.currentFileInfo.getFilePath() != foundFileInfo.getFilePath() || this.currentSongIndex != foundSongIndex) {
					this.stopCurrentSong();
					this.playASMA(foundFileInfo.getFileIndex(), foundSongIndex, false);
					return;
				}
			}

		}

		if (startSearch) {
			UI.getElementById("searchButton").click();
		}
	}

	initFileInfos() {
		this.fileInfos = asma.fileInfos; // "asma" or array of local file
	}

	initInternal() {

		FileInfoList.init(asma.fileInfos);

		this.initFileInfos();

		this.demozoo = new Demozoo(asma.demozoo.productions);
		this.demozooSummary = {};
		this.demozoo.initProductions(this.fileInfos, this.demozooSummary);

		this.composerList = new ComposerList(asma.composerInfos);
		this.composerList.initFileInfos(this.fileInfos, this.demozoo);

		this.detailsMode = false;
		this.shuffleMode = false;

		// Player
		this.currentFileIndex = FileIndex.NONE;
		this.currentFileInfo = null;
		this.currentSongState = SongState.NONE;

		// Editing
		this.editSongDialog = null;

		// Search
		this.searchFields = [];

		this.currentSelectedIndex = undefined;

		let index = 0;
		for (let fileInfo of this.fileInfos) {
			fileInfo.fileIndex = index++;
			if (fileInfo.hardware == undefined) {
				fileInfo.hardware = Hardware.ATARI800;
			}
			if (fileInfo.comment == undefined) {
				fileInfo.comment = "";
			}
			switch (fileInfo.originalModuleExt) {
				case 'ttt':
					fileInfo.originalModuleExtDescription = "TIATracker";
					break;
				default:
					fileInfo.originalModuleExtDescription = ASAPInfo.getExtDescription(fileInfo.originalModuleExt);
					break;
			}
			if (fileInfo.channels == undefined) {
				fileInfo.channels = 1;
			}
			if (fileInfo.songs == undefined) {
				fileInfo.songs = 1;
			}
			if (fileInfo.defaultSongIndex == undefined) {
				fileInfo.defaultSongIndex = SongIndex.FIRST;
			}
			fileInfo.url = "../asma/" + fileInfo.getFilePath();
		}

		// Setup main window
		window.setInterval(this.refreshCurrentASAPInfo, 500, this);
		window.addEventListener('popstate', (event) => { asmaInstance.onWindowPopState(event.state); });

		// Get controls
		this.displayAuthorDialog = UI.getElementById("displayAuthorDialog");
		this.editSongDialog = UI.getElementById("editSongDialog");
		this.aboutDialog = UI.getElementById("aboutDialog");

		this.initSearchField("searchKeyword", "");
		this.initSearchField("searchFilePath", "");
		this.initSearchField("searchTitle", "");
		this.initSearchField("searchAuthor", "");
		this.initSearchField("searchDate", "");
		this.initSearchField("searchHardware", "1");
		this.initSearchField("searchChannels", "1");
		this.initSearchField("searchFormat", "");

		this.initFromWindowLocation();
	}

	init() {
		try {
			this.initInternal();
		} catch (ex) {
			window.alert(ex);
			throw ex;
		}
	}

	setDetailsMode(detailsMode) {
		this.detailsMode = detailsMode;
		let detailsModeButton = UI.getElementById("detailsModeButton");

		if (this.detailsMode == true) {
			detailsModeButton.innerHTML = "Hide Details";
		} else {
			detailsModeButton.innerHTML = "Show Details";
		}

		this.setSearchParameters(this.getSearchParameters());

		// Set visbility for all search fields
		UI.setVisible("hardwareLabel", this.detailsMode);
		UI.setVisible("hardware", this.detailsMode);
		UI.setVisible("originalModuleFormatLabel", this.detailsMode);
		UI.setVisible("originalModuleFormat", this.detailsMode);
		UI.setVisible("replayFrequencyLabel", this.detailsMode);
		UI.setVisible("replayFrequency", this.detailsMode);
		UI.setVisible("addressesLabel", this.detailsMode);
		UI.setVisible("addresses", this.detailsMode);
		UI.setVisible("filePathLabel", this.detailsMode);
		UI.setVisible("filePath", this.detailsMode);
		UI.setVisible("fileSizeLabel", this.detailsMode);
		UI.setVisible("fileSize", this.detailsMode);
		UI.setVisible("demozooIDLabel", this.detailsMode);
		UI.setVisible("demozooID", this.detailsMode);
		UI.setVisible("saveExtensionsLabel", this.detailsMode);
		UI.setVisible("saveExtensions", this.detailsMode);
	}

	toggleDetailsMode() {
		this.setDetailsMode(!this.detailsMode);
		this.clearSearchResult();
	}

	clearCurrentFile() {
		this.setCurrentFileIndexAndSong(FileIndex.NONE, SongIndex.NONE);
	}

	clearSearchFields() {
		for (let searchField of this.searchFields) {
			searchField.element.value = searchField.defaultValue;
		}

	}
	clearSearchResult() {
		let searchResult = UI.getElementById("searchResult");
		searchResult.innerHTML = "";
		this.currentSelectedIndex = undefined;
	}
	getSearchParameters() {
		return {
			searchKeyword: UI.getElementById("searchKeyword").value,
			searchFilePath: UI.getElementById("searchFilePath").value,
			searchTitle: UI.getElementById("searchTitle").value,
			searchAuthor: UI.getElementById("searchAuthor").value,
			searchDate: UI.getElementById("searchDate").value,
			searchHardware: UI.getElementById("searchHardware").value,
			searchChannels: UI.getElementById("searchChannels").value,
			searchFormat: UI.getElementById("searchFormat").value
		};
	}

	setSearchField(id, searchParameters) {
		let label = document.getElementById(id + "Label");
		UI.setVisible("searchAuthorLabel", this.detailsMode);
		let field = UI.getElementById(id);
		field.placeholder = (this.detailsMode ? "" : label.innerHTML);
		UI.setVisible("searchAuthor", this.detailsMode || searchParameters[id] != "");
	}

	setSearchParameters(searchParameters) {

		UI.setVisible("searchKeywordLabel", this.detailsMode);
		let searchKeyword = UI.getElementById("searchKeyword");
		searchKeyword.placeholder = (this.detailsMode ? "" : "Keyword");
		UI.setVisible("searchButtonLabel", this.detailsMode);

		UI.setVisible("searchFilePathLabel", false);
		UI.setVisible("searchFilePath", false);

		UI.setVisible("searchTitleLabel", this.detailsMode);
		UI.setVisible("searchTitle", this.detailsMode);
		this.setSearchField("searchAuthor", searchParameters);

		UI.setVisible("searchDateLabel", this.detailsMode);
		UI.setVisible("searchDate", this.detailsMode);

		UI.setVisible("searchHardwareLabel", this.detailsMode);
		UI.setVisible("searchHardware", this.detailsMode);
		UI.setVisible("searchChannelsLabel", this.detailsMode);
		UI.setVisible("searchChannels", this.detailsMode);
		UI.setVisible("searchFormatLabel", this.detailsMode);
		UI.setVisible("searchFormat", this.detailsMode);

		UI.getElementById("searchKeyword").value = searchParameters.searchKeyword;
		UI.getElementById("searchFilePath").value = searchParameters.searchFilePath;
		UI.getElementById("searchTitle").value = searchParameters.searchTitle;
		UI.getElementById("searchAuthor").value = searchParameters.searchAuthor;
		UI.getElementById("searchDate").value = searchParameters.searchDate;
		UI.getElementById("searchHardware").value = searchParameters.searchHardware;
		UI.getElementById("searchChannels").value = searchParameters.searchChannels;
		UI.getElementById("searchFormat").value = searchParameters.searchFormat;
	}

	search() {
		this.searchUsingParameters(this.getSearchParameters());
	}

	searchUsingParameters(searchParameters) {

		const wasPlaying = asapWeb.isPaused() == false;
		if (wasPlaying) {
			asapWeb.togglePause();
		}

		this.setSearchParameters(searchParameters);

		let searchKeyword = searchParameters.searchKeyword.toLowerCase();
		let searchFilePath = searchParameters.searchFilePath.toLowerCase();
		let searchTitle = searchParameters.searchTitle.toLowerCase();
		let searchAuthor = searchParameters.searchAuthor.toLowerCase();
		let searchDate = searchParameters.searchDate;
		let searchHardware = searchParameters.searchHardware;
		let searchChannels = searchParameters.searchChannels;
		let searchFormat = searchParameters.searchFormat.toLowerCase();

		let searchResult = UI.getElementById("searchResult");
		let index = FileIndex.NONE;
		let foundIndex = 0;
		let foundFileInfos = [];
		let fileInfo = null;
		for (fileInfo of this.fileInfos) {
			index += 1;
			fileInfo.fileIndex = index;
			if (searchKeyword != "") {
				if ((!fileInfo.getFilePath().toLowerCase().includes(searchKeyword)) &&
					(!fileInfo.title.toLowerCase().includes(searchKeyword)) &&
					(!fileInfo.author.toLowerCase().includes(searchKeyword)) &&
					(!fileInfo.date.toLowerCase().includes(searchKeyword))) {
					continue;
				}
			};

			if (searchTitle != "") {
				if (!fileInfo.title.toLowerCase().includes(searchTitle)) {
					continue;
				}
			};

			if (searchFilePath != "") {
				if (!fileInfo.getFilePath().toLowerCase().includes(searchFilePath)) {
					continue;
				}
			};

			if (searchAuthor != "") {
				if (!fileInfo.author.toLowerCase().includes(searchAuthor)) {
					continue;
				}
			};

			if (searchDate != "") {
				if (!fileInfo.date.toLowerCase().includes(searchDate)) {
					continue;
				}
			};

			switch (searchHardware) {
				case "1":
					break;
				case "2":
					if (fileInfo.hardware != Hardware.ATARI2600)
						continue;
					break;
				case "3":
					if (fileInfo.hardware != Hardware.ATARI800)
						continue;
					break;
			};
			switch (searchChannels) {
				case "1":
					break;
				case "2":
					if (fileInfo.channels != "1")
						continue;
					break;
				case "3":
					if (fileInfo.channels != "2")
						continue;
					break;
			};

			if (searchFormat != "") {
				if ((!fileInfo.originalModuleExtDescription.toLowerCase().includes(searchFormat)) &&
					(!fileInfo.originalModuleExt.toLowerCase().includes(searchFormat))) {
					continue;
				}
			}
			foundFileInfos[foundIndex++] = fileInfo;
		};

		let resultHTML;
		resultHTML = "<p>" + foundIndex + " of " + this.fileInfos.length + " matching files. Click to play.";
		//if (this.detailsMode) {
		//  resultHTML+= " If something recent is missing, try clearing the browser cache (CTRL-F5).";
		//}
		resultHTML += "</p>";
		resultHTML += '<table id="searchResultTable" class="search_result_table sortable">';
		resultHTML += "<tr>";
		resultHTML += '<th id="searchResultTableTitleHeader" class="search_result_header sorttable_alpha">Title</th><th class="search_result_header sorttable_alpha">Author</th>';
		if (this.detailsMode) {
			resultHTML += '<th class="search_result_header sorttable_alpha">Date</th>';
		}
		if (this.detailsMode) {
			resultHTML += '<th class="search_result_header sorttable_alpha">Channels</th><th class="search_result_header">Format</th><th class="search_result_header sorttable_alpha">Songs (Default)</th>';
		}
		resultHTML += "</tr>";

		for (fileInfo of foundFileInfos) {
			let dateParts = fileInfo.date.split("/").reverse();
			let dateSortKey = dateParts.join("/");
			resultHTML += "<tr id=\"searchResultIndex" + fileInfo.getFileIndex() + "\" onclick=\"asmaInstance.playOrStopAtIndex(" + fileInfo.getFileIndex() + "," + fileInfo.defaultSongIndex + ")\">";
			resultHTML += "<td class=\"search_result_cell\" title=\"" + fileInfo.getFilePath() + "\" sorttable_customkey=\"" + fileInfo.title + "\">" + UI.encodeHTML(fileInfo.title) + "</td>";
			resultHTML += "<td class=\"search_result_cell\">" + UI.encodeHTML(fileInfo.author) + "</td>";
			if (this.detailsMode || searchDate != "") {
				resultHTML += "<td class=\"search_result_cell\" style=\"text-align:right\"sorttable_customkey=\"" + dateSortKey + "\">" + UI.encodeHTML(fileInfo.date) + "</td>";
			}
			if (this.detailsMode || searchChannels != "1") {
				resultHTML += "<td class=\"search_result_cell\">" + fileInfo.getChannelsText() + "</td>";
			}
			if (this.detailsMode || searchFormat != "") {
				resultHTML += "<td class=\"search_result_cell\">" + fileInfo.getOriginalModuleFormatText() + "</td>";
			}
			if (this.detailsMode) {
				resultHTML += "<td class=\"search_result_cell\">" + fileInfo.getSongsText() + "</td>";
			}
			resultHTML += "</tr>";
		}

		resultHTML += "</table>";
		searchResult.innerHTML = resultHTML;
		let newTableObject = UI.getElementById("searchResultTable");
		sorttable.makeSortable(newTableObject);
		UI.getElementById("searchResultTableTitleHeader").click();

		if (wasPlaying) {
			asapWeb.togglePause();
		}

		if (foundIndex == 1) {
			this.playASMA(fileInfo.getFileIndex(), SongIndex.NONE); // Chrome will not allow audio without user gesture
		}
	}

	downloadPlayList() {
		//   header('Content-Type: text/plain; name="playlist.m3u"');
		// List file paths
		//   header('Pragma: public');
		//   header('Cache-Control: no-store, no-cache, must-revalidate'); // HTTP/1.1
		//   header('Cache-Control: pre-check=0, post-check=0, max-age=0'); // HTTP/1.1
		//   header('Content-Transfer-Encoding: none');
		//   header('Content-Type: application/force-download; name="playlist.m3u"'); // This should work for IE & Opera
		//  header('Content-Disposition: attachment; filename="playlist.m3u"');
		//   header("Content-length: $fsize");
	}

	setRowClassList(index, classList) {
		let row = document.getElementById("searchResultIndex" + index);
		if (row != undefined) {
			for (let i = 0; i < row.cells.length; i++) {
				row.cells[i].classList = classList;
			}
		}
	}
	clearSelectedIndexRow() {
		this.setSelectedIndexRow(FileIndex.NONE);
	}
	setSelectedIndexRow(index) {
		if (this.currentSelectedIndex == index) {
			return;
		}
		if (this.currentSelectedIndex != undefined) {
			this.setRowClassList(this.currentSelectedIndex, "");
		}
		this.setRowClassList(index, "search_result_selected_row");
		this.currentSelectedIndex = index;
	}

	playOrStopAtIndex(fileIndex, songIndex) {
		if (this.currentSongState == SongState.PLAYING && fileIndex == this.currentFileIndex && songIndex == this.currentSongIndex) {
			this.stopCurrentSong();
			this.currentSongIndex = null;
		} else {
			this.playASMA(fileIndex, songIndex, true);
		}
	}

	getPlaySongButtonId(songIndex) {
		return "playSong" + songIndex + "Button";
	}

	setCurrentFileInfo(fileInfo) {
		this.currentFileInfo = fileInfo;

		let replayFrequency = UI.getElementById("replayFrequency");
		let addresses = UI.getElementById("addresses");
		let title = "None";
		let author = "None";
		let comment = "";
		let hardware = "";
		let date = "";
		if (this.currentFileInfo == null) {
			this.currentSongState = SongState.NONE;
			UI.getElementById("title").innerHTML = UI.encodeHTML(title);
			UI.getElementById("author").innerHTML = UI.encodeHTML(author);
			UI.getElementById("date").innerHTML = UI.encodeHTML(date);

			UI.getElementById("channels").innerHTML = "";
			UI.getElementById("details").innerHTML = UI.encodeHTML(comment);
			UI.getElementById("hardware").innerHTML = UI.encodeHTML(hardware);
			UI.getElementById("originalModuleFormat").innerHTML = "";
			UI.getElementById("songList").innerHTML = "";
			replayFrequency.innerhtml = "";
			addresses.innerhtml = "";
			UI.getElementById("filePath").innerHTML = "";
			UI.getElementById("fileSize").innerHTML = "";
			UI.getElementById("demozooID").innerHTML = "";
			UI.getElementById("saveExtensions").innerHTML = "";

			UI.getElementById("editButton").disabled = "true";
			UI.getElementById("playButton").disabled = "true";
			UI.getElementById("pauseButton").disabled = "true";
			UI.getElementById("stopButton").disabled = "true";



		} else {

			comment = fileInfo.comment;
			switch (fileInfo.hardware) {
				case Hardware.ATARI2600:
					hardware = "Atari 2600";
					break;
				case Hardware.ATARI800:
					hardware = "Atari 8-bit";
					break;
				default:
					throw "Unknown hardware " + fileInfo.hardware;
			}

			let asapInfo = this.currentFileInfo.getASAPInfo();
			let canPlay = (asapInfo != null);

			if (asapInfo == null) {
				title = fileInfo.title;
				author = fileInfo.author;
				date = fileInfo.date;
				UI.getElementById("channels").innerHTML = this.currentFileInfo.getChannelsText();
				UI.getElementById("originalModuleFormat").innerHTML = this.currentFileInfo.getOriginalModuleFormatText();
				UI.getElementById("songList").innerHTML = "";
				replayFrequency.innerhtml = "";
				addresses.innerhtml = "";
			} else {
				// There is ASAPInfo
				title = asapInfo.getTitle() == "" ? fileInfo.title : asapInfo.getTitle();
				author = asapInfo.getAuthor() == "" ? fileInfo.author : asapInfo.getAuthor();
				date = asapInfo.getDate() == "" ? fileInfo.date : asapInfo.getDate();
				UI.getElementById("channels").innerHTML = this.currentFileInfo.getChannelsText();
				UI.getElementById("originalModuleFormat").innerHTML = this.currentFileInfo.getOriginalModuleFormatText();
				let songListHTML = "";
				for (let songIndex = 0; songIndex < this.currentFileInfo.songs; songIndex++) {
					let id = this.getPlaySongButtonId(songIndex);
					let songText = (songIndex + 1).toString();
					if (this.currentFileInfo.songs > 1 && songIndex == this.currentFileInfo.defaultSongIndex) {
						songText += " (Default)";
					}
					songListHTML += '<input id="' + id + '" type="button" value="' + songText + '" onclick="asmaInstance.playASMA(' + this.currentFileIndex + ',' + songIndex + ',true)">';
				}

				UI.getElementById("songList").innerHTML = songListHTML;

				let replayFrequencyString = (asapInfo.getPlayerRateHz() + " Hz / " + asapInfo.getPlayerRateScanlines() + " scanlines (" + (asapInfo.ntsc ? "NTSC" : "PAL") + ")");
				let addressesString = Util.getAddressHexString(asapInfo.getInitAddress()) + " / " + Util.getAddressHexString(asapInfo.getPlayerAddress()) + " / " + Util.getAddressHexString(asapInfo.getMusicAddress());
				replayFrequency.innerHTML = replayFrequencyString;
				addresses.innerHTML = (addressesString);
			}

			let authorsHTML = "";
			let composerProxies = fileInfo.getComposerProxies();
			let composerCount = composerProxies.length;
			if (composerCount == 0) {
				authorsHTML = UI.encodeHTML(author);
			} else {
				for (let i = 0; i < composerCount; i++) {
					let composerProxy = composerProxies.at(i);
					let fullNameUnicode = composerProxy.getFullNameUnicode();

					let author = fullNameUnicode;
					if (composerProxy.getHandles() != "") {
						author += " (" + composerProxy.getHandles() + ")";
					}
					let authorHTML = UI.encodeHTML(author);
					authorHTML = "<a href=\"javascript:asmaInstance.displayAuthorDetails(" + i + ")\">" + authorHTML + "</a>";

					authorsHTML += authorHTML;
					if (i < composerCount - 1) {
						authorsHTML += " &amp; ";
					}
				}
			}

			UI.getElementById("title").innerHTML = UI.encodeHTML(title);
			UI.getElementById("author").innerHTML = authorsHTML;
			UI.getElementById("date").innerHTML = UI.encodeHTML(date);

			UI.getElementById("details").innerHTML = UI.encodeHTML(comment);
			UI.getElementById("hardware").innerHTML = UI.encodeHTML(hardware);

			// Download URL with "#<songNumber>" addition for Demozoo and other references.
			let filePath = fileInfo.getFilePath();
			if ((this.currentFileInfo.songs > 1) && (this.currentSongIndex != this.currentFileInfo.defaultSongIndex)) {
				filePath += "#" + (this.currentSongIndex + 1)
			}
			let sapURL = "https://asma.atari.org/asma/" + filePath;


			UI.getElementById("filePath").innerHTML = "<a href=\"" + sapURL + "\">" + filePath + "</a>";
			UI.getElementById("fileSize").innerHTML = fileInfo.getFileSizeText();
			UI.getElementById("demozooID").innerHTML = this.demozoo.getMusicHTMLForDemozooID(fileInfo.getDemozooID(), title);

			let saveExtensionsString = "";
			for (let i = 0; i < fileInfo.saveExtensions.length; i++) {
				if (i > 0) { saveExtensionsString += " "; }
				saveExtensionsString += "<span class=\"link\" onclick=\"javascript:asmaInstance.exportFileContentWithExtensionIndex(" + i + ")\">" + fileInfo.saveExtensions[i].toUpperCase() + '</span>';
			}
			UI.getElementById("saveExtensions").innerHTML = saveExtensionsString;
			// Enable/disable buttons.
			UI.getElementById("editButton").disabled = null;
			UI.getElementById("playButton").disabled = (canPlay ? null : "true");
			UI.getElementById("pauseButton").disabled = (canPlay && this.currentSongState == SongState.PLAYING ? null : "true");
			UI.getElementById("stopButton").disabled = (canPlay && this.currentSongState == SongState.PLAYING ? null : "true");
		}
	}

	// Special values of songIndex:
	// =>0 - play the song with the index
	// -1 = SongIndex.NONE - don't play the song
	// -2 = SongIndex.DEFAULT - play the default song
	// -3 = SongIndex.RANDOM - play a random song
	setCurrentFileIndexAndSong(fileIndex, songIndex) {
		if (fileIndex == undefined) {
			throw ("Parameter fileIndex is undefined.");
		}
		if (songIndex == undefined) {
			throw ("Parameter songIndex is undefined.");
		}
		this.currentFileIndex = fileIndex;
		this.currentSongIndex = songIndex;

		if (fileIndex != FileIndex.NONE) {
			let fileInfo = this.fileInfos[fileIndex];
			this.currentFileInfo = fileInfo;
			let url = new URL(window.location);
			url.hash = "#/" + this.currentFileInfo.getFilePath();
			if (songIndex != fileInfo.defaultSongIndex && songIndex != SongIndex.DEFAULT) {
				url.hash += "/" + (songIndex + 1); // Convert internal zero-base song index to external one-based song index
			}
			window.history.pushState(null, fileInfo.title, url);
			this.loadFileContent(fileInfo, this.onFileContentLoadSuccess, this.onFileContentLoadFailure, songIndex);
		}
		else {
			// document.location.assign("/#/");
			this.setCurrentFileInfo(null);
		}
	}

	playASMA(fileIndex, songIndex) {
		this.setCurrentFileIndexAndSong(fileIndex, songIndex); // Will call playSong() if songIndex >=0
		this.setSelectedIndexRow(fileIndex);
	}

	playFile(file) {
		Logger.log("Playing local file \"" + file + "\"");
		UI.getElementById("filePath").innerHTML = file.name;

		asapWeb.playFile(file);
	}

	seekPlayingTime() {
		if (this.currentSongState != SongState.PLAYING) {
			return;
		}
		let playingTimeSlider = UI.getElementById("playingTimeSlider");
		let position = playingTimeSlider.value;
		asapWeb.seek(position);
	}

	// asma is passed as parameter, because "this" is the window in case of an interval event handler
	refreshCurrentASAPInfo(asma) {
		asma.onRefreshCurrentASAPInfo();
	}

	onRefreshCurrentASAPInfo() {

		let playingTime = UI.getElementById("playingTime");
		let playingTimeSlider = UI.getElementById("playingTimeSlider");
		let sliderVisible = true;
		let sliderMin = 0;
		let sliderMax = 0;
		let sliderValue = 0;

		let windowTitleString = DEFAULT_WINDOW_TITLE;

		let songTitleString = "";
		if (this.currentFileInfo != null) {
			songTitleString = this.currentFileInfo.title + " by " + this.currentFileInfo.author;
		}

		let durationString = null;
		switch (this.currentSongState) {
			case SongState.NONE:
				durationString = "";
				sliderVisible = false;
				break;

			case SongState.PLAYING:
				windowTitleString = songTitleString;
				for (let i = 0; i < this.currentFileInfo.songs; i++) {
					let id = this.getPlaySongButtonId(i);
					let playSongButton = UI.getElementById(id);
					let classList = (i == this.currentSongIndex ? 'player_song_button_active' : '');
					playSongButton.classList = classList;
				}
				const asapInfo = this.currentFileInfo.getASAPInfo();

				if (asapInfo != undefined) {
					const duration = asapInfo.getDuration(this.currentSongIndex);
					const loop = asapInfo.getLoop(this.currentSongIndex);
					let position = asapWeb.asap.getPosition();
					if (duration > 0) {
						// If the defined duration [milli seconds] is reached, play new random song?
						if (this.isShuffleMode() && position > duration) {
							this.playRandomSong();
							return;
						}
						position %= duration;
					} else {
						// If the maximum duration [milli seconds] is reached, play new random song?
						if (this.isShuffleMode() && position > 2 * 60 * 1000) {
							this.playRandomSong();
							return;
						}
					}
					durationString = Util.getDurationString(position);
					if (asapWeb.isPaused() == true) {
						windowTitleString += " (Paused)";
						durationString += " (Paused)";
					}
					if (duration > 0) {
						durationString += " / " + Util.getDurationString(duration);
						sliderMin = 0;
						sliderMax = duration;
						sliderValue = position;
					}
					if (loop) {
						durationString += " (Loop)";
					}

				}
				break;

			case SongState.STOPPED:
				windowTitleString = songTitleString + "(Stopped)";
				durationString = "Stopped";
				break;
			default:
				throw "Invalid song state " + this.currentSongState;
		} // case

		window.document.title = windowTitleString;
		playingTime.innerHTML = durationString;
		playingTimeSlider.style.display = (sliderVisible ? "" : "none");
		playingTimeSlider.min = sliderMin;
		playingTimeSlider.max = sliderMax;
		playingTimeSlider.value = sliderValue;
	}

	/*
	 * Shuffel Mode Routines
	 */
	isShuffleMode() {
		return this.shuffleMode;
	}

	setShuffleMode(shuffleMode) {
		this.shuffleMode = shuffleMode;
		let shuffleModeButton = UI.getElementById("shuffleModeButton");
		shuffleModeButton.disabled = (this.fileInfos.length == 0);
		shuffleModeButton.style.fontWeight = shuffleMode ? "bold" : "normal";
		shuffleModeButton.style.backgroundColor = shuffleMode ? "lightBlue" : "";

	}

	toggleShuffleMode() {
		this.setShuffleMode(true);
		this.playRandomSong();
	}

	playRandomSong() {
		this.stopCurrentSong();
		if (this.fileInfos.length > 0) {
			let fileIndex = Math.floor(Math.random() * this.fileInfos.length);
			this.playASMA(fileIndex, SongIndex.RANDOM);
		}
	}

	playCurrentSong() {
		Logger.log("Play current song.");
		let songIndex = this.currentSongIndex;
		if (songIndex < 0) {
			songIndex = this.currentFileInfo.getDefaultSongIndex();
		}
		this.currentModuleInfo = this.playASMA(this.currentFileIndex, songIndex, true);
	}

	togglePauseCurrentSong() {
		Logger.log("Toggle pause current song.");
		asapWeb.togglePause();
	}

	setCurrentSongState(songState) {
		this.currentSongState = songState;
		this.setCurrentFileInfo(this.currentFileInfo);
	}

	stopCurrentSong() {
		if (this.currentSongState == SongState.PLAYING) {
			Logger.log("Stopping current song.");
			let playingTime = UI.getElementById("playingTime");
			playingTime.innerHTML = "Stopping";
			asapWeb.stop();
			this.setCurrentSongState(SongState.STOPPED);
			UI.getElementById("playButton").focus();
		}

	}

	stopCurrentSongAndShuffleMode() {
		this.stopCurrentSong();
		this.setShuffleMode(false);
	}

	setFileInfoContent(fileInfo, content) {
		fileInfo.content = content;
		if (fileInfo.hardware == Hardware.ATARI800) {
			const asapInfo = new ASAPInfo();
			try {
				asapInfo.load(fileInfo.getFilePath(), fileInfo.getContent(), fileInfo.getContent().length);
			} catch (exception) {
				throw "Cannot load \"" + fileInfo.getFilePath() + "\": " + exception;
			}
			fileInfo.setASAPInfo(asapInfo);
			fileInfo.saveExtensions = [];
			ASAPWriter.getSaveExts(fileInfo.saveExtensions, fileInfo.getASAPInfo());
		} else {
			fileInfo.setASAPInfo(null);
			fileInfo.saveExtensions = ["ttt"];
		}
	}

	// Loads the song content either from fileInfo.url or from fileInfo.file
	loadFileContent(fileInfo, onLoadCompleted, onLoadFailed, parameter) {
		if (fileInfo.getContent() == null) {
			if (fileInfo.url != null) {
				const request = new XMLHttpRequest();
				request.open("GET", fileInfo.url, true);
				request.responseType = "arraybuffer";
				request.onload = e => {
					if (request.status == 200 || request.status == 0) {
						try {
							this.setFileInfoContent(fileInfo, new Uint8Array(request.response));
						} catch (exception) {
							onLoadFailed(this, fileInfo, parameter);
							return;
						}
						onLoadCompleted(this, fileInfo, parameter);
					} else {
						onLoadFailed(this, fileInfo, parameter);
					}
				};
				request.send();
			} else if (fileInfo.file != null) {
				const reader = new FileReader();
				reader.onload = e => {
					try {
						this.setFileInfoContent(fileInfo, new Uint8Array(e.target.result));
					} catch (exception) {
						onLoadFailed(this, fileInfo, parameter);
						return;
					}
					onLoadCompleted(this, fileInfo, parameter);
				};
				reader.readAsArrayBuffer(fileInfo.file);
			}
		} else {
			onLoadCompleted(this, fileInfo, parameter);
		}

	}

	exportFileContentWithExtensionIndex(extensionIndex) {
		this.stopCurrentSong();
		let fileInfo = this.currentFileInfo;
		return this.exportFileContentWithExtension(fileInfo, fileInfo.saveExtensions[extensionIndex]);
	}

	exportFileContentWithExtension(fileInfo, extension) {
		let targetFilename = getFileNameWithoutExtensionFromFilePath(fileInfo.getFilePath());
		targetFilename += "." + extension;
		Logger.log("Exporting file content of \"" + fileInfo.getFilePath() + "\" with extension \"" + extension + "\".");
		if (fileInfo.getASAPInfo() != null) {
			const asapWriter = new ASMAWriter();
			const tag = true;
			asapWriter.write(targetFilename, fileInfo.getASAPInfo(), fileInfo.getContent(), fileInfo.getContent().length, tag);
		} else {
			const output = fileInfo.getContent();
			const blob = new Blob([output], { type: "application/octet-stream" });
			saveAs(blob, targetFilename);
		}
		return targetFilename;
	}

	onFileContentLoadSuccess(asma, fileInfo, parameter) {
		Logger.log("Loaded \"" + fileInfo.title + "\" from \"" + fileInfo.getFilePath() + "\".");
		asma.setCurrentFileInfo(fileInfo);
		let songIndex = parameter;
		if (songIndex == SongIndex.RANDOM) {
			songIndex = Math.floor(Math.random() * fileInfo.songs);
			asma.currentSongIndex = songIndex; // Write back to global state
		}
		if (songIndex >= 0) {
			switch (fileInfo.hardware) {
				case Hardware.ATARI800:
					asapWeb.playContent(fileInfo.getFilePath(), fileInfo.getContent(), songIndex);
					asma.setCurrentSongState(SongState.PLAYING);
					break;
				case Hardware.ATARI2600:
					asma.setCurrentSongState(SongState.NONE);
					break;
			}
		}
	}

	onFileContentLoadFailure(asma, fileInfo, parameter) {
		window.alert("Loading \"" + fileInfo.getFilePath() + "\" with parameter \"" + parameter + "\" failed");
	}

	playSong(songIndex) {
		Logger.log("Playing \"" + this.currentFileInfo.title + "\", songIndex " + songIndex + ".");
		asapWeb.stop(); // Stop audio without change the asma state/display
		this.currentSongState = SongState.STOPPED;
		this.setCurrentFileIndexAndSong(this.currentFileInfo.getFileIndex(), songIndex);
	}

	onOpenedFileContentLoadSuccess(asma, fileInfo, state) {
		const fileIndex = asma.fileInfos.length;
		const asapInfo = fileInfo.getASAPInfo();
		fileInfo.fileIndex = fileIndex;
		fileInfo.title = asapInfo.getTitle();
		if (fileInfo.title == "") {
			fileInfo.title = titleCase(getFileNameWithoutExtensionFromFilePath(fileInfo.file.name));
		}
		fileInfo.author = asapInfo.getAuthor();
		if (fileInfo.author == "") {
			fileInfo.author = "Unknown";
		}
		fileInfo.date = asapInfo.getDate();
		fileInfo.channels = asapInfo.getChannels();
		fileInfo.fileSize = fileInfo.getContent().length;
		fileInfo.originalModuleExt = asapInfo.getOriginalModuleExt();
		if (fileInfo.originalModuleExt != null) {
			fileInfo.originalModuleExtDescription = ASAPInfo.getExtDescription(fileInfo.getOriginalModuleExt());
		} else {
			fileInfo.originalModuleExt = "???";
			fileInfo.originalModuleExtDescription = "Unknown";
		}
		fileInfo.songs = asapInfo.getSongs();
		fileInfo.defaultSongIndex = asapInfo.getDefaultSong();

		asma.fileInfos[fileInfo.fileIndex] = fileInfo;
		asma.clearSearchFields();
		asma.search();
		asma.playSong(fileIndex);
	}

	onOpenedFileContentLoadFailure(asma, fileInfo, state) {
		window.alert("Loading \"" + fileInfo.getFilePath() + "\" failed");

		state.count -= 1;
		if (state.count == 0) {
			asma.clearSearchFields();
			asma.search();
		}
	}

	openFiles(files) {

		this.stopCurrentSong();

		if (files.length == 0) {
			this.clearCurrentFile();
			this.initFileInfos();
			this.clearSearchResult();
		} else {
			this.fileInfos = [];

			let state = {
				count: files.length
			};

			for (const file of files) {
				let fileInfo = new FileInfo();
				fileInfo.hardware = Hardware.ATARI800;
				fileInfo.filePath = file.name;
				fileInfo.file = file;
				fileInfo.title = getFileNameFromFilePath(file.name);
				fileInfo.author = "";
				fileInfo.date = "";
				fileInfo.comment = "";
				fileInfo.originalModuleExt = "";
				fileInfo.originalModuleExtDescription = "";
				this.loadFileContent(fileInfo, this.onOpenedFileContentLoadSuccess, this.onOpenedFileContentLoadFailure, state);
			}
		}
		this.setShuffleMode(false);
	}

	displayAuthorDetails(composerIndex) {
		let fileInfo = this.currentFileInfo;
		if (fileInfo == null) {
			return;
		}
		let composerProxies = fileInfo.getComposerProxies();
		if (composerIndex >= composerProxies.size) {
			return;
		}
		let composerProxy = composerProxies.at(composerIndex);
		this.displayComposerDetails(composerProxy);
	}

	displayComposerDetails(composerProxy) {

		let dialogTitle = UI.getElementById("displayAuthorDialogTitle");
		let authorFullNameUnicode = UI.getElementById("authorFullNameUnicode");
		let authorHandles = UI.getElementById("authorHandles");
		let authorCountry = UI.getElementById("authorCountry");
		let authorDemozooID = UI.getElementById("authorDemozooID");
		let authorFileCount = UI.getElementById("authorFileCount");
		let authorFolderPath = UI.getElementById("authorFolderPath");

		let title = "Author Details";
		if (composerProxy.getStatus() == ComposerStatus.UNVERIFIED) { title += "(Unverified)"; }
		dialogTitle.innerHTML = title;
		authorFullNameUnicode.innerHTML = UI.encodeHTML(composerProxy.getFullNameUnicode());
		authorHandles.innerHTML = UI.encodeHTML(composerProxy.getHandles());
		authorCountry.innerHTML = Util.getCountryFlagHTML(composerProxy.getCountry());

		let demozooHTML = composerProxy.getDemozooHTML();
		if (demozooHTML != "") {
			authorDemozooID.innerHTML = demozooHTML;
		} else {
			authorDemozooID.innerHTML = "<a href=\"https://demozoo.org/search/?q="+composerProxy.getFullNameUnicode()+"&category=scener\" target=\"blank\">Find Scener</a>, <a href=\"https://demozoo.org/sceners/new/\" target=\"blank\">Create Scener</a>";
		}

		authorFileCount.innerHTML = "<a href=\"javascript:asmaInstance.displayAuthorSongs(" + composerProxy.getComposerIndex() + ")\">" + UI.encodeHTML(composerProxy.getFileCount().toString()) + "</a>";
		authorFolderPath.innerHTML = UI.encodeHTML(composerProxy.getFolderPath());

		UI.showModalDialog(this.displayAuthorDialog);
	}

	displayAuthorSongs(composerIndex) {
		UI.hideModalDialog(this.displayAuthorDialog);
		UI.hideModalDialog(this.aboutDialog);

		let searchParameters = new SearchParameters();
		if (composerIndex === undefined) {
			searchParameters.searchAuthor = this.currentFileInfo.author;
		} else {
			let composerProxy = this.composerList.getComposerProxy(composerIndex);
			let author = composerProxy.getFullName();
			if (author == "<?>") {
				author = composerProxy.handles;
			}
			searchParameters.searchAuthor = author;
		}
		this.searchUsingParameters(searchParameters);
	}

	editCurrentSong() {
		let oldFileInfo = this.currentFileInfo;
		if (oldFileInfo == null || oldFileInfo.getASAPInfo() == null) {
			return;
		}
		this.stopCurrentSong();

		let editFilePath = UI.getElementById("editFilePath");
		let editTitle = UI.getElementById("editTitle");
		let editAuthor = UI.getElementById("editAuthor");
		let editDate = UI.getElementById("editDate");
		let editNTSC = UI.getElementById("editNTSC");

		let fileInfo = Object.assign(new FileInfo(), oldFileInfo);
		editFilePath.innerHTML = UI.encodeHTML(fileInfo.getFilePathWithBreaks());
		editTitle.value = this.currentFileInfo.title;
		editAuthor.value = this.currentFileInfo.author;
		editDate.value = this.currentFileInfo.date;
		editNTSC.value = ASMA.getNTSCPALString(this.currentFileInfo);

		// Get the <span> element that closes the modal
		var closeIcon = UI.getElementById("editSongDialogCloseIcon");

		// When the user clicks on <span> (x), close the modal
		closeIcon.onclick = function() {
			asmaInstance.closeEditSongDialog();
		};

		this.editSongDialog.style.display = "block";
	}

	closeEditSongDialog() {
		this.editSongDialog.style.display = "none";
	}

	downloadEditSongDialog() {
		this.closeEditSongDialog();
	}

	getAttributeLine(label, newValue, oldValue) {
		return `${label} ${newValue == oldValue ? " Unchanged" : "Changed"}: ${newValue}\n`;
	}

	// With mail=true it is "submit"", otherwise it is only "download" of the modified file
	submitEditSongDialog(mail) {
		let editTitle = UI.getElementById("editTitle");
		let editAuthor = UI.getElementById("editAuthor");
		let editDate = UI.getElementById("editDate");
		let editNTSC = UI.getElementById("editNTSC");
		let title = editTitle.value;
		let author = editAuthor.value;
		let date = editDate.value;
		let ntsc = editNTSC.value;
		let fileInfo = this.currentFileInfo;
		let upload = (fileInfo.file != undefined);

		let targetFilePath = this.exportFileContentWithExtension(fileInfo, "sap");
		if (mail) {
			let emailTo = "asma-team@googlegroups.com";
			let emailSubject = "ASMA: " + (upload ? "Upload" : "Update ") + " " + fileInfo.getFilePath();
			let emailBody = this.getAttributeLine("Title", title, fileInfo.title)
				+ this.getAttributeLine("Author", author, fileInfo.author)
				+ this.getAttributeLine("Date", date, fileInfo.date)
				+ this.getAttributeLine("NTSC/PAL", ntsc, ASMA.getNTSCPALString(fileInfo))
				+ "Please attach the generated \"" + targetFilePath + "\" file from your Downloads folder.";
			let mailWindow = window.open(`mailto:${emailTo}?subject=${encodeURI(emailSubject)}&body=${encodeURI(emailBody)}`, '_self');
			if (!mailWindow) {
				window.alert("No mail program available.");
			}
			this.closeEditSongDialog();
		}
	}

	cancelEditSongDialog() {
		this.closeEditSongDialog();
	}

	displayAboutDialog() {

		let aboutDialogGeneralContent = UI.getElementById("aboutDialogGeneralContent");
		aboutDialogGeneralContent.innerHTML = "<iframe id=\"aboutDialogGeneralContentFrame\" src=\"../asma/asma.txt\" height=\"90px\" style=\"border:none;\"></iframe>" +
			"<div><pre> <a href=\"https://asap.sourceforge.net/\">ASAP version " + ASAPInfo.VERSION + "</a>.</pre></div>";

		let aboutDialogASMAContent = UI.getElementById("aboutDialogASMAContent");
		aboutDialogASMAContent.innerHTML = this.demozooSummary.asma;

		let aboutDialogDemozooContent = UI.getElementById("aboutDialogDemozooContent");
		aboutDialogDemozooContent.innerHTML = this.demozooSummary.demozoo;
		this.demozooSummary.demozoo;


		let resultHTML;
		resultHTML = "";
		resultHTML += '<table id="composersByNameTable" class="statistic_table sortable">';
		resultHTML += "<tr>";
		if (this.detailsMode) {
			resultHTML += '<th id="composersByNameTableSortHeader" class="statistic_header sorttable_alpha">Last Name</th>';
			resultHTML += '<th class="statistic_header sorttable_alpha">First Name</th>';
			resultHTML += '<th class="statistic_header sorttable_alpha">Handles</th>';
		} else {
			resultHTML += '<th id="composersByNameTableSortHeader" class="statistic_header sorttable_alpha">Full Name (Handles)</th>';
		}
		resultHTML += '<th class="statistic_header sorttable_alpha">Country</th>';
		resultHTML += '<th class="statistic_header sorttable_numeric">Demozoo ID</th>';
		resultHTML += '<th class="statistic_header_numeric sorttable_numeric">File Count</th>';
		resultHTML += "</tr>";
		for (let composerProxy of this.composerList.composerProxies) {
			if (composerProxy.getStatus() != ComposerStatus.VERIFIED) { continue; } // Only verified composers

			resultHTML += "<tr>";
			if (this.detailsMode) {
				resultHTML += "<td sorttable_customkey=\"" + composerProxy.getLastNameSortKey() + "\" title=\"" + UI.encodeHTML(composerProxy.getFolderName()) + "\">" + UI.encodeHTML(composerProxy.getLastNameUnicode()) + "</td>";
				resultHTML += "<td class=\"statistic_cell\">" + UI.encodeHTML(composerProxy.getFirstNameUnicode()) + "</td>";
				resultHTML += "<td class=\"statistic_cell\">" + UI.encodeHTML(composerProxy.getHandles()) + "</td>";
			} else {
				let fullName = composerProxy.getFullNameUnicode();
				let handles = composerProxy.getHandles();
				if (handles != "") {
					fullName += " (" + handles + ")";
				}
				resultHTML += "<td class=\"statistic_cell\">" + UI.encodeHTML(fullName) + "</td>";
			}
			resultHTML += "<td class=\"statistic_cell\">" + Util.getCountryFlagHTML(composerProxy.getCountry()) + "</td>";
			resultHTML += "<td class=\"statistic_cell\" sorttable_customkey=\"" + composerProxy.getDemozooID() + "\">" + composerProxy.getDemozooHTML() + "</td>";
			resultHTML += "<td class=\"statistic_cell_numeric\">" + composerProxy.getFileCountHTML() + "</td>";

			resultHTML += "</tr>";
		}

		resultHTML += "</table>";
		UI.initResult("composersByName", resultHTML);

		resultHTML = "";
		resultHTML += '<table id="composersByCountryTable" class="statistic_table sortable">';
		resultHTML += "<tr>";
		resultHTML += '<th id="composersByCountryTableSortHeader" class="statistic_header_numeric sorttable_numeric">Composers</th>';
		resultHTML += '<th class="statistic_header sorttable_alpha">Country</th>';
		resultHTML += "</tr>";
		for (let countryInfo of this.composerList.countryInfoMap) {
			resultHTML += "<tr>";
			let normalizedCountry = countryInfo[1].normalizedCountry;
			resultHTML += "<td class=\"statistic_cell_numeric\">" + UI.encodeHTML(countryInfo[1].composerCount.toString()) + "</td>";
			resultHTML += "<td class=\"statistic_cell\">" + Util.getCountryFlagHTML(normalizedCountry) + "</td>";
			resultHTML += "</tr>";
		}

		resultHTML += "</table>";
		UI.initResult("composersByCountry", resultHTML, true);

		UI.showModalDialog(aboutDialog);
	}

	fetchDemozoo() {
		this.demozoo.fetchDemozoo();
	}
}

class SearchParameters {
	constructor() {
		this.searchKeyword = "";
		this.searchFilePath = "";
		this.searchTitle = "";
		this.searchAuthor = "";
		this.searchDate = "";
		this.searchHardware = "1";
		this.searchChannels = "1";
		this.searchFormat = "";
	}
}

function stripExtensionFromFilePath(filePath) {
	let result = filePath;
	let lastPeriodIndex = result.lastIndexOf(".");
	if (lastPeriodIndex >= 0) { result = result.substring(0, lastPeriodIndex); }
	return result;
}

function getFileNameFromFilePath(filePath) {
	let result = filePath;
	let lastSlashIndex = result.lastIndexOf("/");
	if (lastSlashIndex >= 0) { result = result.substr(lastSlashIndex + 1); }
	return result;
}

function getFileNameWithoutExtensionFromFilePath(filePath) {
	return stripExtensionFromFilePath(getFileNameFromFilePath(filePath));
}


function titleCase(string) {
	return string[0].toUpperCase() + string.slice(1).toLowerCase();
}

window.asmaInstance = new ASMA();
