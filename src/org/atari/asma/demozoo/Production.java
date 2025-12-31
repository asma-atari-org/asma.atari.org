package org.atari.asma.demozoo;

public final class Production {

	public int id;
	public String title = "";
	public int[] authorIDs = new int[0];
	public String filePath = "";

	public Production() {
	}

	public String getURL() {
		return "https://demozoo.org/music/" + id;
	}

	@Override
	public String toString() {
		return id + " - " + title + "( " + getURL() + " )";
	}
}