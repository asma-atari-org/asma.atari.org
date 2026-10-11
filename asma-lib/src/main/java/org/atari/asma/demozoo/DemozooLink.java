package org.atari.asma.demozoo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DemozooLink {

	public static String getMusic(int productionID) {
		return "https://demozoo.org/music/" + productionID;
	}

	public static String getScener(int scenerID) {
		return "https://demozoo.org/sceners/" + scenerID;
	}

	// Not used by the website; the route of Demozoo's group pages.
	public static String getGroup(int groupID) {
		return "https://demozoo.org/groups/" + groupID;
	}

	// Like the website's "Find Scener" link (asma.js), category "scener", "group" or "music".
	public static String getFind(String query, String category) {
		StringBuilder builder = new StringBuilder();
		addParameter(builder, "q", query);
		addParameter(builder, "category", category);
		return "https://demozoo.org/search/?" + builder.toString();
	}

	private static void addParameter(StringBuilder builder, String key, String value) {
		if (value != null & !value.isEmpty()) {

			if (builder.length() > 0) {
				builder.append("&");
			}
			try {
				value = URLEncoder.encode(value.trim(), "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex);
			}
			builder.append(key).append("=").append(value);
		}
	}

	public static String getFindMusic(String title) {
		StringBuilder builder = new StringBuilder();
		addParameter(builder, "q", title.trim() + " type:music");
		return "https://demozoo.org/search/?" + builder.toString();
	}

	/**
	 * The URL to create music productions on Demozoo require the TamperMonkey user
	 * script from https://asma.atari.org/asmadb/demozoo-music-new.js
	 **/
	public static String getNewMusic(int scenerID, String title, String releaseDate, String url) {
		StringBuilder builder = new StringBuilder();

		if (scenerID > 0) {
			addParameter(builder, "releaser_id", String.valueOf(scenerID));
		}
		addParameter(builder, "title", title);
		addParameter(builder, "release_date", releaseDate);
		addParameter(builder, "url", url);

		return "https://demozoo.org/music/new/?" + builder.toString();
	}

	public static String getEditDownloadLinks(int productionID) {
		return "https://demozoo.org/productions/" + productionID + "/edit_download_links/";
	}
}