package org.atari.asma.sap.player;

import java.util.List;

import org.atari.asma.sap.ASAPFile;
import org.atari.asma.sap.ASAPFileLogic;
import org.atari.asma.sap.SegmentList;
import org.atari.asma.util.MessageQueue;

public abstract class RMT128Player extends RMTPlayer {

	protected static final int MONO_ENDADDRESS = 0x3958;
	protected static final int STEREO_ENDADDRESS = 0x3A64;

	private static final int TEXT_SEGMENT = 2;

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
		if (segmentList.size() == 5 && segmentMatches(segmentList, 0, true, 0x3182, segment0EndAddres)
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

	public final void getTexts(SegmentList segmentList, List<String> texts) {
		getScreenCodeTexts(segmentList.get(TEXT_SEGMENT), 0, texts);

	}

	public boolean fillSAPFile(ASAPFile asapFile, SegmentList segmentList, MessageQueue messageQueue) {
		var sapFileLogic = new ASAPFileLogic();
		var rmtContent = segmentList.get(RMT_SEGMENT).toByteArray(true);
		var rmtFile = sapFileLogic.loadOriginalModuleFile("Converted.rmt", rmtContent, messageQueue);
		if (rmtFile == null) {
			return false;
		}
		asapFile.content = rmtFile.content;
		asapFile.setASAPInfo(rmtFile.getASAPInfo());
		return fillSAPFile(asapFile, segmentList, messageQueue);
	}

}
