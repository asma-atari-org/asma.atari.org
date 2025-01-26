package org.atari.asma;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.zip.CRC32;

import org.atari.asma.ASMAExporter.FileExtension;
import org.atari.asma.util.FileUtility;

public class SAPEditor {

	private static class ByteUtility {
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

	private static class Segment {
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
			return sb.toString();
		}
	}

	private static class SAPTags {

		public static final String AUTHOR = "AUTHOR";
		public static final String NAME = "NAME";
		public static final String DATE = "DATE";
		public static final String SONGS = "SONGS";
		public static final String DEFSONG = "DEFSONG";
		public static final String STEREO = "STEREO";
		public static final String NTSC = "NTSC";
		public static final String TYPE = "TYPE";
		public static final String FASTPLAY = "FASTPLAY";
		public static final String INIT = "INIT";
		public static final String MUSIC = "MUSIC";
		public static final String PLAYER = "PLAYER";
		public static final String COVOX = "COVOX";
		public static final String TIME = "TIME";

		public static List<String> getTags() {
			List<String> result = new ArrayList<String>();

			result.add(AUTHOR);
			result.add(NAME);
			result.add(DATE);
			result.add(SONGS);
			result.add(DEFSONG);
			result.add(STEREO);
			result.add(NTSC);
			result.add(TYPE);
			result.add(FASTPLAY);
			result.add(INIT);
			result.add(MUSIC);
			result.add(PLAYER);
			result.add(COVOX);
			result.add(TIME);
			return result;
		}

	}

	private static class SAPFile {
		// https://asap.sourceforge.net/sap-format.html
		final static String UPPPER_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final static String LOWER_LETTERS = "abcdefghijklmnopqrstuvwxyz";
		final static String NUMBERS = "0123456789";
		final static String SPECICAL_CHARACTERS = " !\"#$%&'()*+,-./:;<=>?@[\\]^_";
		final static String ATASCII_CHARACTERS = UPPPER_LETTERS + LOWER_LETTERS + NUMBERS + SPECICAL_CHARACTERS + "|";

		public SAPFile() {
			segmentList = new ArrayList<Segment>();
		}

		public String header;
		public List<Segment> segmentList;

		public String toString() {
			return header + getSegmentsString();
		}

