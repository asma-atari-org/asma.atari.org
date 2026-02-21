package org.atari.asma.sap.player;

import java.util.*;

import org.atari.asma.sap.SegmentList;

public class PlayerFactory {

	private List<Player> players;

	public PlayerFactory() {
		players = new ArrayList<Player>();
		players.add(new RMT128MonoPlayer());
		players.add(new RMT128StereoPlayer());
		players.add(new RMT134PlayerV1());

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
