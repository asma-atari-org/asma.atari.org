package org.atari.asma;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FileInfoTest {

	@Test
	public void channelsTextMatchesTheWebsite() {
		FileInfo fileInfo = new FileInfo();
		fileInfo.hardware = FileInfo.ATARI800;
		fileInfo.channels = 1;
		assertEquals("Mono (4)", fileInfo.getChannelsText());
		fileInfo.channels = 2;
		assertEquals("Stereo (8)", fileInfo.getChannelsText());
		fileInfo.hardware = FileInfo.ATARI2600;
		fileInfo.channels = 1;
		assertEquals("Mono (2)", fileInfo.getChannelsText());
	}
}
