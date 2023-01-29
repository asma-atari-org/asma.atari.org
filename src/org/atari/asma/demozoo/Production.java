package org.atari.asma.demozoo;

public final class Production {

	public int id;
	public String title = "";
	public int[] authorIDs = new int[0];
	public String filePath = "";
	public int songIndex = -1;

	public Production() {
	}

	@Override
	public String toString() {
		return id + " - " + title;
	}
}