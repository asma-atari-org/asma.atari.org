package org.atari.asma.util;

import java.util.Set;
import java.util.TreeSet;

public final class StringUtility {
	
	private StringUtility() {
		
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
}