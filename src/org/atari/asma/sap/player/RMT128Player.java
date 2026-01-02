package org.atari.asma.sap.player;

import java.util.List;

import org.atari.asma.sap.SAPFile;
import org.atari.asma.sap.SAPFileLogic;
import org.atari.asma.sap.SegmentList;
import org.atari.asma.util.MessageQueue;

public abstract class RMT128Player extends Player {

	protected static final int MONO_ENDADDRESS = 0x3958;
	protected static final int STEREO_ENDADDRESS = 0x3A64;

	private static final int RMT_SEGMENT = 3;

	protected boolean matches(SegmentList segmentList, int segment0EndAddres, String magicBytes) {
		/*
		 * RMT128 Mono Player
		 * 
		 * $3182-$3958, $3e00-$3ed5, $3f00-$3fc9, $4000-any: RMT4, $02e0-$02e1
		 * 
		 * RMT128 Stereo Player
		 * 
		 * $3182-$3A64, $3e00-$3ed5, $3f00-$3fc9, $4000-any: RMT8, $02e0-$02e1
		 * 
		 * $4000-any $2e0-$2e1
		 */
		if (segmentMatches(segmentList, 0, true, 0x3182, segment0EndAddres)
				&& segmentMatches(segmentList, 1, false, 0x3e00, 0x3ed5)
				&& segmentMatches(segmentList, 2, false, 0x3f00, 0x3fc9)
				&& segmentMatches(segmentList, 4, false, 0x02e0, 0x02e1)) {
			var segment = segmentList.get(RMT_SEGMENT);
			if (segment.header == false && segment.startAddress == 0x4000 && segment.contentEquals(0, magicBytes)) {
				return true;
			}
		}
		return false;
	}

	protected void getTextsInternal(SegmentList segmentList, List<String> texts) {
		final int WIDTH = 40;
		for (int i = 0; i < 5; i++) {
			texts.add(segmentList.get(2).getContentScreenCodeString(i * WIDTH, WIDTH));
		}
	}

	public boolean fillSAPFile(SAPFile sapFile, SegmentList segmentList, MessageQueue messageQueue) {
		var sapFileLogic = new SAPFileLogic();
		var rmtContent = segmentList.get(RMT_SEGMENT).toByteArray(true);
		var rmtFile = sapFileLogic.loadOriginalModuleFile("Converted.rmt", rmtContent, messageQueue);
		if (rmtFile == null) {
			return false;
		}
		sapFile.content = rmtFile.content;
		sapFile.asapInfo = rmtFile.asapInfo;
		return true;
	}

}
