package org.atari.asma.demozoo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class DemozooIDTest {

	@Test
	public void missingOrInvalidIDsAreNull() {
		assertNull(DemozooID.parse(DemozooID.Kind.SCENER, null));
		assertNull(DemozooID.parse(DemozooID.Kind.SCENER, ""));
		assertNull(DemozooID.parse(DemozooID.Kind.SCENER, "abc"));
		assertNull(DemozooID.parse(DemozooID.Kind.SCENER, "0"));
		assertNull(DemozooID.of(DemozooID.Kind.MUSIC, 0));
	}

	@Test
	public void urlsMatchTheWebsite() {
		// composer.js getDemozooURL(), demozoo.js getMusicHTMLForDemozooID().
		assertEquals("https://demozoo.org/sceners/8931", DemozooID.parse(DemozooID.Kind.SCENER, " 8931 ").getURL());
		assertEquals("https://demozoo.org/music/12345", DemozooID.of(DemozooID.Kind.MUSIC, 12345).getURL());
		assertEquals("https://demozoo.org/groups/52101", DemozooID.parse(DemozooID.Kind.GROUP, "52101").getURL());
		assertEquals("8931", DemozooID.of(DemozooID.Kind.SCENER, 8931).toString());
	}

	@Test
	public void findAndCreateURLs() {
		assertEquals("https://demozoo.org/search/?q=Jakub+Hummel&category=scener",
				DemozooID.Kind.SCENER.getFindURL("Jakub Hummel"));
		assertEquals("https://demozoo.org/sceners/new/", DemozooID.Kind.SCENER.getCreateURL());
		assertEquals("https://demozoo.org/groups/new/", DemozooID.Kind.GROUP.getCreateURL());
		assertEquals("https://demozoo.org/music/new/", DemozooID.Kind.MUSIC.getCreateURL());
	}

	@Test
	public void equalityUsesKindAndID() {
		assertEquals(DemozooID.of(DemozooID.Kind.GROUP, 1), DemozooID.parse(DemozooID.Kind.GROUP, "1"));
		assertEquals(false, DemozooID.of(DemozooID.Kind.GROUP, 1).equals(DemozooID.of(DemozooID.Kind.SCENER, 1)));
	}
}
