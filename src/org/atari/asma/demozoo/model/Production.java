package org.atari.asma.demozoo.model;

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

	public String getURLFilePath() {
		for (var link : download_links) {

			if ((link.link_class.equals("BaseUrl")) && (link.url.startsWith(ASMA_URL_PREFIX))) {
				return link.url.substring(ASMA_URL_PREFIX.length());
			}

		}
		return "";

	}
}