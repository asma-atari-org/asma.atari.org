package org.atari.asma;

// Based on https://sourceforge.net/p/jsidplay2/code/HEAD/tree/trunk/jsidplay2/src/main/java/libsidutils/stil/STIL.java
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class STIL {

	public final static class Info {
		public String name;
		public String author;
		public String title;
		public String artist;
		public String comment;

		@Override
		public String toString() {
			return "info";
		}
	}

	public final static class TuneEntry {
		public int tuneNo = -1;
		public List<Info> infos = new ArrayList<>();

		@Override
		public String toString() {
			return "" + tuneNo;
		}
	}

	public static class STILEntry {
		private String comment;
		private String filename;
		private List<TuneEntry> subTunes = new ArrayList<>();
		private List<Info> infos = new ArrayList<>();

		public STILEntry(String name) {
			filename = name;
		}

		public String getComment() {
			return comment;
		}

		public String getFilename() {
			return filename;
		}

		public List<TuneEntry> getSubTunes() {
			return subTunes;
		}

		public List<Info> getInfos() {
			return infos;
		}

		@Override
		public String toString() {
			return "" + filename.substring(filename.lastIndexOf('/') + 1);
		}
	}

	private Map<String, STILEntry> fastMap = new HashMap<>();

	public STIL() {
	}

	void load(File inputFile) throws IOException {

		fastMap.clear();

		Pattern p = Pattern.compile("(NAME|AUTHOR|TITLE|ARTIST|COMMENT): *(.*)");

		STILEntry entry = null;
		TuneEntry tuneEntry = null;
		Info lastInfo = null;
		String lastProp = null;
		StringBuilder cmts = new StringBuilder();
		Reader reader = null;
		try {
			reader = new FileReader(inputFile, StandardCharsets.UTF_8);
			final BufferedReader bufferedReader = new BufferedReader(reader);
			String line;
			int lineNumber = 0;
			while ((line = bufferedReader.readLine()) != null) {
				lineNumber++;
				// See UTF-8 BOM bug,
				// https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4508058
				if ((lineNumber == 1) && (line.charAt(0) == 0xfeff)) {
					line = line.substring(1);
				}

				if (line.startsWith("#")) {
					cmts.append(line.trim() + "\n");
					continue;
				}

				/* New entry? */
				if (line.startsWith("/")) {
					entry = new STILEntry(line);
					fastMap.put(line, entry);

					entry.comment = cmts.toString();
					cmts.delete(0, cmts.length());

					lastInfo = new Info();
					entry.infos.add(lastInfo);

					tuneEntry = null;
					lastProp = null;
					continue;
				}

				if (line.startsWith("(#")) {
					if (entry == null) {
						throw new RuntimeException(
								"Invalid format in STIL file: '(#' before '/' in line " + lineNumber + ".");
					}

					// subtune
					int end = line.indexOf(")");
					int tuneNo = Integer.parseInt(line.substring(2, end));

					// subtune number
					tuneEntry = new TuneEntry();
					tuneEntry.tuneNo = tuneNo;
					entry.subTunes.add(tuneEntry);

					lastInfo = new Info();
					tuneEntry.infos.add(lastInfo);

					lastProp = null;
					continue;
				}

				line = line.trim();
				if ("".equals(line)) {
					continue;
				}

				if (entry == null) {
					throw new RuntimeException("No entry to put data in '" + line + "' in line \"+lineNumber+\".");
				}

				if (lastInfo == null) {
					throw new RuntimeException("No context to put data in '" + line + "' in line \"+lineNumber+\".");
				}

				try {
					Matcher m = p.matcher(line);
					if (m.matches()) {
						lastProp = m.group(1);

						// If a field repeats, that starts a new tuneinfo structure.
						Field f = getField(lastInfo, lastProp);
						if (f.get(lastInfo) != null) {
							lastInfo = new Info();
							if (tuneEntry != null) {
								tuneEntry.infos.add(lastInfo);
							} else {
								entry.infos.add(lastInfo);
							}
						}
						f.set(lastInfo, m.group(2));
					} else if (lastProp != null) {
						// Concatenate more text after the previous line
						Field f = getField(lastInfo, lastProp);
						f.set(lastInfo, f.get(lastInfo) + "\n" + line);
					}
				} catch (IllegalAccessException ex) {
					throw new RuntimeException("Illegal access exception in line " + lineNumber + ".", ex);
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

	}

	private static Field getField(Info lastInfo, String lastProp) {
		try {
			Field f = lastInfo.getClass().getField(lastProp.toLowerCase(Locale.ENGLISH));
			return f;
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		}
	}

	public int getSize() {
		return fastMap.size();
	}

	public STILEntry getSTILEntry(String collectionName) {
		return fastMap.get(collectionName);
	}

}
