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
	getOriginalModuleExt() {
		return this.originalModuleExt;
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

	// Zero-based defaut song index.
	getDefaultSongIndex() {
		return this.defaultSongIndex;
	}
	
	getSongs(){
		return this.songs;
	}
	
	getSongsText() {
		if (this.songs == 1) {
			return "1";
		}
		return this.songs + " (" + (this.getDefaultSongIndex() + 1) + ")";
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
	getContent() {
		return this.content;
	}

	getASAPInfo() {
		return this.asapInfo;
	}
	setASAPInfo(asapInfo) {
		this.asapInfo = asapInfo;
	}

	// The file path format in URLs is:
	// - "filePath" if the file has only one song or if the song is the default song
	// - "filePath#songNumber" if the file more than one song and if the song is not
	// the default song
	getURLFilePath(songNumber) {
		if ((this.songs > 0) && (songNumber - 1 != this.defaultSongIndex)) {
			return this.filePath + "#" + songNumber;
		}
		return this.filePath;
	}

	getDemozooID() {
		return this.demozooID;
	}

}

class FileInfoList {
	constructor() {
	}
	// Date and file path validation happens in the Java exporter
	// (org.atari.asma.FileInfoList.checkFileInfos, SAP-1xx messages),
	// so the published database only contains validated entries.
	static init(fileInfos) {
		for (let i = 0; i < fileInfos.length; i++) {
			fileInfos[i] = Object.assign(new FileInfo(), fileInfos[i]);
		}
	}

}


