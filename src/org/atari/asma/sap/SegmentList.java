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

	public Segment get(int i) {
		return entries.get(i);
	}

	public int size() {
		return entries.size();
	}

	public byte[] toByteArray() {
		int length = 0;
		for (var segment : entries) {
			length += segment.content.length;
		}
		var result = new byte[length];
		int index = 0;
		for (var segment : entries) {
			System.arraycopy(segment.content, 0, result, index, segment.content.length);
			length += segment.content.length;
		}
		return result;
	}
}
