package org.atari.asma.demozoo;

/**
 * A reference to a Demozoo entity, with the URL the website uses for it (see
 * {@link DemozooLink}). There is one subclass per kind of entity, because IDs
 * of different kinds are never mixed. Each subclass provides
 * <code>of(int)</code> and <code>parse(String)</code>, plus the find and
 * create URLs, which are needed without an ID. Instances are immutable; a
 * missing or invalid ID is represented by <code>null</code>. See plans/10.
 *
 * @author Peter Dell
 */
public abstract sealed class DemozooID permits ScenerDemozooID, GroupDemozooID, MusicDemozooID {

	private final int id;

	/**
	 * @param id
	 *            The ID, greater than 0.
	 */
	protected DemozooID(int id) {
		if (id <= 0) {
			throw new IllegalArgumentException("Parameter 'id' must be greater than 0, but is " + id + ".");
		}
		this.id = id;
	}

	/**
	 * Parses the ID as text as in the JSON files.
	 *
	 * @param text
	 *            The text, may be <code>null</code> or empty.
	 * @return The ID, or 0 if the text is not a positive number.
	 */
	protected static int parseID(String text) {
		if (text == null) {
			return 0;
		}
		try {
			return Math.max(0, Integer.parseInt(text.trim()));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	public final int getID() {
		return id;
	}

	/**
	 * The ID as a sortable table value.
	 *
	 * @param demozooID
	 *            The ID or <code>null</code>.
	 * @return The ID, or <code>null</code> if there is none.
	 */
	public static Integer toInteger(DemozooID demozooID) {
		return (demozooID == null) ? null : Integer.valueOf(demozooID.id);
	}

	/** The URL of the entity's page on Demozoo, like the website's links. */
	public abstract String getURL();

	@Override
	public final boolean equals(Object object) {
		return object != null && object.getClass() == getClass() && ((DemozooID) object).id == id;
	}

	@Override
	public final int hashCode() {
		return getClass().hashCode() * 31 + id;
	}

	/** The plain ID, without digit grouping. */
	@Override
	public final String toString() {
		return String.valueOf(id);
	}
}
