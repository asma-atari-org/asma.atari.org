package org.atari.asma.browser;

import com.wudsn.tools.base.repository.Message;
import com.wudsn.tools.base.repository.NLS;

/**
 * Message repository of the ASMABrowser.
 *
 * @author Peter Dell
 */
public final class Messages extends NLS {

	/** The user-defined short upper-case area identifier prefixing all message numbers of this class. */
	public static final String AREA = "ASB"; // ASMABrowser

	/** The database was loaded: the file count, the source. */
	public static Message I001;
	/** The database could not be loaded: the source, the reason. */
	public static Message E002;
	/** A file could not be played: the file path, the reason. */
	public static Message E004;
	/** The database is being loaded: the source. */
	public static Message S005;

	static {
		initializeClass(Messages.class, null);
	}
}
