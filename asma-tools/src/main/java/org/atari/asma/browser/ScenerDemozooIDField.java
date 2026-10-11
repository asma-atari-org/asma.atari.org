package org.atari.asma.browser;

import org.atari.asma.demozoo.ScenerDemozooID;

/**
 * Display field for a {@link ScenerDemozooID}. See plans/10.
 *
 * @author Peter Dell
 */
public final class ScenerDemozooIDField extends DemozooIDField<ScenerDemozooID> {

	@Override
	protected String getFindText() {
		return Texts.DemozooIDField_FindScener;
	}

	@Override
	protected String getFindURL(String searchText) {
		return ScenerDemozooID.getFindURL(searchText);
	}

	@Override
	protected String getCreateText() {
		return Texts.DemozooIDField_CreateScener;
	}

	@Override
	protected String getCreateURL() {
		return ScenerDemozooID.getCreateURL();
	}
}
