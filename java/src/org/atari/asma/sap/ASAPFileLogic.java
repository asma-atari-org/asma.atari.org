package org.atari.asma.sap;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.atari.asma.sap.player.PlayerFactory;
import org.atari.asma.util.FileUtility;
import org.atari.asma.util.MessageQueue;

import net.sf.asap.ASAP;
import net.sf.asap.ASAPArgumentException;
import net.sf.asap.ASAPConversionException;
import net.sf.asap.ASAPFormatException;
import net.sf.asap.ASAPIOException;
import net.sf.asap.ASAPWriter;

/**
 * See the <a href="https://asap.sourceforge.net/sap-format.html">SAP Format
 * Specification</a>. <br>
 * See also <a href=
 * "https://sourceforge.net/p/asap/code/ci/master/tree/chksap.pl">chksap.pl</a>.
 * 
 * @author Peter Dell
 *
 */
public class ASAPFileLogic {

	private static final class MemoryASAPWriter extends ASAPWriter {

		public Map<String, byte[]> fileMap = new TreeMap<String, byte[]>();

		protected void save(String filename, byte[] buffer, int offset, int length) throws ASAPIOException {
			byte[] content = new byte[length];
			System.arraycopy(buffer, offset, content, 0, length);
			fileMap.put(filename, content);
		}

	}

	private SegmentListLogic segmentListLogic;

	public ASAPFileLogic() {
		segmentListLogic = new SegmentListLogic();
	}

	public ASAPFile loadSAPFile(File file, MessageQueue messageQueue) {
		return loadSAPFile(file.getName(), FileUtility.readAsByteArray(file), messageQueue);
	}

