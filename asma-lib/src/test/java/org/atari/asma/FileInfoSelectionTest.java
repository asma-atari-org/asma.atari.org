package org.atari.asma;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class FileInfoSelectionTest {

	private static FileInfo createFileInfo() {
		FileInfo fileInfo = new FileInfo();
		fileInfo.filePath = "Composers/Hummel_Jakub/Sketch_43.sap";
		fileInfo.title = "Sketch 43";
		fileInfo.author = "Jakub Hummel (Fel)";
		fileInfo.date = "2020";
		fileInfo.hardware = FileInfo.ATARI800;
		fileInfo.originalModuleExt = "rmt";
		fileInfo.channels = 2;
		fileInfo.songs = 1;
		return fileInfo;
	}

	@Test
	public void emptySelectionMatchesEverything() {
		assertTrue(FileInfo.Selection.ALL.matches(createFileInfo()));
		assertTrue(new FileInfo.Selection(null, null, null, null, null, null, null, null).matches(createFileInfo()));
	}

	@Test
	public void keywordMatchesFilePathTitleAuthorAndDateCaseInsensitively() {
		FileInfo fileInfo = createFileInfo();
		assertTrue(new FileInfo.Selection("HUMMEL", "", "", "", "", null, null, "").matches(fileInfo)); // path/author
		assertTrue(new FileInfo.Selection("sketch", "", "", "", "", null, null, "").matches(fileInfo)); // title
		assertTrue(new FileInfo.Selection("2020", "", "", "", "", null, null, "").matches(fileInfo)); // date
		assertFalse(new FileInfo.Selection("zybex", "", "", "", "", null, null, "").matches(fileInfo));
	}

	@Test
	public void fieldsFilterIndividually() {
		FileInfo fileInfo = createFileInfo();
		assertTrue(new FileInfo.Selection("", "hummel_jakub", "", "", "", null, null, "").matches(fileInfo));
		assertTrue(new FileInfo.Selection("", "", "43", "fel", "", null, null, "").matches(fileInfo));
		assertFalse(new FileInfo.Selection("", "", "", "yerzmyey", "", null, null, "").matches(fileInfo));
		assertFalse(new FileInfo.Selection("", "", "", "", "1995", null, null, "").matches(fileInfo));
	}

	@Test
	public void hardwareChannelsAndFormatFilter() {
		FileInfo fileInfo = createFileInfo();
		assertTrue(new FileInfo.Selection("", "", "", "", "", Hardware.ATARI800, Channels.STEREO_8, "rmt").matches(fileInfo));
		assertFalse(new FileInfo.Selection("", "", "", "", "", Hardware.ATARI2600, null, "").matches(fileInfo));
		assertFalse(new FileInfo.Selection("", "", "", "", "", null, Channels.MONO_4, "").matches(fileInfo));
		assertFalse(new FileInfo.Selection("", "", "", "", "", null, null, "ttt").matches(fileInfo));
	}
}
