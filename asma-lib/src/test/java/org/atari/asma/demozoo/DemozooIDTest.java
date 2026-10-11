package org.atari.asma.demozoo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class DemozooIDTest {

	@Test
	public void missingOrInvalidIDsAreNull() {
		assertNull(ScenerDemozooID.parse(null));
		assertNull(ScenerDemozooID.parse(""));
		assertNull(ScenerDemozooID.parse("abc"));
		assertNull(GroupDemozooID.parse("0"));
		assertNull(GroupDemozooID.parse("-1"));
		assertNull(MusicDemozooID.of(0));
	}

	@Test
	public void urlsMatchTheWebsite() {
		// composer.js getDemozooURL(), demozoo.js getMusicHTMLForDemozooID().
		assertEquals("https://demozoo.org/sceners/8931", ScenerDemozooID.parse(" 8931 ").getURL());
		assertEquals("https://demozoo.org/music/12345", MusicDemozooID.of(12345).getURL());
		assertEquals("https://demozoo.org/groups/52101", GroupDemozooID.parse("52101").getURL());
		assertEquals("8931", ScenerDemozooID.of(8931).toString());
		assertEquals(8931, ScenerDemozooID.of(8931).getID());
		assertEquals(Integer.valueOf(8931), DemozooID.toInteger(ScenerDemozooID.of(8931)));
		assertNull(DemozooID.toInteger(null));
	}

	@Test
	public void findAndCreateURLs() {
		assertEquals("https://demozoo.org/search/?q=Jakub+Hummel&category=scener",
				ScenerDemozooID.getFindURL("Jakub Hummel"));
		assertEquals("https://demozoo.org/search/?q=Top&category=group", GroupDemozooID.getFindURL("Top"));
		assertEquals("https://demozoo.org/search/?q=Tune&category=music", MusicDemozooID.getFindURL("Tune"));
		assertEquals("https://demozoo.org/sceners/new/", ScenerDemozooID.getCreateURL());
		assertEquals("https://demozoo.org/groups/new/", GroupDemozooID.getCreateURL());
		assertEquals("https://demozoo.org/music/new/", MusicDemozooID.getCreateURL());
	}

	@Test
	public void equalityUsesClassAndID() {
		assertEquals(GroupDemozooID.of(1), GroupDemozooID.parse("1"));
		assertEquals(GroupDemozooID.of(1).hashCode(), GroupDemozooID.parse("1").hashCode());
		assertNotEquals(GroupDemozooID.of(1), ScenerDemozooID.of(1));
		assertNotEquals(GroupDemozooID.of(1), GroupDemozooID.of(2));
	}
}