	public ASAPFile loadSAPFile(String fileName, byte[] content, MessageQueue messageQueue) {

		if (content.length == 0) {

			messageQueue.sendError("SAP-001", "File '" + fileName + "' is empty");
			return null;
		}

		int index = 0;
		boolean endOfHeader = false;
		var headerBuilder = new StringBuilder();
		while (index < content.length && !endOfHeader) {
			int b = content[index] & 0xff;
			if (b == 0xff) {
				endOfHeader = true;
				break;
			} else if (b == 0x0d) {
				if (index < content.length - 1) {
					b = content[index + 1];
					if (b == 0x0a) {
						headerBuilder.append("\n");
						index++;

					} else {
						messageQueue.sendError("SAP-002", "Invalid character " + ByteUtility.getCharForByte(b)
								+ " after carriage return at index. Line feed expected.");
						return null;
					}
				} else {
					messageQueue.sendError("SAP-003", "Missing line feed after carriage return at index "
							+ ByteUtility.getIndexString(index) + ".");
				}
			} else {
				char c = (char) (b & 0xff);
				if (ASAPFile.ATASCII_CHARACTERS.indexOf(c) == -1) {
					messageQueue.sendError("SAP-004", "Invalid non-ATASCII character " + ByteUtility.getCharForByte(b)
							+ " at index " + ByteUtility.getIndexString(index) + ".");
					return null;
				}
				headerBuilder.append(c);
			}
			index++;
		}
		if (!endOfHeader) {
			messageQueue.sendError("SAP-005", "Invalid file structure.");
			return null;
		}
		var header = headerBuilder.toString();
		if (!header.startsWith("SAP\n")) {
			messageQueue.sendError("SAP-006", "Header does not start with 'SAP' line.");
			return null;
		}
		var lines = header.split("\n");
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
				messageQueue.sendError("SAP-007", "Undefined tag '" + foundTag + "' found.");
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
				messageQueue.sendWarning("SAP-008", "Found tag " + foundTag + " does not fit to the recommended tag sequece '"
						+ tags.toString() + "'.");
				return null;

			}

//				messageQueue.sendMessage(line);
		}

		if (!foundTagSet.contains(SAPTags.AUTHOR)) {
			messageQueue.sendWarning("SAP-009", "Stronly recommended tag '" + SAPTags.AUTHOR + "' missing");
			return null;
		}
		if (!foundTagSet.contains(SAPTags.NAME)) {
			messageQueue.sendWarning("SAP-010", "Stronly recommended tag '" + SAPTags.NAME + "' missing");
			return null;
		}
		if (!foundTagSet.contains(SAPTags.DATE)) {
			messageQueue.sendWarning("SAP-011", "Stronly recommended tag '" + SAPTags.DATE + "' missing");
			return null;
		}

		// There must be at least one segment header followed by at least one byte.
		if (index + 6 >= content.length) {
			messageQueue.sendError("SAP-012", "Invalid binary segment at index " + ByteUtility.getIndexString(index) + ".");
			return null;
		}

		var sapFile = new ASAPFile();
		sapFile.content = content;
		if (!segmentListLogic.loadSegmentList(sapFile.segmentList, content, index, messageQueue)) {
			return null;
		}
		var asap = new ASAP();
		try {
			asap.load(fileName, content, content.length);
		} catch (ASAPFormatException ex) {
			messageQueue.sendError("SAP-006", "Invalid SAP file. " + ex.getMessage());
			return null;
		}
		sapFile.setASAPInfo(asap.getInfo());
		return sapFile;
	}

	// Load an original module and convert is to SAP format upon loading.
	public ASAPFile loadOriginalModuleFile(File file, MessageQueue messageQueue) {

		return loadOriginalModuleFile(file.getName(), FileUtility.readAsByteArray(file), messageQueue);
	}

	// Load an original module and convert is to SAP format upon loading.
	public ASAPFile loadOriginalModuleFile(String fileName, byte[] content, MessageQueue messageQueue) {
		var asap = new ASAP();
		try {
			asap.load(fileName, content, content.length);
		} catch (ASAPFormatException ex) {
			messageQueue.sendError("SAP-013", "Invalid ASAP file. " + ex.getMessage());
			return null;
		}

		var tempSAPFileName = FileUtility.changeFileExtension(new File(fileName), ".sap").getName();
		var asapWriter = new MemoryASAPWriter();
		try {
			var asapInfo = asap.getInfo();
			asapWriter.write(tempSAPFileName, asapInfo, content, content.length, false);

		} catch (ASAPConversionException ex) {
			messageQueue.sendError("SAP-014", "Cannot save SAP file. " + ex.getMessage());
			return null;
		} catch (ASAPIOException ex) {
			messageQueue.sendError("SAP-015","Cannot save SAP file. " + ex.getMessage());
			return null;
		}

		if (asapWriter.fileMap.size() != 1) {
			messageQueue.sendError("SAP-016", "File could not be converted to SAP format.");
			return null;
		}

		var sapFile = loadSAPFile(tempSAPFileName, asapWriter.fileMap.get(tempSAPFileName), messageQueue);
		return sapFile;
	}

	public boolean saveSAPFile(File file, ASAPFile asapFile, MessageQueue messageQueue) {

		var asapWriter = new MemoryASAPWriter();

		var asapInfo = asapFile.getASAPInfo();
		try {
			asapInfo.setAuthor(asapFile.getAuthor());
		} catch (ASAPArgumentException ex) {
			messageQueue.sendError("SAP-017", "Cannot set author. " + ex.getMessage());
		}
		try {
			asapInfo.setTitle(asapFile.getTitle());
		} catch (ASAPArgumentException ex) {
			messageQueue.sendError("SAP-018","Cannot set title. " + ex.getMessage());
		}
		try {
			asapInfo.setDate(asapFile.getDate());
		} catch (ASAPArgumentException ex) {
			messageQueue.sendError("SAP-019","Cannot set date. " + ex.getMessage());
		}
		if (messageQueue.getErrorCount() > 0) {
			return false;
		}

		try {
			asapWriter.write(file.getAbsolutePath(), asapInfo, asapFile.content, asapFile.content.length, false);
			var content = asapWriter.fileMap.get(file.getAbsolutePath());
			if (content == null) {
				throw new IOException("Could not write SAP file to memory.");
			}
			Files.write(file.toPath(), content, StandardOpenOption.CREATE);

		} catch (ASAPConversionException ex) {
			messageQueue.sendError("SAP-020","Cannot save SAP file. " + ex.getMessage());
			return false;
		} catch (ASAPIOException ex) {
			messageQueue.sendError("SAP-020","Cannot save SAP file. " + ex.getMessage());
			return false;
		} catch (IOException ex) {
			messageQueue.sendError("SAP-020","Cannot save SAP file. " + ex.getMessage());
			return false;
		}

		return true;
	}

	public ASAPFile loadXEXFile(SAPFileProcessor fileProcessor, File inputFile, PrintWriter header, MessageQueue messageQueue) {

		var segmentList = new SegmentList();
		if (!segmentListLogic.loadSegmentList(segmentList, inputFile, messageQueue)) {
			return null;
		}

		header.println("XEX");
		var playerFactory = new PlayerFactory();
		var players = playerFactory.getMatchingPlayers(segmentList);
		if (players.isEmpty()) {
			header.println("PLAYER UNKNOWN");
		} else {
			for (var player : players) {
				header.println("PLAYER \"" + player.getName() + "\"");

				header.println();
				List<String> texts = new ArrayList<String>();
				player.getTexts(segmentList, texts);
				for (var text : texts) {
					header.println(text);
				}
			}
		}

		var asapFile = new ASAPFile();
		asapFile.segmentList.getEntries().addAll(segmentList.getEntries());
		if (players.isEmpty()) {
			for (int i = 0; i < segmentList.size(); i++) {
				var segment = asapFile.segmentList.get(i);
				messageQueue.sendInfo("Scanning segment " + i + " for modules.");
				scanSegment(fileProcessor, inputFile, segment, header, messageQueue);
			}
		} else

		if (players.size() == 1) {
			var player = players.get(0);
			if (player.fillSAPFile(asapFile, segmentList, messageQueue)) {
				messageQueue.sendInfo("Convertered " + player.getName() + " file to SAP format.");
			}
			;
		}

		return asapFile;
	}

	private static class RMTEntry {
		int offset;
		String type;
	}

	private static List<RMTEntry> findRMTEntries(Segment segment) {
		List<RMTEntry> result = new ArrayList<RMTEntry>();

		for (int i = 0; i < segment.getLength() - 4; i++) {
			char c1 = (char) segment.content[i];
			char c2 = (char) segment.content[i + 1];
			char c3 = (char) segment.content[i + 2];
			char c4 = (char) segment.content[i + 3];

			if (c1 == 'R' && c2 == 'M' && c3 == 'T' && (c4 == '4' || c4 == '8')) {
				var entry = new RMTEntry();
				entry.offset = i;
				entry.type = String.valueOf(c1) + String.valueOf(c2) + String.valueOf(c3) + String.valueOf(c4);
				result.add(entry);
			}
		}
		return result;

	}

	private static void scanSegment(SAPFileProcessor fileProcessor, File inputFile, Segment segment, PrintWriter header,
			MessageQueue messageQueue) {
		List<RMTEntry> rmtOffsets = findRMTEntries(segment);

		for (int i = 0; i < rmtOffsets.size(); i++) {
			var entry = rmtOffsets.get(i);

			int startAddress = segment.startAddress + entry.offset;
			int endAddress;
			if (i < rmtOffsets.size() - 1) {
				endAddress = segment.startAddress + rmtOffsets.get(i + 1).offset - 1;
			} else {
				endAddress = segment.endAddress;
			}
			int length = endAddress - startAddress + 1;
			String startAddressString = ByteUtility.getWordHexString(startAddress);
			String endAddressString = ByteUtility.getWordHexString(endAddress);

			messageQueue
					.sendInfo("Found " + entry.type + " at $" + startAddressString + " - $" + endAddressString + ".");

			byte[] rmtContent = new byte[6 + length];
			rmtContent[0] = (byte) 0xff;
			rmtContent[1] = (byte) 0xff;
			rmtContent[2] = (byte) (startAddress & 0xff);
			rmtContent[3] = (byte) (startAddress >> 8);
			rmtContent[4] = (byte) (endAddress & 0xff);
			rmtContent[5] = (byte) (endAddress >> 8);
			System.arraycopy(segment.content, entry.offset, rmtContent, 6, length);
			File rmtFile = FileUtility.changeFileExtension(inputFile, "");
			rmtFile = new File(rmtFile.getAbsolutePath() + "-$" + startAddressString + ".rmt");
			try {
				Files.write(rmtFile.toPath(), rmtContent, StandardOpenOption.CREATE);
				messageQueue.sendInfo("Opening " + rmtFile.getAbsolutePath() + " in separate window.");
				fileProcessor.processFile(rmtFile);
			} catch (IOException e) {
				messageQueue.sendError("SAP-021",e.getMessage());
			}
//
//			var asapFile = loadOriginalModuleFile(rmtFileName, rmtContent, messageQueue);
//			if (asapFile != null) {
//				header.println("RMT file " + rmtFileName + " loaded.");
//			}
		}

	}
}
