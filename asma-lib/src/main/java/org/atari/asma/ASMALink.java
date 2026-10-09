package org.atari.asma;

public class ASMALink {

	private ASMALink() {
		
	}
	
	public static String getFile(String filePath) {
		return "https://asma.atari.org/asma/" + filePath;
	}
}
