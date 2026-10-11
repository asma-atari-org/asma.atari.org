package org.atari.asma.browser;

import org.atari.asma.demozoo.MusicDemozooID;

/**
 * Display field for a {@link MusicDemozooID}. See plans/10.
 *
 * @author Peter Dell
 */
public final class MusicDemozooIDField extends DemozooIDField<MusicDemozooID> {

	@Override
	protected String getFindText() {
		return Texts.DemozooIDField_FindMusic;
	}

	@Override
	protected String getFindURL(String searchText) {
		return MusicDemozooID.getFindURL(searchText);
	}

	@Override
	protected String getCreateText() {
		return Texts.DemozooIDField_CreateMusic;
	}

	@Override
	protected String getCreateURL() {
		return MusicDemozooID.getCreateURL();
	}
}
