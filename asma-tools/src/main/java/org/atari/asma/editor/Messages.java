package org.atari.asma.editor;

import com.wudsn.tools.base.repository.Message;
import com.wudsn.tools.base.repository.NLS;

/**
 * Message repository of the SAPFileEditor, area ASA, numbers 401-499. The
 * archive checks use the same area with the numbers 001-299, see
 * {@link org.atari.asma.Messages}. See plans/06, phase 2.
 *
 * @author Peter Dell
 */
public final class Messages extends NLS {

	/** The user-defined short upper-case area identifier prefixing all message numbers of this class. */
	public static final String AREA = "ASA";

	/** The file path, the reason. */
	public static Message E401;
	public static Message W402;
	public static Message E403;
	/** The folder path. */
	public static Message I404;
	/** The number of files. */
	public static Message I405;
	/** The number of files. */
	public static Message I406;
	/** The file path. */
	public static Message I407;
	/** The number of files. */
	public static Message I408;
	/** The file path. */
	public static Message I409;
	/** The input file name, the output file name. */
	public static Message I410;
	/** The file path. */
	public static Message I411;

	static {
		initializeClass(Messages.class, null);
	}
}
