package org.atari.asma;

public class Utils {

	private Utils() {
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
}
