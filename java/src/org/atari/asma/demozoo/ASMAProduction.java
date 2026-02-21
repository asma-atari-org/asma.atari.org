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

	public ASMAProduction(Production production) {
		this.id = production.id;
		this.title = production.title;
		var authorIDs = new TreeSet<Integer>();
		for (var authorNick : production.author_nicks) {
			authorIDs.add(authorNick.releaser.id);
		}

		this.authorIDs = new int[authorIDs.size()];
		var it = authorIDs.iterator();
		for (int i = 0; it.hasNext(); i++) {
			this.authorIDs[i] = it.next().intValue();
		}
		// TODO
		this.urlFilePath = production.getASMADefaultURLFilePath();
	}

	public String getURL() {
		return "https://demozoo.org/music/" + id;
	}

	@Override
	public String toString() {
		return id + " - " + title + "( " + getURL() + " )";
	}
}