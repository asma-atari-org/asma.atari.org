package org.atari.asma.sap.player;

import org.atari.asma.sap.SegmentList;

public class RMT128Patch8MonoPlayer extends RMT128Player {

	public String getName() {
		return "RMT 1.28 Patch 8 by Analmux - Mono Player";
	}

	public boolean matches(SegmentList segmentList) {

		return matches(segmentList, MONO_ENDADDRESS, PATCH8_MONO_CRC32, "RMT4");
	}

}
