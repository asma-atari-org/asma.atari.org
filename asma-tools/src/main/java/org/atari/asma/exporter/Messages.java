package org.atari.asma.exporter;

import com.wudsn.tools.base.repository.Message;
import com.wudsn.tools.base.repository.NLS;

/**
 * Message repository of the ASMAExporter and its FileInfoScanner, area ASA,
 * numbers 301-399. The archive checks use the same area with the numbers
 * 001-299, see {@link org.atari.asma.Messages}. See plans/06, phase 2.
 *
 * @author Peter Dell
 */
public final class Messages extends NLS {

	/** The user-defined short upper-case area identifier prefixing all message numbers of this class. */
	public static final String AREA = "ASA";

	/** The reason. */
	public static Message E301;
	/** The STIL file path. */
	public static Message I302;
	/** The number of STIL entries. */
	public static Message I303;
	/** The composers file path. */
	public static Message I304;
	/** The number of composers. */
	public static Message I305;
	/** The groups file path. */
	public static Message I306;
	/** The number of groups. */
	public static Message I307;
	/** The Demozoo database file path. */
	public static Message I308;
	/** The number of productions. */
	public static Message I309;
	/** The number of file infos, the database file path. */
	public static Message I310;
	/** The database file path, the file size. */
	public static Message I311;
	/** The HTML file path, the file size. */
	public static Message I312;
	/** The folder path. */
	public static Message I313;
	/** The number of files. */
	public static Message I314;
	/** The file path. */
	public static Message I315;

	static {
		initializeClass(Messages.class, null);
	}
}
