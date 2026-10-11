package org.atari.asma;

import com.wudsn.tools.base.repository.Message;
import com.wudsn.tools.base.repository.NLS;

/**
 * Message repository of the ASMA archive checks, area ASA, numbers 001-299:
 * files (0xx), composers (1xx) and groups (2xx) against the folders and the
 * database. The ASMA programs use the same area with their own ranges in
 * their own packages: the exporter 3xx, the editor 4xx and the browser 5xx.
 * See plans/06, phase 2.
 *
 * @author Peter Dell
 */
public final class Messages extends NLS {

	/** The user-defined short upper-case area identifier prefixing all message numbers of this class. */
	public static final String AREA = "ASA";

	// Files

	/** The file path. */
	public static Message E001;
	// ASA-002 (file name longer than 26 characters) was dropped in 2026, see FileInfoList.
	/** The file path, the invalid character, its hex code, the index. */
	public static Message E003;
	/** The file path, the author, the file's authors, the composer's authors. */
	public static Message W004;
	/** The file path, the author, the file's authors. */
	public static Message I005;
	/** The file path. */
	public static Message E006;
	/** The file path. */
	public static Message E007;
	/** The song numbers, the file path. */
	public static Message W008;
	/** The file path, the maximum length. */
	public static Message E009;
	/** The file path, the date, the reason. */
	public static Message E010;
	/** The file path. */
	public static Message W011;
	/** The file path, the invalid character, its hex code, the index. */
	public static Message E012;

	// Composers

	/** The folder name, the composer, the default folder name. */
	public static Message E101;
	/** The composer. */
	public static Message E102;
	/** The composer. */
	public static Message E103;
	/** The folder, the composer. */
	public static Message E104;
	/** The path, the composer. */
	public static Message E105;
	/** The path. */
	public static Message E106;
	/** The folder. */
	public static Message E107;
	/** The group folder name, the composer. */
	public static Message E108;

	// Groups

	/** The group. */
	public static Message E201;
	/** The group. */
	public static Message E202;
	/** The folder, the group. */
	public static Message E203;
	/** The path, the group. */
	public static Message E204;
	/** The path. */
	public static Message E205;
	/** The folder. */
	public static Message E206;

	static {
		initializeClass(Messages.class, null);
	}
}
