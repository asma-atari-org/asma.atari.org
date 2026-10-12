package org.atari.asma.editor;

import com.wudsn.tools.base.repository.Action;
import com.wudsn.tools.base.repository.NLS;

/**
 * Action repository of the SAPFileEditor.
 *
 * @author Peter Dell
 */
public final class Actions extends NLS {

	public static Action SAPFileDialog_Open;
	public static Action SAPFileDialog_Save;
	public static Action FilePanel_Play;
	public static Action FilePanel_OpenFolder;

	static {
		initializeClass(Actions.class, null);
	}
}
