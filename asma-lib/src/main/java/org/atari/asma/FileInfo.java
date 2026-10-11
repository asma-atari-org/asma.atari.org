package org.atari.asma;

import java.io.IOException;

import org.atari.asma.demozoo.MusicDemozooID;
import org.atari.asma.util.JSONWriter;
import com.wudsn.tools.base.common.MessageQueue;

import com.wudsn.tools.base.repository.Attribute;

public final class FileInfo {

	/**
	 * A selection (filter) over file infos, mirroring the search form of the
	 * website player (asma.js). Empty strings mean "no restriction"; a
	 * <code>null</code> hardware means all hardware, channels 0 means all
	 * channel counts. String matching is a case-insensitive "contains"; the
	 * keyword matches against file path, title, author and date.
	 */
	public static record Selection(String keyword, String filePath, String title, String author, String date,
			String hardware, int channels, String format) {

		public static final Selection ALL = new Selection("", "", "", "", "", null, 0, "");

		public Selection {
			keyword = normalize(keyword);
			filePath = normalize(filePath);
			title = normalize(title);
			author = normalize(author);
			date = normalize(date);
			format = normalize(format);
		}

		private static String normalize(String value) {
			return (value == null) ? "" : value.toLowerCase();
		}

		private static boolean contains(String value, String part) {
			return value != null && value.toLowerCase().contains(part);
		}

		public boolean matches(FileInfo fileInfo) {
			if (fileInfo == null) {
				throw new IllegalArgumentException("Parameter 'fileInfo' must not be null.");
			}
			if (!keyword.isEmpty() && !(contains(fileInfo.filePath, keyword) || contains(fileInfo.title, keyword)
					|| contains(fileInfo.author, keyword) || contains(fileInfo.date, keyword))) {
				return false;
			}
			if (!filePath.isEmpty() && !contains(fileInfo.filePath, filePath)) {
				return false;
			}
			if (!title.isEmpty() && !contains(fileInfo.title, title)) {
				return false;
			}
			if (!author.isEmpty() && !contains(fileInfo.author, author)) {
				return false;
			}
			if (!date.isEmpty() && !contains(fileInfo.date, date)) {
				return false;
			}
			if (hardware != null && !hardware.equals(fileInfo.hardware)) {
				return false;
			}
			if (channels != 0 && fileInfo.channels != channels) {
				return false;
			}
			if (!format.isEmpty() && !contains(fileInfo.originalModuleExt, format)) {
				return false;
			}
			return true;
		}
	}

	/**
	 * The attributes of a file info, bound to the gson field names, for use
	 * with WUDSN Base's attribute tables. See plans/07.
	 */
	public static final class Attributes {
		public static final Attribute FILE_PATH = new Attribute("filePath", DataTypes.FileInfo_FilePath);
		public static final Attribute TITLE = new Attribute("title", DataTypes.FileInfo_Title);
		public static final Attribute AUTHOR = new Attribute("author", DataTypes.FileInfo_Author);
		public static final Attribute DATE = new Attribute("date", DataTypes.FileInfo_Date);
		public static final Attribute HARDWARE = new Attribute("hardware", DataTypes.FileInfo_Hardware);
		public static final Attribute ORIGINAL_MODULE_EXT = new Attribute("originalModuleExt",
				DataTypes.FileInfo_OriginalModuleExt);
		public static final Attribute CHANNELS = new Attribute("channels", DataTypes.FileInfo_Channels);
		public static final Attribute SONGS = new Attribute("songs", DataTypes.FileInfo_Songs);
		/** The songs with the default song, as one table column like on the website. */
		public static final Attribute SONGS_AND_DEFAULT_SONG = new Attribute("songsAndDefaultSong",
				DataTypes.FileInfo_SongsAndDefaultSong);
		public static final Attribute DEFAULT_SONG_INDEX = new Attribute("defaultSongIndex",
				DataTypes.FileInfo_DefaultSongIndex);
		public static final Attribute FILE_SIZE = new Attribute("fileSize", DataTypes.FileInfo_FileSize);
		public static final Attribute DEMOZOO_ID = new Attribute("demozooID", DataTypes.FileInfo_DemozooID);

		private Attributes() {
		}
	}

	public static String ATARI800 = "ATARI800"; // Default
	public static String ATARI2600 = "ATARI2600";

	public String filePath; // Relative file path to ASMA root folder
	public long fileSize;
	public String hardware;
	public String title;
	public String author;
	public String date;
	public String comment; // TODO use COMMENT from STIL
	public String typeLetter;;
	public String originalModuleExt;
	public int channels;
	public int songs;
	public int defaultSongIndex; // Zero-based

	private transient MessageQueue messageQueue;

	// TODO Duration and details from STIL per Song
	private int demozooID;

	public FileInfo() {
		messageQueue = new MessageQueue();
	}

	/**
	 * Gets the channels as display text, like getChannelsText() in the
	 * website's file.js: the POKEY channels for Atari 800, TIA for 2600.
	 */
	public String getChannelsText() {
		if (ATARI2600.equals(hardware)) {
			return "Mono (2)";
		}
		return (channels == 1) ? "Mono (4)" : "Stereo (8)";
	}

	/**
	 * Gets the songs as display text, like getSongsText() in the website's
	 * file.js: "1" for a single song, otherwise the song count followed by
	 * the one-based default song in brackets, e.g. "4 (2)".
	 */
	public String getSongsText() {
		if (songs == 1) {
			return "1";
		}
		return songs + " (" + (defaultSongIndex + 1) + ")";
	}

	public String getFolderPath() {
		String result = "";
		var index = filePath.lastIndexOf("/");
		if (index >= 0) {
			result = filePath.substring(0, index);
		}
		return result;
	}

	// The file path format in URLs is:
	// - "filePath" if the file has only one song or if the song is the default song
	// - "filePath#songNumber" if the file more than one song and if the song is not
	// the default song
	public String getURLFilePath(int songNumber, boolean includeDefaultSongSuffix) {
		if ((songs > 0) && (includeDefaultSongSuffix || (songNumber - 1 != defaultSongIndex))) {
			return filePath + "#" + songNumber;
		}
		return filePath;
	}

	// TODO: DemozooID must be array, one value per song
	public int getDemozooID() {
		return demozooID;
	}

	public void setDemozooID(int demozooID) {
		this.demozooID = demozooID;
	}

	/** The Demozoo ID, or <code>null</code> if there is none. See plans/10. */
	public MusicDemozooID getMusicDemozooID() {
		return MusicDemozooID.of(demozooID);
	}

	public MessageQueue getMessageQueue() {
		return messageQueue;
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

		if (demozooID != 0) {
			writer.writeSeparator();
			writer.writeJSONLong("demozooID", demozooID);
		}

		writer.endObject();
	}
}