		public String getSegmentsString() {
			StringBuilder sb = new StringBuilder();
			int segmentCount = 0;
			for (var segment : segmentList) {
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

	private MessageQueue messageQueue;

	private SAPEditor() {
		messageQueue = new MessageQueue(System.out, System.err);

	}

	public static void main(String[] args) {
		SAPEditor instance = new SAPEditor();
		instance.run(args);
	};

	private void run(String[] args) {

		if (args.length != 1) {
			messageQueue.sendMessage("Usage: SAPEditor <sap file|sap folder>");
			return;
		}

		String filePath = args[0];

		try {
			File file = new File(filePath);
			if (file.isDirectory()) {
				scanFolder(file);
			} else {
				messageQueue.sendInfo("Reading '" + file.getAbsolutePath() + "'.");
				checkSAPFile(file, true);
			}

		} catch (Exception ex) {
			messageQueue.sendError(ex.getMessage());
			ex.printStackTrace();
		}
		messageQueue.printSummary();

	}

	private void scanFolder(File folder) {
		messageQueue.sendInfo("Scanning " + folder.getAbsolutePath());

		var fileList = FileUtility.getRecursiveFileList(folder, new FileFilter() {

			@Override
			public boolean accept(File file) {
				String fileName = file.getName().toLowerCase();
				if (fileName.endsWith(FileExtension.SAP)) {
					return true;
				}
				return false;
			}
		});
		int totalCount = fileList.size();
		messageQueue.sendInfo(totalCount + " files found.");

		int count = 0;
		for (File file : fileList) {
			checkSAPFile(file, false);
			count++;
			if (count % 100 == 0 || count == totalCount) {
				messageQueue.sendInfo(count + " files processed.");
			}
		}
		messageQueue.sendInfo(fileList.size() + " files processed.");

	}

	private void checkSAPFile(File file, boolean details) {
		var sapFile = readSAPFile(file);
		if (sapFile != null) {
			if (details) {
				messageQueue.sendMessage(sapFile.toString());
			}
		} else {
			messageQueue.sendInfo("Error reading '" + file.getAbsolutePath() + "'. See above.");

		}
	}

	private SAPFile readSAPFile(File file) {
		SAPFile sapFile = new SAPFile();
		byte[] content = FileUtility.readAsByteArray(file);
		int index = 0;
		boolean endOfHeader = false;
		StringBuilder header = new StringBuilder();
		while (index < content.length && !endOfHeader) {
			int b = content[index] & 0xff;
			if (b == 0xff) {
				endOfHeader = true;
				break;
			} else if (b == 0x0d) {
				if (index < content.length - 1) {
					b = content[index + 1];
					if (b == 0x0a) {
						header.append("\n");
						index++;

					} else {
						messageQueue.sendError("Invalid character " + ByteUtility.getCharForByte(b)
								+ " after carriage return at index. Line feed expected.");
						return null;
					}
				} else {
					messageQueue.sendError("Missing line feed after carriage return at index "
							+ ByteUtility.getIndexString(index) + ".");
				}
			} else {
				char c = (char) (b & 0xff);
				if (SAPFile.ATASCII_CHARACTERS.indexOf(c) == -1) {
					messageQueue.sendError("Invalid non-ATASCII character " + ByteUtility.getCharForByte(b)
							+ " at index " + ByteUtility.getIndexString(index) + ".");
					return null;
				}
				header.append(c);
			}
			index++;
		}
		if (!endOfHeader) {
			messageQueue.sendError("Invalid file structure.");
			return null;
		}
		sapFile.header = header.toString();
		if (!sapFile.header.startsWith("SAP\n")) {
			messageQueue.sendError("Header does not start with 'SAP' line.");
			return null;
		}
		var lines = sapFile.header.split("\n");
		var tags = SAPTags.getTags();
		Set<String> foundTagSet = new TreeSet<String>();
		int tagIndex = 0;
		for (int lineIndex = 1; lineIndex < lines.length; lineIndex++) {
			// Find tag that starts the line
			var line = lines[lineIndex];
			var spaceIndex = line.indexOf(' ');
			if (spaceIndex >= 0) {
				line = line.substring(0, spaceIndex);
			}
			if (line.length() == 0) {
				continue;
			}
			var foundTag = line;
			foundTagSet.add(foundTag);
			if (!tags.contains(foundTag)) {
				messageQueue.sendError("Undefined tag '" + foundTag + "' found.");
				return null;
			}
			boolean ok = false;
			while (tagIndex < tags.size()) {
				var tag = tags.get(tagIndex);
				if (tag.equals(foundTag)) {
					ok = true;
					break;
				}
				tagIndex++;
			}
			if (!ok) {
				messageQueue.sendWarning("Found tag " + foundTag + " does not fit to the recommended tag sequece '"
						+ tags.toString() + "'.");
				return null;

			}

//			messageQueue.sendMessage(line);
		}

		if (!foundTagSet.contains(SAPTags.AUTHOR)) {
			messageQueue.sendWarning("Stronly recommended tag '" + SAPTags.AUTHOR + "' missing");
			return null;
		}
		if (!foundTagSet.contains(SAPTags.NAME)) {
			messageQueue.sendWarning("Stronly recommended tag '" + SAPTags.NAME + "' missing");
			return null;
		}
		if (!foundTagSet.contains(SAPTags.DATE)) {
			messageQueue.sendWarning("Stronly recommended tag '" + SAPTags.DATE + "' missing");
			return null;
		}

		// There must be at least one segment header followed by at least one byte.
		if (index + 6 >= content.length) {
			messageQueue.sendError("Invalid binary segment at index " + ByteUtility.getIndexString(index) + ".");
			return null;
		}

		int segmentCount = 0;
		while (index + 4 < content.length) {
			var segmentStartIndex = index;
			Segment segment = new Segment();
			int w = ByteUtility.getWord(content, index);
			index += 2;
			if (w == 0xffff) {
				w = ByteUtility.getWord(content, index);
				index += 2;
			}
			segment.startAddress = w;
			w = ByteUtility.getWord(content, index);
			index += 2;
			segment.endAddress = w;
			if (segment.startAddress > segment.endAddress) {
				messageQueue.sendError("Invalid segment " + segmentCount + " at index "
						+ ByteUtility.getIndexString(segmentStartIndex) + " has start address "
						+ ByteUtility.getWordHexString(segment.startAddress) + " greater than end address "
						+ ByteUtility.getWordHexString(segment.endAddress) + ".");
				return null;
			}
			var binaryStartIndex = index;
			sapFile.segmentList.add(segment);
			segment.content = new byte[segment.getLength()];
			System.arraycopy(content, binaryStartIndex, segment.content, 0, segment.getLength());
			index += segment.getLength();

			segmentCount++;
		}
		if (index != content.length) {
			messageQueue.sendError("Invalid additinal data after last segment " + segmentCount + " at index "
					+ ByteUtility.getIndexString(index) + ".");
			return null;
		}

		return sapFile;
	}

}
//
//SAP
//AUTHOR "<?> (Makary Brauner)"
//NAME "Serial Invaders"
//DATE "01/07/2017"
//TYPE B
//FASTPLAY 78
//INIT 3950
//PLAYER 3403
//TIME 02:18.328
//LOAD 3182-3958 CRC32=D625ACA6
//LOAD 4000-4940 CRC32=F61B8B68
