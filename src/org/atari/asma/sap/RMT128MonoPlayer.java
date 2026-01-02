package org.atari.asma.sap;

public class RMT128MonoPlayer extends RMT128Player {

	public boolean matches(SegmentList segmentList) {

		return matches(segmentList, MONO_ENDADDRESS, "RMT4");
	}

	public String getName() {
		return "RMT 1.28 - Stereo Player";
	}

}
