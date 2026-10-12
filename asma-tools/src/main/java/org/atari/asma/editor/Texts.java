package org.atari.asma.editor;

import com.wudsn.tools.base.repository.NLS;

/**
 * Text repository of the SAPFileEditor.
 *
 * @author Peter Dell
 */
public final class Texts extends NLS {

	public static String SAPFileList_Title;
	/** The file name, the title, the author. */
	public static String SAPFileDialog_Title;
	public static String SAPFileDialog_InputFile;
	public static String SAPFileDialog_SegmentsFolder;
	public static String SAPFileDialog_OutputFile;
	public static String FilePanel_Exists;
	public static String FilePanel_New;
	public static String FilePanel_Directory;

	static {
		initializeClass(Texts.class, null);
	}
}
