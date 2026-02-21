package org.atari.asma.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

public final class StringUtility {
	
	private StringUtility() {
		
	}
	
	private static String getTwoDigiNumber(long value) {
		if (value >= 10) {
			return String.valueOf(value);
		}
		return "0" + String.valueOf(value);
	}

	public static String getDurationString(long milliseconds) {
		if (milliseconds < 0) {
			return "";
		}

		long seconds = milliseconds / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;

		var durationString = hours + ":" + getTwoDigiNumber(minutes % 60) + ":" + getTwoDigiNumber(seconds % 60);
		return durationString;
	}
	
	public static String toString(Set<String> set) {
		var result = new StringBuilder();
		var it = set.iterator();
		for (int i = 0; it.hasNext(); i++) {
			result.append(it.next());
			if (i < set.size() - 1) {
				result.append(",");
			}
		}
		return result.toString();
	}

	public static String toSortedString(String[] strings) {
		Set<String> set = new TreeSet<String>();
		if (strings != null) {
			for (String s : strings) {
				set.add(s);
			}
		}
		return toString(set);
	}

	public static boolean hasElement(String[] array, String element) {
		if (element == null) {
			throw new IllegalArgumentException("Paramtere tag must not be null.");
		}
		if (array != null) {
			for (String s : array) {
				if (s.equals(element)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static String condense(String s) {
		while (s.contains("  ")) {
			s = s.replace("  ", " ");
		}
		s = s.trim();
		return s;
	}

	public static String getToday() {
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}

}