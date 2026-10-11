package org.atari.asma.demozoo;

/**
 * The Demozoo ID of a music production. See plans/10.
 *
 * @author Peter Dell
 */
public final class MusicDemozooID extends DemozooID {

	private MusicDemozooID(int id) {
		super(id);
	}

	/**
	 * @param id
	 *            The ID, 0 or negative for none.
	 * @return The ID, or <code>null</code> if there is none.
	 */
	public static MusicDemozooID of(int id) {
		return (id > 0) ? new MusicDemozooID(id) : null;
	}

	/**
	 * @param text
	 *            The ID as text as in the JSON files, may be
	 *            <code>null</code> or empty.
	 * @return The ID, or <code>null</code> if the text is not a positive
	 *         number.
	 */
	public static MusicDemozooID parse(String text) {
		return of(parseID(text));
	}

	/** The URL to search for the given text among the music productions, not <code>null</code>. */
	public static String getFindURL(String query) {
		if (query == null) {
			throw new IllegalArgumentException("Parameter 'query' must not be null.");
		}
		return DemozooLink.getFind(query, "music");
	}

	/** The URL of Demozoo's form to create a music production. */
	public static String getCreateURL() {
		return "https://demozoo.org/music/new/";
	}

	@Override
	public String getURL() {
		return DemozooLink.getMusic(getID());
	}
}
