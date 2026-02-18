package org.atari.asma.sap;

import java.util.Set;
import java.util.TreeSet;

import net.sf.asap.ASAPInfo;

public class ASAPFile {
	// https://asap.sourceforge.net/sap-format.html
	public final static String UPPPER_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public final static String LOWER_LETTERS = "abcdefghijklmnopqrstuvwxyz";
	public final static String NUMBERS = "0123456789";
	public final static String SPECICAL_CHARACTERS = " !\"#$%&'()*+,-./:;<=>?@[\\]^_";
	public final static String ATASCII_CHARACTERS = UPPPER_LETTERS + LOWER_LETTERS + NUMBERS + SPECICAL_CHARACTERS
			+ "|";

	private static Set<String> orginalFileExtensions = null;

	public static boolean isOriginalModuleFileExtension(String fileExtension) {
		if (orginalFileExtensions == null) {
			orginalFileExtensions = new TreeSet<String>();
			for (var s : new String[] { ".dmc", ".cmc", ".cm3", ".cmr", ".cms", ".dlt", ".mpd", ".mpt", ".rmt", ".tmc",
					".tm2", ".fc" }) {
				orginalFileExtensions.add(s);
			}
		}
		return orginalFileExtensions.contains(fileExtension);

	}

	public static boolean isStringEmpty(String s) {
		return s.trim().isEmpty() || s.trim().equals("<?>");
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
		if (s==null || s.isBlank()) {
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