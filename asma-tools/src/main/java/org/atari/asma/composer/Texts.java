package org.atari.asma.composer;

import com.wudsn.tools.base.repository.NLS;

/**
 * Text repository of the reusable composer list.
 *
 * @author Peter Dell
 */
public final class Texts extends NLS {

	public static String ComposerListDialog_Title;
	public static String ComposerListDialog_ChooseTitle;

	static {
		initializeClass(Texts.class, null);
	}
}
