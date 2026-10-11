package org.atari.asma;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ComposerSelectionTest {

	private static Composer createComposer() {
		Composer composer = new Composer();
		composer.folderName = "Badkowski_Marek";
		composer.lastName = "Badkowski";
		composer.firstName = "Marek";
		composer.lastNameUnicode = "Bądkowski";
		composer.firstNameUnicode = "Marek";
		composer.handles = "Marek, Mareczek";
		composer.country = "Poland";
		composer.demozooID = "1234";
		composer.groups = "Slight";
		return composer;
	}

	@Test
	public void emptySelectionMatchesEverything() {
		assertTrue(Composer.Selection.ALL.matches(createComposer()));
		assertTrue(new Composer.Selection(null, null, null, null, null).matches(createComposer()));
	}

	@Test
	public void nullComposerIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> Composer.Selection.ALL.matches(null));
	}

	@Test
	public void keywordMatchesAllTextFieldsCaseInsensitively() {
		Composer composer = createComposer();
		assertTrue(new Composer.Selection("BADKOWSKI", "", "", "", "").matches(composer)); // name
		assertTrue(new Composer.Selection("mareczek", "", "", "", "").matches(composer)); // handles
		assertTrue(new Composer.Selection("poland", "", "", "", "").matches(composer)); // country
		assertTrue(new Composer.Selection("slight", "", "", "", "").matches(composer)); // groups
		assertTrue(new Composer.Selection("_marek", "", "", "", "").matches(composer)); // folder name
		assertFalse(new Composer.Selection("germany", "", "", "", "").matches(composer));
	}

	@Test
	public void nameMatchesBothSpellingsAndTheFullName() {
		Composer composer = createComposer();
		assertTrue(new Composer.Selection("", "badkowski", "", "", "").matches(composer));
		assertTrue(new Composer.Selection("", "Bądkowski", "", "", "").matches(composer));
		assertTrue(new Composer.Selection("", "marek bad", "", "", "").matches(composer));
		assertFalse(new Composer.Selection("", "poland", "", "", "").matches(composer));
	}

	@Test
	public void fieldsFilterIndividually() {
		Composer composer = createComposer();
		assertTrue(new Composer.Selection("", "", "mareczek", "pol", "sli").matches(composer));
		assertFalse(new Composer.Selection("", "", "yerzmyey", "", "").matches(composer));
		assertFalse(new Composer.Selection("", "", "", "germany", "").matches(composer));
		assertFalse(new Composer.Selection("", "", "", "", "taquart").matches(composer));
	}
}
