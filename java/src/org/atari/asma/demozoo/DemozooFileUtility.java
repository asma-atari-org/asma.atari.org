package org.atari.asma.demozoo;

import org.atari.asma.util.FileUtility;

public class DemozooFileUtility {
	public static String getNormalizedFileExtension(String fileName) {
		var fileExtension = FileUtility.getFileExtension(fileName);
		if (fileExtension.startsWith(".")) {
			fileExtension = fileExtension.substring(1);
		}
		fileExtension = fileExtension.toLowerCase();
		return fileExtension;
	}

}
