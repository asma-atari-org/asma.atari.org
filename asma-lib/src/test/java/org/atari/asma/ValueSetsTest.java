package org.atari.asma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.wudsn.tools.base.repository.ValueSet;

public class ValueSetsTest {

	@Test
	public void hardwareIDsAreTheStoredValues() {
		assertEquals(FileInfo.ATARI800, Hardware.ATARI800.getId());
		assertEquals(FileInfo.ATARI2600, Hardware.ATARI2600.getId());
		assertSame(Hardware.ATARI2600, Hardware.getInstance("ATARI2600"));
		// The JSON files omit the default.
		assertSame(Hardware.ATARI800, Hardware.getInstance(null));
		assertSame(Hardware.ATARI800, Hardware.getInstance(""));
		assertNull(Hardware.getInstance("C64"));
		assertEquals("Atari 2600", Hardware.ATARI2600.getText());
	}

	@Test
	public void channelsAreDerivedLikeTheWebsite() {
		// file.js getChannelsText()
		assertSame(Channels.MONO_2, Channels.of(Hardware.ATARI2600, 1));
		assertSame(Channels.MONO_4, Channels.of(Hardware.ATARI800, 1));
		assertSame(Channels.STEREO_8, Channels.of(Hardware.ATARI800, 2));
		assertEquals("Mono (2)", Channels.MONO_2.getText());
		assertEquals("Mono (4)", Channels.MONO_4.getText());
		assertEquals("Stereo (8)", Channels.STEREO_8.getText());
	}

	@Test
	public void valuesAreSortedBySortKey() {
		assertEquals(List.of(Hardware.ATARI800, Hardware.ATARI2600), ValueSet.getValues(Hardware.class));
		assertEquals(List.of(Channels.MONO_2, Channels.MONO_4, Channels.STEREO_8),
				ValueSet.getValues(Channels.class));
	}

	@Test
	public void fileInfoGettersAndSelection() {
		FileInfo fileInfo = new FileInfo();
		fileInfo.hardware = FileInfo.ATARI2600;
		fileInfo.channels = 1;
		assertSame(Hardware.ATARI2600, fileInfo.getHardware());
		assertSame(Channels.MONO_2, fileInfo.getChannels());
		assertEquals("Mono (2)", fileInfo.getChannelsText());

		// "Mono (4)" no longer matches the Atari 2600 files, see plans/13 V3.
		assertFalse(new FileInfo.Selection("", "", "", "", "", null, Channels.MONO_4, "").matches(fileInfo));
		assertTrue(new FileInfo.Selection("", "", "", "", "", null, Channels.MONO_2, "").matches(fileInfo));
	}
}
