package org.atari.asma.demozoo;

import java.util.TreeSet;

import org.atari.asma.demozoo.model.Production;

/**
 * Reduced version of the Demozoo {@link Production} for usage within ASMA.
 *
 * @author Peter Dell
 *
 */
public final class ASMAProduction {

	public final int id;
	public final String title;
	public final int[] authorIDs;
	public final String urlFilePath;

	// For gson, which sets the fields afterwards. Without it, gson needs
	// sun.misc.Unsafe, which is missing when running on the module path, e.g.
	// from Eclipse.
	private ASMAProduction() {
		this(0, "", new int[0], "");
	}

	public ASMAProduction(Production production) {
		// TODO
		this(production.id, production.title, getAuthorIDs(production), production.getASMADefaultURLFilePath());
	}

	private ASMAProduction(int id, String title, int[] authorIDs, String urlFilePath) {
		this.id = id;
		this.title = title;
		this.authorIDs = authorIDs;
		this.urlFilePath = urlFilePath;
	}

	// The distinct IDs of the production's authors, sorted.
	private static int[] getAuthorIDs(Production production) {
		var authorIDs = new TreeSet<Integer>();
		for (var authorNick : production.author_nicks) {
			authorIDs.add(authorNick.releaser.id);
		}

		var result = new int[authorIDs.size()];
		var it = authorIDs.iterator();
		for (int i = 0; it.hasNext(); i++) {
			result[i] = it.next().intValue();
		}
		return result;
	}

	public String getURL() {
		return "https://demozoo.org/music/" + id;
	}

	@Override
	public String toString() {
		return id + " - " + title + " (" + getURL() + ")";
	}
}
