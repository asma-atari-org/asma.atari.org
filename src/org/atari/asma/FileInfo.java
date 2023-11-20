package org.atari.asma;

import java.io.IOException;

import org.atari.asma.util.JSONWriter;

import net.sf.asap.ASAP;
import net.sf.asap.ASAPFormatException;
import net.sf.asap.ASAPInfo;

public final class FileInfo {
	public static String ATARI800 = "ATARI800"; // Default
	public static String ATARI2600 = "ATARI2600";

	public String filePath; // Relative file path to ASMA root folder
	public long fileSize;
	public String hardware;
	public String title;
	public String author;
	public String date;
	public String comment;
	public String typeLetter;;
	public String originalModuleExt;
	public int channels;
	public int songs;
	public int defaultSongIndex; // Zero-based
	// TODO Duration and details from STIL per Song

	FileInfo() {
	}

	public void readFromSAPFile(String filePath, byte[] module) {
		// Parse binary.
		ASAP asap = new ASAP();
		try {
			asap.load(filePath, module, module.length);
		} catch (ASAPFormatException ex) {
			// ERROR: Cannot load sound file '{0}'. {1}
			throw new RuntimeException(ex);
		}
		ASAPInfo asapInfo = asap.getInfo();
		this.title = asapInfo.getTitleOrFilename();
		this.author = asapInfo.getAuthor();
		this.date = asapInfo.getDate();

		// Determine the original file type in the container.
		// Only if it cannot be determined, the file extension is used.
		this.typeLetter = String.valueOf((char) asap.getInfo().getTypeLetter());
		this.originalModuleExt = asap.getInfo().getOriginalModuleExt(module, module.length);
		if (this.originalModuleExt == null) {
			this.originalModuleExt = filePath.substring(filePath.lastIndexOf('.') + 1);
		}

		// Translate the file type to a human readable text.
		try {
			ASAPInfo.getExtDescription(this.originalModuleExt);
		} catch (ASAPFormatException ex) {
			throw new RuntimeException(ex);
		}

		this.channels = asapInfo.getChannels();
		this.songs = asapInfo.getSongs();
		this.defaultSongIndex = asapInfo.getDefaultSong();
	}

	public void write(JSONWriter writer) throws IOException {
		if (writer == null) {
			throw new IllegalArgumentException("Parameter writer must not be null.");
		}
		writer.beginObject();
		writer.writeJSONString("filePath", filePath);
		writer.writeSeparator();
		writer.writeJSONLong("fileSize", fileSize);
		writer.writeSeparator();
		// Save space
		if (!hardware.equals(ATARI800)) {
			writer.writeJSONString("hardware", hardware);
			writer.writeSeparator();

		}
		writer.writeJSONString("title", title);
		writer.writeSeparator();

		writer.writeJSONString("author", author);
		writer.writeSeparator();

		writer.writeJSONString("date", date);
		if (comment != null) {
			writer.writeSeparator();
			writer.writeJSONString("comment", comment);
		}
		writer.writeSeparator();
		writer.writeJSONString("typeLetter", typeLetter);
		writer.writeSeparator();
		writer.writeJSONString("originalModuleExt", originalModuleExt);
		if (channels != 1) {
			writer.writeSeparator();
			writer.writeJSONLong("channels", channels);
		}
		if (songs != 1) {
			writer.writeSeparator();
			writer.writeJSONLong("songs", songs);
		}
		if (defaultSongIndex != 0) {
			writer.writeSeparator();
			writer.writeJSONLong("defaultSongIndex", defaultSongIndex);
			if (defaultSongIndex < 0) {
				throw new IllegalArgumentException("Invalid default song index " + defaultSongIndex);
			}
		}
		writer.endObject();
	}
}