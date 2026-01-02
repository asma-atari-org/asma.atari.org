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
			if (segment.header) {
				length += 2;

			}
			length += 4 + segment.content.length;
		}
		var result = new byte[length];
		int index = 0;
		for (var segment : entries) {
			var content = segment.toByteArray(false);
			System.arraycopy(content, 0, result, index, content.length);
			index += segment.content.length;
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int segmentCount = 0;
		for (var segment : entries) {
			sb.append("LOAD ");
			sb.append(ByteUtility.getByteHexString(segmentCount));
			sb.append(": ");
			sb.append(segment.toString());
			sb.append("\n");
			segmentCount++;
		}
		return sb.toString();
	}

}
