package org.atari.asma.sap.player;

import java.util.List;

import org.atari.asma.sap.ASAPFile;
import org.atari.asma.sap.SegmentList;
import org.atari.asma.util.MessageQueue;

import net.sf.asap.ASAPInfo;

/**
 * V1.0 final commit https://github.com/VinsCool/VUPlayer-LZSS/commit/66a4c81d8bb15d3b7f9e9ab32f38a6ca0cfdc90d
 * See https://github.com/VinsCool/VUPlayer-LZSS/tree/66a4c81d8bb15d3b7f9e9ab32f38a6ca0cfdc90d.
 * See https://github.com/VinsCool/VUPlayer-LZSS/blob/66a4c81d8bb15d3b7f9e9ab32f38a6ca0cfdc90d/VUPlayer.asm
 * 
 * V1.1 initial commit https://github.com/VinsCool/VUPlayer-LZSS/commit/82365c918c467d6bedd73c4b5899e6bc2b4b220f#diff-8dda124f34303dd761acb42425b8219513d92e23e0b28678cc2cb3a2ed76c6e5
 * V1.2. initial commit: https://github.com/VinsCool/VUPlayer-LZSS/commit/4c07a54e8d24466b01afa6e7b21053771578963b
 * 
 * @author JAC
 *
 */
public class RMT134PlayerV1 extends RMTPlayer {

	private static final int LZSS_SEGMENT = 3;

	public String getName() {
		return "RMT 1.34 - VUPlayer-LZSS by VinsCool V1.0";
	}

	public boolean matches(SegmentList segmentList) {
		/*
		 * RMT134 - VUPlayer-LZSS by VinsCool V1.0
		 * 
		 * $1900-$1eff, $2000-$2FFF, $02e0-$02e1, $3000-any: data
		 * 
		 * The screen text starts at $2EBC.
		 */
		if (segmentList.size() == 4 && segmentMatches(segmentList, 0, true, 0x1900, 0x1eff)
				&& segmentMatches(segmentList, 1, false, 0x2000, 0x2fff)
				&& segmentMatches(segmentList, 2, false, 0x02e0, 0x02e1)) {
			var segment = segmentList.get(LZSS_SEGMENT);
			if (segment.header == false && segment.startAddress == 0x3000) {
				return true;
			}
		}
		return false;
	}

	public void getTexts(SegmentList segmentList, List<String> texts) {
		getScreenCodeTexts(segmentList.get(1), 0x2ebc - 0x2000, texts);

	}

	public boolean fillSAPFile(ASAPFile asapFile, SegmentList segmentList, MessageQueue messageQueue) {

		asapFile.content = new byte[0];
		asapFile.setASAPInfo(new ASAPInfo());
		return fillSAPFileInternal(asapFile, segmentList, messageQueue);
	}

}
