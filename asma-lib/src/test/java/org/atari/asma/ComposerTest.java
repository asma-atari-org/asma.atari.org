package org.atari.asma;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ComposerTest {

	@Test
	public void unicodeNamesAreDeserialized() throws IOException {
		var database = new ASMAClient().parseDatabase("{\"composerInfos\": [{\"lastName\": \"Sterba\","
				+ " \"lastNameUnicode\": \"Štěrba\", \"firstName\": \"Radek\"}],"
				+ " \"groupInfos\": [], \"fileInfos\": [], \"demozoo\": {\"productions\": []}}");
		Composer composer = database.composerInfos.get(0);
		assertEquals("Sterba", composer.lastName);
		// Regression: the field was misspelled "lastnNameUnicode" and never filled.
		assertEquals("Štěrba", composer.lastNameUnicode);
	}

	@Test
	public void attributesBindTheGsonFieldNames() {
		assertEquals("lastName", Composer.Attributes.LAST_NAME.getName());
		assertEquals("demozooID", Composer.Attributes.DEMOZOO_ID.getName());
		assertEquals(DataTypes.Composer_Country, Composer.Attributes.COUNTRY.getDataType());
	}
}
