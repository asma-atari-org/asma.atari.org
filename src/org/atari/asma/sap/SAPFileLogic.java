package org.atari.asma.sap;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import org.atari.asma.MessageQueue;
import org.atari.asma.util.FileUtility;

class SAPFileLogic {

		public SAPFile readSAPFile(File file, MessageQueue messageQueue) {
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