package org.atari.asma.sap.player;

import java.util.List;

import org.atari.asma.sap.SegmentList;

public class RMT128StereoPlayer extends RMT128Player {

	public String getName() {
		return "RMT 1.28 - Stereo Player";
	}

	public boolean matches(SegmentList segmentList) {

		return matches(segmentList, STEREO_ENDADDRESS, "RMT8");
	}

	public void getTexts(SegmentList segmentList, List<String> texts) {
		getTextsInternal(segmentList, texts);
	}

}
