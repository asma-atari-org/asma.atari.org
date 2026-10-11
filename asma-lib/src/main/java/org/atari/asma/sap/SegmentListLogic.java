package org.atari.asma.sap;

import java.io.File;

import org.atari.asma.util.FileUtility;

import com.wudsn.tools.base.common.MessageQueue;

public class SegmentListLogic {

	public boolean loadSegmentList(SegmentList segmentList, byte[] content, int index, MessageQueue messageQueue) {

		int segmentCount = 0;

		segmentList.clear();
		if (content.length < 7) {
			messageQueue.sendMessage(this, null, Messages.E024);
			return false;
		}
		while (index + 4 < content.length) {
			var segmentStartIndex = index;
			Segment segment = new Segment();
			int w = ByteUtility.getWord(content, index);
			index += 2;
			if (w == 0xffff) {
				w = ByteUtility.getWord(content, index);
				index += 2;
				segment.header = true;
			}
			segment.startAddress = w;
			w = ByteUtility.getWord(content, index);
			index += 2;
			segment.endAddress = w;
			if (segment.startAddress > segment.endAddress) {
				messageQueue.sendMessage(this, null, Messages.E025, String.valueOf(segmentCount),
						ByteUtility.getIndexString(segmentStartIndex), ByteUtility.getWordHexString(segment.startAddress),
						ByteUtility.getWordHexString(segment.endAddress));
				return false;
			}
			var binaryStartIndex = index;
			segmentList.add(segment);
			segment.content = new byte[segment.getLength()];
			System.arraycopy(content, binaryStartIndex, segment.content, 0, segment.getLength());
			index += segment.getLength();

			segmentCount++;
		}
		if (index != content.length) {
			messageQueue.sendMessage(this, null, Messages.E026, String.valueOf(segmentCount),
					ByteUtility.getIndexString(index));
			return false;
		}
		return true;
	}

	public boolean loadSegmentList(SegmentList segmentList, File file, MessageQueue messageQueue) {
		var content = FileUtility.readAsByteArray(file);
		return loadSegmentList(segmentList, content, 0, messageQueue);
	}
}