package org.atari.asma.sap;

public class ASAPFileUtility {

	public int MILLIS_PER_SECOND = 1000;
	public int SECONDS_PER_MINUTE = 60;

	private ASAPFileUtility() {

	}

	public static int getFrameSize(boolean stereo) {
		return stereo ? 18 : 9;
	}

	public static String getTimeString(int millis) {
		int MILLIS_PER_SECOND = 1000;
		int SECONDS_PER_MINUTE = 60;

		int seconds = millis / MILLIS_PER_SECOND;
		millis -= seconds * MILLIS_PER_SECOND;
		int minutes = seconds / SECONDS_PER_MINUTE;
		seconds -= minutes * SECONDS_PER_MINUTE;
		String minutesString = (minutes < 10 ? "0" + minutes : "" + minutes);
		String secondsString = (seconds < 10 ? "0" + seconds : "" + seconds);
		String millisString = (millis < 100 ? "00" + millis : (millis < 100 ? "0" + millis : "" + millis));
		String timeString = minutesString + ":" + secondsString + "." + millisString;
		return timeString;
	}

	private static void assertEquals(String actual, String expected) {
		if (!actual.equals(expected)) {
			throw new RuntimeException("Actual " + actual + ", expected " + expected + ".");
		}
	}

	private static void assertTimeString(int millies, String result) {
		assertEquals(getTimeString(millies), result);
	}

	static {
		assertTimeString(0, "00:00.000");
		assertTimeString(1234, "00:01.234");
	}

}
