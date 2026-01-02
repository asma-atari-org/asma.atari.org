package org.atari.asma.sap;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

import org.atari.asma.RMTFile;

public class Segment {
	public boolean header;
	public int segment;
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

	public byte[] toByteArray(boolean forceHeader) {
		byte[] result;
		int index = 0;
		if (header || forceHeader) {
			result = new byte[6 + content.length];
			result[index++] = (byte) 0xff;
			result[index++] = (byte) 0xff;
		} else {
			result = new byte[4 + content.length];
		}
		result[index++] = (byte) (startAddress & 0xff);
		result[index++] = (byte) (startAddress >>> 8);
		result[index++] = (byte) (endAddress & 0xff);
		result[index++] = (byte) (endAddress >>> 8);

		System.arraycopy(content, 0, result, index, content.length);
		return result;
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

	public boolean contentEquals(int offset, byte[] bytes) {
		if (offset >= 0 && offset + bytes.length < getLength()) {
			for (int i = 0; i < bytes.length; i++) {
				if (content[offset + i] != bytes[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean contentEquals(int offset, String asciiText) {
		// TODO Auto-generated method stub
		return contentEquals(offset, asciiText.getBytes(StandardCharsets.US_ASCII));
	}

	public String getContentScreenCodeString(int offset, int length) {
		var builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			var b = (content[offset + i] & 0x7f);
			if (b <= 96) {
				b = b + 32;
			}
			builder.append((char) b);
		}
		return builder.toString();
	}

	public String getContentATATSCIIString(int offset, int length) {
		var builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			var c = (char) content[offset + i];
			if (c == 0x9b) {
				c = '\n';
			}
			builder.append(c);
		}
		return builder.toString();
	}

}