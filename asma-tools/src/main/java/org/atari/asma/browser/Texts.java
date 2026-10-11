package org.atari.asma.browser;

import com.wudsn.tools.base.repository.NLS;

/**
 * Text repository of the ASMABrowser.
 *
 * @author Peter Dell
 */
public final class Texts extends NLS {

	public static String Replay_PlayingTime;
	public static String LoadFromLocalFileDialog_Title;
	public static String LoadFromLocalFileDialog_Filter;

	static {
		initializeClass(Texts.class, null);
	}
}
