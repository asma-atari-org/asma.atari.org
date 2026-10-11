package org.atari.asma.sap;

import com.wudsn.tools.base.repository.Message;
import com.wudsn.tools.base.repository.NLS;

/**
 * Message repository of the SAP file format: reading, checking, converting and
 * saving SAP files, including their segments. Sent by asma-lib and asma-tools.
 * See plans/06, phase 2.
 *
 * @author Peter Dell
 */
public final class Messages extends NLS {

	/** The user-defined short upper-case area identifier prefixing all message numbers of this class. */
	public static final String AREA = "SAP";

	/** The file name. */
	public static Message E001;
	/** The invalid character, the index. */
	public static Message E002;
	/** The index. */
	public static Message E003;
	/** The invalid character, the index. */
	public static Message E004;
	public static Message E005;
	public static Message E006;
	/** The tag. */
	public static Message E007;
	/** The tag, the recommended tag sequence. */
	public static Message W008;
	/** The tag. */
	public static Message W009;
	/** The tag. */
	public static Message W010;
	/** The tag. */
	public static Message W011;
	/** The index. */
	public static Message E012;
	/** The reason. */
	public static Message E013;
	/** The reason. */
	public static Message E014;
	/** The reason. */
	public static Message E015;
	public static Message E016;
	/** The reason. */
	public static Message E017;
	/** The reason. */
	public static Message E018;
	/** The reason. */
	public static Message E019;
	/** The reason. */
	public static Message E020;
	/** The reason. */
	public static Message E021;
	/** The file path. */
	public static Message E022;
	/** The folder path. */
	public static Message E023;
	public static Message E024;
	/** The segment number, the index, the start address, the end address. */
	public static Message E025;
	/** The segment number, the index. */
	public static Message E026;
	/** The reason. */
	public static Message E027;
	/** The segment number. */
	public static Message I028;
	/** The player name. */
	public static Message I029;
	/** The module type, the start address, the end address. */
	public static Message I030;
	/** The file path. */
	public static Message I031;

	static {
		initializeClass(Messages.class, null);
	}
}
