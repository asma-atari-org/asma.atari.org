package org.atari.asma.demozoo;

/**
 * A reference to a Demozoo entity: a scener (composer), a group or a music
 * production, with the URLs the website uses for it (see
 * {@link DemozooLink}). Instances are immutable; a missing or invalid ID is
 * represented by <code>null</code>, see {@link #parse(Kind, String)}. See
 * plans/09.
 *
 * @author Peter Dell
 */
public final class DemozooID {

	/** The kind of Demozoo entity, with its URL and search category. */
	public enum Kind {
		SCENER("scener"), GROUP("group"), MUSIC("music");

		private final String category;

		private Kind(String category) {
			this.category = category;
		}

		/** The Demozoo search category, e.g. "scener". */
		public String getCategory() {
			return category;
		}

		/** The URL to search for the given text in this kind's category, not <code>null</code>. */
		public String getFindURL(String query) {
			if (query == null) {
				throw new IllegalArgumentException("Parameter 'query' must not be null.");
			}
			return DemozooLink.getFind(query, category);
		}

		/** The URL of Demozoo's form to create an entity of this kind, not <code>null</code>. */
		public String getCreateURL() {
			switch (this) {
			case SCENER:
				return "https://demozoo.org/sceners/new/";
			case GROUP:
				return "https://demozoo.org/groups/new/";
			case MUSIC:
				return "https://demozoo.org/music/new/";
			default:
				throw new IllegalStateException("Unknown kind " + this + ".");
			}
		}
	}

	private final Kind kind;
	private final int id;

	private DemozooID(Kind kind, int id) {
		this.kind = kind;
		this.id = id;
	}

	/**
	 * @param kind
	 *            The kind, not <code>null</code>.
	 * @param id
	 *            The ID, 0 or negative for none.
	 * @return The reference, or <code>null</code> if there is no ID.
	 */
	public static DemozooID of(Kind kind, int id) {
		if (kind == null) {
			throw new IllegalArgumentException("Parameter 'kind' must not be null.");
		}
		return (id > 0) ? new DemozooID(kind, id) : null;
	}

	/**
	 * @param kind
	 *            The kind, not <code>null</code>.
	 * @param text
	 *            The ID as text as in the JSON files, may be
	 *            <code>null</code> or empty.
	 * @return The reference, or <code>null</code> if the text is not a
	 *         positive number.
	 */
	public static DemozooID parse(Kind kind, String text) {
		if (kind == null) {
			throw new IllegalArgumentException("Parameter 'kind' must not be null.");
		}
		if (text == null) {
			return null;
		}
		try {
			return of(kind, Integer.parseInt(text.trim()));
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	public Kind getKind() {
		return kind;
	}

	public int getID() {
		return id;
	}

	/** The URL of the entity's page on Demozoo, like the website's links. */
	public String getURL() {
		switch (kind) {
		case SCENER:
			return DemozooLink.getScener(id);
		case GROUP:
			return DemozooLink.getGroup(id);
		case MUSIC:
			return DemozooLink.getMusic(id);
		default:
			throw new IllegalStateException("Unknown kind " + kind + ".");
		}
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof DemozooID other && other.kind == kind && other.id == id;
	}

	@Override
	public int hashCode() {
		return kind.hashCode() * 31 + id;
	}

	/** The plain ID, without digit grouping. */
	@Override
	public String toString() {
		return String.valueOf(id);
	}
}
