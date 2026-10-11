package org.atari.asma.browser;

import com.wudsn.tools.base.repository.Message;
import com.wudsn.tools.base.repository.NLS;

/**
 * Message repository of the ASMABrowser, area ASA, numbers 501-599. The
 * archive checks use the same area with the numbers 001-299, see
 * {@link org.atari.asma.Messages}. See plans/06, phase 2.
 *
 * @author Peter Dell
 */
public final class Messages extends NLS {

	/** The user-defined short upper-case area identifier prefixing all message numbers of this class. */
	public static final String AREA = "ASA";

	/** The database was loaded: the file count, the source. */
	public static Message I501;
	/** The database could not be loaded: the source, the reason. */
	public static Message E502;
	/** A file could not be played: the file path, the reason. */
	public static Message E504;
	/** The database is being loaded: the source. */
	public static Message S505;
	/** A web page could not be opened: the URL, the reason. */
	public static Message E506;

	static {
		initializeClass(Messages.class, null);
	}
}
