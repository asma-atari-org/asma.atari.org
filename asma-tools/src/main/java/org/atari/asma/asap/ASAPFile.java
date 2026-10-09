package org.atari.asma.asap;
import org.atari.asma.sap.Segment;
import org.atari.asma.sap.SegmentList;

import net.sf.asap.ASAPInfo;

public class ASAPFile {
	// https://asap.sourceforge.net/sap-format.html
	public final static String UPPPER_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public final static String LOWER_LETTERS = "abcdefghijklmnopqrstuvwxyz";
	public final static String NUMBERS = "0123456789";
	public final static String SPECICAL_CHARACTERS = " !\"#$%&'()*+,-./:;<=>?@[\\]^_";
	public final static String ATASCII_CHARACTERS = UPPPER_LETTERS + LOWER_LETTERS + NUMBERS + SPECICAL_CHARACTERS
			+ "|";

	public static boolean isOriginalModuleFileExtension(String fileExtension) {
		var result = fileExtension.startsWith(".");
		if (result) {
			result = ASAPInfo.isOurExt(fileExtension.substring(1));
		}
		return result;
	}

	public static boolean isStringEmpty(String s) {
		return s.trim().isEmpty() || s.trim().equals("<?>");
	}

	public static String normalizeFileName(String fileName) {
		var builder = new StringBuilder(fileName.length());
		int index = fileName.lastIndexOf(".");
		if (index < 0) {
			index = fileName.length();
		}
		for (int i = 0; i < index; i++) {
			var c = fileName.charAt(i);
			if (c == ' ' || c == '-') {
				c = '_';
			}
			if ("0123456789_ABCDEFGHIHJKLMNOPQRSTUVWXYZabcdefghihjklmnopqrstuvwxyz".indexOf(c) < 0) {
				continue;
			}
			builder.append(c);
		}
		if (index < fileName.length()) {
			builder.append(fileName.substring(index).toLowerCase());
		}
		return builder.toString();
	}

	public ASAPFile() {
		segmentList = new SegmentList();
		author = "";
		title = "";
		date = "";
	}

	public byte[] content;

	public SegmentList segmentList;
	private ASAPInfo asapInfo;

	private String author;
	private String title;
	private String date;

	private static String getString(String s) {
		if (s == null || s.isBlank()) {
			return "<?>";
		}
		return s;
	}

	public ASAPInfo getASAPInfo() {
		return asapInfo;
	}

	public void setASAPInfo(ASAPInfo asapInfo) {
		this.asapInfo = asapInfo;
		author = getString(asapInfo.getAuthor());
		title = getString(asapInfo.getTitle());
		date = getString(asapInfo.getDate());
	}

	public String getAuthor() {

		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTitle() {

		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}