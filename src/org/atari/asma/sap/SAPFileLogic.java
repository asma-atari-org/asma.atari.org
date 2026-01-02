package org.atari.asma.sap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.TreeSet;

import org.atari.asma.util.FileUtility;
import org.atari.asma.util.MessageQueue;

import net.sf.asap.ASAP;
import net.sf.asap.ASAPConversionException;
import net.sf.asap.ASAPFormatException;
import net.sf.asap.ASAPInfo;
import net.sf.asap.ASAPWriter;

/**
 * See https://asap.sourceforge.net/sap-format.html See
 * https://sourceforge.net/p/asap/code/ci/master/tree/chksap.pl
 * 
 * @author Peter Dell
 *
 */
public class SAPFileLogic {

	private SegmentListLogic segmentListLogic;

	public SAPFileLogic() {
		segmentListLogic = new SegmentListLogic();
	}

	public SAPFile loadSAPFile(File file, MessageQueue messageQueue) {
		return loadSAPFile(file.getName(), FileUtility.readAsByteArray(file), messageQueue);
	}

	public SAPFile loadSAPFile(String fileName, byte[] content, MessageQueue messageQueue) {
		var sapFile = new SAPFile();
		sapFile.content = content;
		int index = 0;
		boolean endOfHeader = false;
		var header = new StringBuilder();
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

//				messageQueue.sendMessage(line);
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

		sapFile.segmentsStartIndex = index;
		if (!segmentListLogic.loadSegmentList(sapFile.segmentList, content, index, messageQueue)) {
			return null;
		}
		var asap = new ASAP();
		try {
			asap.load(fileName, content, content.length);
		} catch (ASAPFormatException ex) {
			messageQueue.sendError("Invalid SAP file. " + ex.getMessage());
			return null;
		}
		sapFile.asapInfo = asap.getInfo();
		return sapFile;
	}

	public SAPFile loadOriginalModuleFile(File file, MessageQueue messageQueue) {

		return loadOriginalModuleFile(file.getName(), FileUtility.readAsByteArray(file), messageQueue);
	}

	public SAPFile loadOriginalModuleFile(String fileName, byte[] content, MessageQueue messageQueue) {
		var asap = new ASAP();
		try {
			asap.load(fileName, content, content.length);
		} catch (ASAPFormatException ex) {
			messageQueue.sendError("Invalid ASAP file. " + ex.getMessage());
			return null;
		}
		var sapFile = new SAPFile();
		sapFile.header = "";
		sapFile.content = content;
		sapFile.asapInfo = asap.getInfo();

		var os = new ByteArrayOutputStream();
		var sapFileName = FileUtility.changeFileExtension(new File(fileName), ".sap").getName();
		if (!saveSAPFile(os, sapFileName, sapFile, messageQueue)) {
			return null;
		}
		sapFile = loadSAPFile(sapFileName, os.toByteArray(), messageQueue);
		return sapFile;
	}

	public boolean saveSAPFile(File file, SAPFile sapFile, MessageQueue messageQueue) {

		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException ex) {
			messageQueue.sendError("Cannot save SAP file. " + ex.getMessage());
			return false;
		}
		try {
			if (!saveSAPFile(os, file.getName(), sapFile, messageQueue)) {
				return false;
			}
		} finally {
			try {
				os.close();
			} catch (IOException ex1) {
				messageQueue.sendError("Cannot close output stream." + ex1.getMessage());
			}
		}
		return true;
	}

	public boolean saveSAPFile(OutputStream os, String fileName, SAPFile sapFile, MessageQueue messageQueue) {
		var output = new byte[128 * 1024 * 1024];
		var length = 0;
		var asapWriter = new ASAPWriter();
		asapWriter.setOutput(output, 0, output.length);
		try {
			length = asapWriter.write(fileName, sapFile.asapInfo, sapFile.content, sapFile.content.length, false);

		} catch (ASAPConversionException ex) {
			messageQueue.sendError("Cannot save SAP file. " + ex.getMessage());
			return false;
		}
		try {
			os.write(output, 0, length);
		} catch (IOException ex) {
			messageQueue.sendError("Cannot save SAP file. " + ex.getMessage());
			return false;
		}
		return true;
	}

	public SAPFile loadXEXFile(File inputFile, MessageQueue messageQueue) {
		var sapFile = new SAPFile();

		if (!segmentListLogic.loadSegmentList(sapFile.segmentList, inputFile, messageQueue)) {
			return null;
		}
		if (sapFile.segmentList.size() == 3) {

		}

		var headerBuilder = new StringBuilder();
		headerBuilder.append("XEX\n");
		var playerFactory = new PlayerFactory();
		var players = playerFactory.getMatchingPlayers(sapFile.segmentList);
		if (players.isEmpty()) {
			headerBuilder.append("PLAYER UNKNOWN\n");
		} else {
			for (var player : players) {
				headerBuilder.append("PLAYER \"" + player.getName() + "\"\n");
			}
		}

		sapFile.header = headerBuilder.toString();
		sapFile.content = sapFile.segmentList.toByteArray();
		sapFile.asapInfo = new ASAPInfo();
		return sapFile;
	}
}