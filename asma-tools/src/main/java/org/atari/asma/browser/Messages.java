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
	/** No local database was found: the candidate paths. */
	public static Message E003;

	static {
		initializeClass(Messages.class, null);
	}
}
