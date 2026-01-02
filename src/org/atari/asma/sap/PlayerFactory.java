package org.atari.asma.sap;

import java.util.*;

public class PlayerFactory {

	private List<Player> players;

	public PlayerFactory() {
		players = new ArrayList<Player>();
		players.add(new RMT128MonoPlayer());
		players.add(new RMT128StereoPlayer());
	}

	public List<Player> getMatchingPlayers(SegmentList segmentList) {
		var result = new ArrayList<Player>();
		for (var player : players) {
			if (player.matches(segmentList)) {
				result.add(player);
			}
		}
		return result;
	}
}
