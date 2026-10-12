package org.atari.asma.asap;

import java.util.List;

import org.atari.asma.FileInfo;

import net.sf.asap.ASAPFormatException;
import net.sf.asap.ASAPInfo;

/**
 * The descriptions of the original module formats, like asma.js determines
 * them after loading the database: from ASAP, "TIATracker" for TIATracker
 * files.
 *
 * @author Peter Dell
 */
public final class ModuleFormatUtility {

	private ModuleFormatUtility() {
	}

	/**
	 * @param originalModuleExt
	 *            The file extension of the original module, e.g. "rmt", may be
	 *            <code>null</code>.
	 * @return The description, e.g. "Raster Music Tracker", or "Unknown", not
	 *         <code>null</code>.
	 */
	public static String getDescription(String originalModuleExt) {
		if (originalModuleExt == null || originalModuleExt.isEmpty()) {
			return "Unknown";
		}
		if (originalModuleExt.equals("ttt")) {
			return "TIATracker";
		}
		try {
			return ASAPInfo.getExtDescription(originalModuleExt);
		} catch (ASAPFormatException ex) {
			return "Unknown";
		}
	}

	/**
	 * Sets {@link FileInfo#originalModuleExtDescription} of all file infos.
	 *
	 * @param fileInfos
	 *            The file infos, not <code>null</code>.
	 */
	public static void setDescriptions(List<FileInfo> fileInfos) {
		if (fileInfos == null) {
			throw new IllegalArgumentException("Parameter 'fileInfos' must not be null.");
		}
		for (FileInfo fileInfo : fileInfos) {
			fileInfo.originalModuleExtDescription = getDescription(fileInfo.originalModuleExt);
		}
	}
}
