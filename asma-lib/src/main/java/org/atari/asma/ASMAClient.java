package org.atari.asma;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.atari.asma.demozoo.ASMAProduction;
import org.atari.asma.util.FileUtility;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Client to consume the published content of the ASMA website from Java.
 *
 * The database is loaded from "asmadb/asmadb.js" (or "asmadb/asmadb.json") and
 * parsed into {@link Database}. The module files themselves are available via
 * {@link #getFileURL(FileInfo)} and {@link #downloadFile(FileInfo)}.
 */
public final class ASMAClient {

	public static final String DEFAULT_BASE_URL = "https://asma.atari.org/";
	public static final String DATABASE_JS_PATH = "asmadb/asmadb.js";
	public static final String DATABASE_JSON_PATH = "asmadb/asmadb.json";
	public static final String ARCHIVE_PATH = "asma/";

	// Prefix of the JavaScript variant of the database, see ASMADatabase.writeJS().
	private static final String JS_PREFIX = "const asma =";

	/**
	 * The content of the ASMA database as published on the website. The field
	 * names correspond to the attribute names in "asmadb.js". Default values
	 * omitted during export are applied after parsing, see applyDefaults().
	 */
	public static final class Database {
		public List<Composer> composerInfos;
		public List<Group> groupInfos;
		public List<FileInfo> fileInfos;
		public DemozooContent demozoo;

		public static final class DemozooContent {
			public List<ASMAProduction> productions;
		}
	}

	private final String baseURL;

	public ASMAClient() {
		this(DEFAULT_BASE_URL);
	}

	public ASMAClient(String baseURL) {
		if (baseURL == null) {
			throw new IllegalArgumentException("Parameter baseURL must not be null.");
		}
		if (!baseURL.endsWith("/")) {
			baseURL = baseURL + "/";
		}
		this.baseURL = baseURL;
	}

	public String getBaseURL() {
		return baseURL;
	}

	// Load the database from the website this client points to.
	public Database loadDatabase() throws IOException {
		return parseDatabase(new String(download(baseURL + DATABASE_JS_PATH), StandardCharsets.UTF_8));
	}

	// Load the database from a local "asmadb.js" or "asmadb.json" file.
	public Database loadDatabase(File file) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("Parameter file must not be null.");
		}
		return parseDatabase(FileUtility.readAsString(file));
	}

	// Parse the database from the content of an "asmadb.js" or "asmadb.json" file.
	public Database parseDatabase(String content) throws IOException {
		if (content == null) {
			throw new IllegalArgumentException("Parameter content must not be null.");
		}
		String jsonString = content.stripLeading();
		if (jsonString.startsWith(JS_PREFIX)) {
			jsonString = jsonString.substring(JS_PREFIX.length());
		}
		Database database;
		try {
			database = new GsonBuilder().create().fromJson(jsonString, Database.class);
		} catch (JsonSyntaxException ex) {
			throw new IOException("Cannot parse ASMA database: " + ex.getMessage(), ex);
		}
		if (database == null) {
			throw new IOException("Cannot parse ASMA database: content is empty.");
		}
		applyDefaults(database);
		return database;
	}

	// Apply the default values that the export omits to save space,
	// mirroring the logic in "asma.js".
	private static void applyDefaults(Database database) {
		if (database.fileInfos == null) {
			return;
		}
		for (FileInfo fileInfo : database.fileInfos) {
			if (fileInfo.hardware == null) {
				fileInfo.hardware = FileInfo.ATARI800;
			}
			if (fileInfo.channels == 0) {
				fileInfo.channels = 1;
			}
			if (fileInfo.songs == 0) {
				fileInfo.songs = 1;
			}
		}
	}

	// The URL under which the module file of the given file info is published.
	public String getFileURL(FileInfo fileInfo) {
		if (fileInfo == null) {
			throw new IllegalArgumentException("Parameter fileInfo must not be null.");
		}
		return baseURL + ARCHIVE_PATH + fileInfo.filePath;
	}

	// Download the module file of the given file info.
	public byte[] downloadFile(FileInfo fileInfo) throws IOException {
		return download(getFileURL(fileInfo));
	}

	private static byte[] download(String url) throws IOException {
		try (InputStream inputStream = new URI(url).toURL().openStream()) {
			return inputStream.readAllBytes();
		} catch (URISyntaxException ex) {
			throw new IOException("Invalid URL \"" + url + "\": " + ex.getMessage(), ex);
		}
	}
}
