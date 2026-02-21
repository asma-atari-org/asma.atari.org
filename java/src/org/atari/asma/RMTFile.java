package org.atari.asma;

// https://github.com/raster-atari-org/RASTER-Music-Tracker/blob/stable/RMT/docs/rmt_format1x.txt
public class RMTFile {

	public static class Instrument {
		
	}
	
	public static class Track {
		
	}

	public static String RMT4 = "RMT4";
	public static String RMT8 = "RMT8";

	public final int startAddress;
	public final int endAddress;
	public final byte[] content;

	public static String getType(byte[] content) {
		if (content.length > 4 && content[0] == 'R' && content[1] == 'M' && content[2] == 'T') {
			if (content[3] == '4') {
				return "RMT4";

			}
			if (content[3] == '8') {
				return "RMT8";

			}
		}
		return "";

	}

	public RMTFile(int startAddress, int endAddress, byte[] content) {
		this.startAddress = startAddress;
		this.endAddress = endAddress;
		this.content = content;

	}

	public String getHeaderString() {
		return getType(content);
	}

	public int getTrackLength() {
		int result = getByte(4);
		if (result == 0) {
			result = 256;
		}
		return result;
	}

	public int getSongSpeed() {
		return getByte(5);
	}

	public int getPlayerFrequency() {
		return getByte(6);
	}

	public int getFormatVersionNumber() {
		return getByte(5);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Header: ").append(getHeaderString()).append("\n");
		sb.append("Track Length: ").append(getTrackLength()).append("\n");
		sb.append("Song Speed ").append(getSongSpeed()).append("\n");
		sb.append("Player Frequency: ").append(getPlayerFrequency()).append("\n");
		sb.append("Format Version Number: ").append(getFormatVersionNumber()).append("\n");

		return sb.toString();
	}

	private int getByte(int offset) {
		return content[offset];
	}
}
