package org.atari.asma.browser;

import com.wudsn.tools.base.repository.Action;
import com.wudsn.tools.base.repository.NLS;

/**
 * Action repository of the ASMABrowser. The top-level "File" and "Help" menus
 * reuse {@link com.wudsn.tools.base.Actions}' own fields, like every other
 * WUDSN Swing tool; only the application-specific entries live here.
 *
 * @author Peter Dell
 */
public final class Actions extends NLS {

	public static Action MainMenu_File_Exit;
	public static Action MainMenu_Database;
	public static Action MainMenu_Database_ReloadLocal;
	public static Action MainMenu_Database_LoadFromWebsite;
	public static Action MainMenu_Help_About;

	public static Action Replay_Play;
	public static Action Replay_Pause;
	public static Action Replay_Stop;
	public static Action Replay_Shuffle;

	static {
		initializeClass(Actions.class, null);
	}
}
