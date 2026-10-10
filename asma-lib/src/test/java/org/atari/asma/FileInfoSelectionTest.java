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
		assertTrue(new FileInfo.Selection(null, null, null, null, null, null, 0, null).matches(createFileInfo()));
	}

	@Test
	public void keywordMatchesFilePathTitleAuthorAndDateCaseInsensitively() {
		FileInfo fileInfo = createFileInfo();
		assertTrue(new FileInfo.Selection("HUMMEL", "", "", "", "", null, 0, "").matches(fileInfo)); // path/author
		assertTrue(new FileInfo.Selection("sketch", "", "", "", "", null, 0, "").matches(fileInfo)); // title
		assertTrue(new FileInfo.Selection("2020", "", "", "", "", null, 0, "").matches(fileInfo)); // date
		assertFalse(new FileInfo.Selection("zybex", "", "", "", "", null, 0, "").matches(fileInfo));
	}

	@Test
	public void fieldsFilterIndividually() {
		FileInfo fileInfo = createFileInfo();
		assertTrue(new FileInfo.Selection("", "hummel_jakub", "", "", "", null, 0, "").matches(fileInfo));
		assertTrue(new FileInfo.Selection("", "", "43", "fel", "", null, 0, "").matches(fileInfo));
		assertFalse(new FileInfo.Selection("", "", "", "yerzmyey", "", null, 0, "").matches(fileInfo));
		assertFalse(new FileInfo.Selection("", "", "", "", "1995", null, 0, "").matches(fileInfo));
	}

	@Test
	public void hardwareChannelsAndFormatFilter() {
		FileInfo fileInfo = createFileInfo();
		assertTrue(new FileInfo.Selection("", "", "", "", "", FileInfo.ATARI800, 2, "rmt").matches(fileInfo));
		assertFalse(new FileInfo.Selection("", "", "", "", "", FileInfo.ATARI2600, 0, "").matches(fileInfo));
		assertFalse(new FileInfo.Selection("", "", "", "", "", null, 1, "").matches(fileInfo));
		assertFalse(new FileInfo.Selection("", "", "", "", "", null, 0, "ttt").matches(fileInfo));
	}
}
