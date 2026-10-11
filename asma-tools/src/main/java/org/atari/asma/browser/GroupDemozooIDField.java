package org.atari.asma.browser;

import org.atari.asma.demozoo.GroupDemozooID;

/**
 * Display field for a {@link GroupDemozooID}. See plans/10.
 *
 * @author Peter Dell
 */
public final class GroupDemozooIDField extends DemozooIDField<GroupDemozooID> {

	@Override
	protected String getFindText() {
		return Texts.DemozooIDField_FindGroup;
	}

	@Override
	protected String getFindURL(String searchText) {
		return GroupDemozooID.getFindURL(searchText);
	}

	@Override
	protected String getCreateText() {
		return Texts.DemozooIDField_CreateGroup;
	}

	@Override
	protected String getCreateURL() {
		return GroupDemozooID.getCreateURL();
	}
}
