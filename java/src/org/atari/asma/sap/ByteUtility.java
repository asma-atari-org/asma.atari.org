package org.atari.asma.sap;

class ByteUtility {
	public static String getCharForByte(int b) {
		return "0x" + Integer.toHexString(b) + " '" + (char) b + "'";
	}

	public static int getWord(byte[] array, int index) {
		return array[index] & 0xff | (array[index + 1] & 0xff) << 8;
	}

	public static String getByteHexString(int value) {
		return String.format("%02X", value);
	}

	public static String getWordHexString(int value) {
		return String.format("%04X", value);

	}

	public static String getLongHexString(long value) {
		return String.format("%08X", value);

	}

	public static String getIndexString(int value) {
		return String.format("%d (%04X)", value, value);
	}

}