package org.atari.asma.demozoo;

import com.wudsn.tools.base.repository.Message;
import com.wudsn.tools.base.repository.NLS;

/**
 * Message repository of Demozoo, area DMZ: the checks of the productions
 * against ASMA (0xx) and the import of the Demozoo database (1xx). Sent by
 * asma-lib and asma-tools. See plans/06, phase 2.
 *
 * @author Peter Dell
 */
public final class Messages extends NLS {

	/** The user-defined short upper-case area identifier prefixing all message numbers of this class. */
	public static final String AREA = "DMZ";

	// Productions

	public static Message I001;
	public static Message W002;
	public static Message E003;
	/** The previous production, its file path, the production. */
	public static Message E004;
	public static Message W005;
	public static Message W006;
	public static Message I007;
	public static Message E008;
	public static Message W009;
	/** The file path. */
	public static Message E010;

	// Import

	/** The database file path. */
	public static Message I101;
	/** The database file path. */
	public static Message E102;
	/** The database file path. */
	public static Message E103;
	/** The database file path. */
	public static Message I104;
	/** The database file path, the reason. */
	public static Message E105;
	/** The file size. */
	public static Message I106;
	/** The page number, the URL. */
	public static Message I107;
	/** The URL, the reason. */
	public static Message E108;
	/** The production number, the production count, the URL, the time until now, the time to go. */
	public static Message I109;
	/** The URL, the HTTP status, the response. */
	public static Message E110;
	/** The URL, the reason. */
	public static Message E111;
	/** The file extensions, the URL. */
	public static Message I112;

	static {
		initializeClass(Messages.class, null);
	}
}
