package org.atari.asma.sap;

import net.sf.asap.ASAPInfo;

public class SAPFile {
	// https://asap.sourceforge.net/sap-format.html
	final static String UPPPER_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	final static String LOWER_LETTERS = "abcdefghijklmnopqrstuvwxyz";
	final static String NUMBERS = "0123456789";
	final static String SPECICAL_CHARACTERS = " !\"#$%&'()*+,-./:;<=>?@[\\]^_";
	final static String ATASCII_CHARACTERS = UPPPER_LETTERS + LOWER_LETTERS + NUMBERS + SPECICAL_CHARACTERS + "|";

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