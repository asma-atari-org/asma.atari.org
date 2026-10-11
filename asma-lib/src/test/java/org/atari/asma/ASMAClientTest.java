package org.atari.asma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ASMAClientTest {

	// The database generated into the website, present in this repository.
	private static final File DATABASE_FILE = new File("../public_html/asmadb/asmadb.js");

	@Test
	public void testParseDatabase() throws IOException {
		var client = new ASMAClient();
		var database = client.parseDatabase("""
				const asma = {
				"composerInfos": [{"lastName": "Hummel", "firstName": "Jakub", "handles": "Fel",
				 "country": "Poland", "folderName": "Fel", "demozooID": "56466", "groups": ""}],
				"groupInfos": [],
				"fileInfos": [
				{"filePath": "Composers/Fel/Test.sap", "fileSize": 4711, "title": "Test",
				 "author": "Fel", "date": "2020", "typeLetter": "B", "originalModuleExt": "rmt",
				 "songs": 2, "defaultSongIndex": 1},
				{"filePath": "Games/Zybex.ttt", "fileSize": 100, "hardware": "ATARI2600", "title": "Zybex",
				 "author": "?", "date": "", "typeLetter": "T", "originalModuleExt": "ttt"}],
				"demozoo": {"productions": [{"id": 1, "title": "Test", "authorIDs": [2],
				 "urlFilePath": "Composers/Fel/Test.sap"}]}
				}""");

		assertEquals(1, database.composerInfos.size());
		assertEquals("Fel", database.composerInfos.get(0).folderName);
		assertTrue(database.groupInfos.isEmpty());

		assertEquals(2, database.fileInfos.size());
		var sapFileInfo = database.fileInfos.get(0);
		// Omitted default values must be applied after parsing.
		assertEquals(FileInfo.ATARI800, sapFileInfo.hardware);
		assertEquals(1, sapFileInfo.channels);
		assertEquals(2, sapFileInfo.songs);
		assertEquals(1, sapFileInfo.defaultSongIndex);
		assertEquals(FileInfo.ATARI2600, database.fileInfos.get(1).hardware);
		assertEquals(1, database.fileInfos.get(1).songs);

		assertEquals(1, database.demozoo.productions.size());
		assertEquals(1, database.demozoo.productions.get(0).id);
		assertEquals("Composers/Fel/Test.sap", database.demozoo.productions.get(0).urlFilePath);

		assertEquals("https://asma.atari.org/asma/Composers/Fel/Test.sap", client.getFileURL(sapFileInfo));
	}

	private static FileInfo createFileInfo(String filePath) {
		FileInfo fileInfo = new FileInfo();
		fileInfo.filePath = filePath;
		return fileInfo;
	}

	@Test
	public void testFileCountByComposerFolderName() {
		var database = new ASMAClient.Database();
		assertTrue(database.getFileCountByComposerFolderName().isEmpty());

		database.fileInfos = java.util.List.of(createFileInfo("Composers/Fel/Test.sap"),
				createFileInfo("Composers/Fel/Unknown/Other.sap"), createFileInfo("Composers/Aki/A.sap"),
				createFileInfo("Composers/Loose.sap"), createFileInfo("Games/Zybex.sap"));
		var fileCounts = database.getFileCountByComposerFolderName();
		assertEquals(2, fileCounts.size());
		assertEquals(2, fileCounts.get("Fel"));
		assertEquals(1, fileCounts.get("Aki"));
	}

	@Test
	public void testLoadDatabaseFromFile() throws IOException {
		assumeTrue(DATABASE_FILE.isFile(), "Generated database " + DATABASE_FILE + " is not present.");

		var client = new ASMAClient();
		var database = client.loadDatabase(DATABASE_FILE);

		assertNotNull(database.composerInfos);
		assertNotNull(database.groupInfos);
		assertNotNull(database.fileInfos);
		assertNotNull(database.demozoo);
		assertNotNull(database.demozoo.productions);

		assertFalse(database.composerInfos.isEmpty());
		assertFalse(database.fileInfos.isEmpty());
		assertFalse(database.demozoo.productions.isEmpty());

		for (var fileInfo : database.fileInfos) {
			assertNotNull(fileInfo.filePath, "filePath must be set");
			assertNotNull(fileInfo.hardware, "hardware must be defaulted");
			assertTrue(fileInfo.channels >= 1, "channels must be defaulted");
			assertTrue(fileInfo.songs >= 1, "songs must be defaulted");
		}
	}
}
