package org.atari.asma;

public class Group {

	public String folderName = "";
	public String handle = "";
	public String demozooID = "";

	@Override
	public String toString() {
		if (!folderName.isEmpty()) {
			return folderName;
		}
		return handle;
	}
}