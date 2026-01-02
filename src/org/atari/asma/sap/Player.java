package org.atari.asma.sap;

public abstract class Player {

	public abstract boolean matches(SegmentList segmentList);

	public abstract String getName();

	protected boolean segmentMatches(SegmentList segmentList, int index, boolean header, int startAddress,
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
	};

}
