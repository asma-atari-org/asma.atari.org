package org.atari.asma.sap.player;

import java.util.List;

import org.atari.asma.sap.SAPFile;
import org.atari.asma.sap.SegmentList;
import org.atari.asma.util.MessageQueue;

public abstract class Player {

	public abstract String getName();

	public abstract boolean matches(SegmentList segmentList);

	public void getTexts(SegmentList segmentList, List<String> texts) {
	}

	public boolean fillSAPFile(SAPFile sapFile, SegmentList segmentList, MessageQueue messageQueue) {
		return false;
	}

	protected static boolean segmentMatches(SegmentList segmentList, int index, boolean header, int startAddress,
			int endAddress) {
		if (index >= segmentList.size()) {
			return false;
		}
		var segment = segmentList.get(index);
		if (segment.header != header) {
			return false;
		}
		if (segment.startAddress != startAddress) {
			return false;
		}
		if (segment.endAddress != endAddress) {
			return false;
		}
		return true;
	}

}
