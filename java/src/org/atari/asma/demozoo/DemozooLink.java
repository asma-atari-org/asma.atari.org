package org.atari.asma.demozoo;

public class DemozooLink {
	
	public static String getMusic(int productionID) {
		return "https://demozoo.org/music/" + productionID;
	}

	public static String getScener(int scenerID) {
		return "https://demozoo.org/sceners/" + scenerID;
	}

	public static String getEditDownloadLinks(int productionID) {
		return "https://demozoo.org/productions/" + productionID + "/edit_download_links/";
	}
}