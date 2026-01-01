package org.atari.asma.sap;

import java.util.zip.CRC32;

import org.atari.asma.RMTFile;

public class Segment {
	public int startAddress;
	public int endAddress;
	public byte[] content;

	public int getLength() {
		return endAddress - startAddress + 1;
	}

	public String getCRC32() {
		var crc32 = new CRC32();
		crc32.update(content);
		return ByteUtility.getLongHexString(crc32.getValue());
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(ByteUtility.getWordHexString(startAddress));
		sb.append(" - ");
		sb.append(ByteUtility.getWordHexString(endAddress));
		sb.append(" CRC32=").append(getCRC32());
		var type = RMTFile.getType(content);
		if (!type.isEmpty()) {
			sb.append("\n");
			var rmtFile = new RMTFile(startAddress, endAddress, content);
			sb.append(rmtFile);
		}
		return sb.toString();
	}
}