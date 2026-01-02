package org.atari.asma.sap.player;

import org.atari.asma.sap.SegmentList;

public class RMT128MonoPlayer extends RMT128Player {

	public String getName() {
		return "RMT 1.28 - Stereo Player";
	}

	public boolean matches(SegmentList segmentList) {

		return matches(segmentList, MONO_ENDADDRESS, "RMT4");
	}

}
