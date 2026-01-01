package org.atari.asma.demozoo.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.atari.asma.demozoo.Demozoo.PlatformDefinition;
import org.atari.asma.util.FileUtility;
import org.atari.asma.util.StringUtility;

public final class Production {

	public String url;
	public String demozoo_url;
	public int id;
	public String title = "";
	public AuthorNick[] author_nicks;
	public String release_date;
	public String supertype;
	public Platform[] platforms;
	public ProductionType[] types;
	public Link[] download_links;
	public String[] tags;

	private final static String ASMA_URL_PREFIX = "https://asma.atari.org/asma/";

	public List<String> getASMAURLFilePaths() {
		List<String> result = new ArrayList<String>(2);
		for (var link : download_links) {

			if ((link.link_class.equals("BaseUrl")) && (link.url.startsWith(ASMA_URL_PREFIX))) {
				result.add(link.url.substring(ASMA_URL_PREFIX.length()));
			}

		}
		return result;

	}

	public String getASMADefaultURLFilePath() {
		var urlFilePaths = getASMAURLFilePaths();
		if (urlFilePaths.size() == 1) {
			return urlFilePaths.get(0);
		}
		return "";
	}

	public Set<String> getFileExtensions() {
		Set<String> set = new TreeSet<String>();
		for (var link : download_links) {

			try {
				var uri = new URI(link.url);

				var fileExtension = FileUtility.getFileExtension(uri.getPath());
				if (fileExtension.startsWith(".")) {
					fileExtension = fileExtension.substring(1);
				}
				fileExtension = fileExtension.toLowerCase();

				set.add(fileExtension);
			} catch (URISyntaxException ex) {

			}

		}
		return set;
	}

	public String getHardware() {
		for (var platform : platforms) {
			if (platform.id == PlatformDefinition.ATARI_800) {
				return "ATARI800";
			}
			if (platform.id == PlatformDefinition.ATARI_2600) {
				return "ATARI2600";
			}
		}
		return "";
	}

	public boolean hasTag(String tag) {
		return StringUtility.hasElement(tags, tag);
	}

	public String getTags() {
		return StringUtility.toSortedString(tags);
	}
}