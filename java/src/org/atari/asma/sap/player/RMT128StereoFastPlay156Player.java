package org.atari.asma.sap.player;

import org.atari.asma.sap.SegmentList;

public class RMT128StereoFastPlay156Player extends RMT128Player {

	public String getName() {
		return "RMT 1.28 - Stereo Player (FASTPLAY 156)";
	}

	public boolean matches(SegmentList segmentList) {

		return matches(segmentList, STEREO_ENDADDRESS, STEREO_CRC32_FASTPLAY_156, "RMT8");
	}

}
