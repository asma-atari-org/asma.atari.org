package org.atari.asma.demozoo;

/**
 * The Demozoo ID of a scener, i.e. a composer. See plans/10.
 *
 * @author Peter Dell
 */
public final class ScenerDemozooID extends DemozooID {

	private ScenerDemozooID(int id) {
		super(id);
	}

	/**
	 * @param id
	 *            The ID, 0 or negative for none.
	 * @return The ID, or <code>null</code> if there is none.
	 */
	public static ScenerDemozooID of(int id) {
		return (id > 0) ? new ScenerDemozooID(id) : null;
	}

	/**
	 * @param text
	 *            The ID as text as in the JSON files, may be
	 *            <code>null</code> or empty.
	 * @return The ID, or <code>null</code> if the text is not a positive
	 *         number.
	 */
	public static ScenerDemozooID parse(String text) {
		return of(parseID(text));
	}

	/** The URL to search for the given text among the sceners, not <code>null</code>. */
	public static String getFindURL(String query) {
		if (query == null) {
			throw new IllegalArgumentException("Parameter 'query' must not be null.");
		}
		return DemozooLink.getFind(query, "scener");
	}

	/** The URL of Demozoo's form to create a scener. */
	public static String getCreateURL() {
		return "https://demozoo.org/sceners/new/";
	}

	@Override
	public String getURL() {
		return DemozooLink.getScener(getID());
	}
}
