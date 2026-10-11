package org.atari.asma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.atari.asma.demozoo.GroupDemozooID;
import org.junit.jupiter.api.Test;

public class GroupSelectionTest {

	private static Group createGroup() {
		Group group = new Group();
		group.folderName = "Top_Odin";
		group.name = "Top Odin";
		group.demozooID = "52101";
		return group;
	}

	@Test
	public void emptySelectionMatchesEverything() {
		assertTrue(Group.Selection.ALL.matches(createGroup()));
		assertTrue(new Group.Selection(null, null).matches(createGroup()));
	}

	@Test
	public void nullGroupIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> Group.Selection.ALL.matches(null));
	}

	@Test
	public void keywordMatchesNameAndFolderNameCaseInsensitively() {
		Group group = createGroup();
		assertTrue(new Group.Selection("TOP ODIN", "").matches(group)); // name
		assertTrue(new Group.Selection("top_", "").matches(group)); // folder name
		assertFalse(new Group.Selection("zur", "").matches(group));
	}

	@Test
	public void nameFiltersOnlyTheName() {
		Group group = createGroup();
		assertTrue(new Group.Selection("", "odin").matches(group));
		assertFalse(new Group.Selection("", "top_odin").matches(group));
	}

	@Test
	public void nameIsDeserializedFromTheDatabase() throws IOException {
		// The JSON attribute is "name", as in "Docs/ASMA-Groups.json".
		var database = new ASMAClient().parseDatabase("""
				{"composerInfos": [], "fileInfos": [],
				 "groupInfos": [{ "folderName" : "Zur_soft", "name" : "Zur-soft", "demozooID" : "52078"}]}""");
		assertEquals("Zur-soft", database.groupInfos.get(0).name);
		assertEquals("52078", database.groupInfos.get(0).demozooID);
		assertEquals(GroupDemozooID.of(52078), database.groupInfos.get(0).getDemozooID());
	}

	@Test
	public void missingDemozooIDIsNull() {
		Group group = createGroup();
		group.demozooID = "";
		assertNull(group.getDemozooID());
	}
}
