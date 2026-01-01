package org.atari.asma.sap;

import java.util.ArrayList;
import java.util.List;

public class SegmentList {

	private List<Segment> entries;

	public SegmentList() {
		entries = new ArrayList<Segment>();
	}

	public void clear() {
		entries.clear();
	}

	public List<Segment> getEntries() {
		return entries;
	}

	public void add(Segment segment) {
		entries.add(segment);
	}
}
