package org.atari.asma.sap;

public class RMT128StereoPlayer extends RMT128Player {

	public boolean matches(SegmentList segmentList) {

		return matches(segmentList, STEREO_ENDADDRESS, "RMT8");
	}

	public String getName() {
		return "RMT 1.28 - Stereo Player";
	}

}
