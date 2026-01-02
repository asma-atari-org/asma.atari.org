package org.atari.asma.sap;

import java.util.Set;
import java.util.TreeSet;

import net.sf.asap.ASAPInfo;

public class SAPFile {
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

	public SAPFile() {
		segmentList = new SegmentList();
	}

	public byte[] content;
	public int segmentsStartIndex;

	public String header;
	public SegmentList segmentList;
	public ASAPInfo asapInfo;

	public String toString() {
		return header + getSegmentsString();
	}

	public String getSegmentsString() {
		StringBuilder sb = new StringBuilder();
		int segmentCount = 0;
		for (var segment : segmentList.getEntries()) {
			sb.append("LOAD ");
			sb.append(ByteUtility.getByteHexString(segmentCount));
			sb.append(": ");
			sb.append(segment.toString());
			sb.append("\n");
			segmentCount++;
		}
		return sb.toString();
	}
}