package org.atari.asma.util;

public class MemoryUtility {

	private MemoryUtility() {

	}

	public static String getRoundedMemorySize(long size) {
	
		long kb=(size+1023)/1024;
		if (kb==0) {
			return size+ " b";
		}
		long mb=(kb+1023)/1024;
		if (mb==0) {
			return size+ " kb";
		}
		return mb+ " mb";
	}
}
