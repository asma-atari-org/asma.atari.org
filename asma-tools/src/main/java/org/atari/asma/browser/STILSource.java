package org.atari.asma.browser;

/**
 * Stub for the STIL information ("SAP Tune Information List",
 * asma/Docs/STIL.txt) shown in the details panels. Reading and parsing STIL
 * will later be part of ASAP, ideally written in Fusion so that it can be
 * reused; until then only {@link #NONE} exists. See plans/09, section 5.
 *
 * @author Peter Dell
 */
public interface STILSource {

	/** The source without any STIL information. */
	public static final STILSource NONE = path -> "";

	/**
	 * @param path
	 *            The STIL path, e.g. "/Composers/Aki/" for a folder or
	 *            "/Composers/Aki/Robots.sap" for a file, not <code>null</code>.
	 * @return The comment of the entry, empty if there is none, not
	 *         <code>null</code>.
	 */
	public String getComment(String path);
}
