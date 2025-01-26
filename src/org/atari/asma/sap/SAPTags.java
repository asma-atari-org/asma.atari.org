package org.atari.asma.sap;

import java.util.ArrayList;
import java.util.List;

class SAPTags {

	public static final String AUTHOR = "AUTHOR";
	public static final String NAME = "NAME";
	public static final String DATE = "DATE";
	public static final String SONGS = "SONGS";
	public static final String DEFSONG = "DEFSONG";
	public static final String STEREO = "STEREO";
	public static final String NTSC = "NTSC";
	public static final String TYPE = "TYPE";
	public static final String FASTPLAY = "FASTPLAY";
	public static final String INIT = "INIT";
	public static final String MUSIC = "MUSIC";
	public static final String PLAYER = "PLAYER";
	public static final String COVOX = "COVOX";
	public static final String TIME = "TIME";

	public static List<String> getTags() {
		List<String> result = new ArrayList<String>();

		result.add(AUTHOR);
		result.add(NAME);
		result.add(DATE);
		result.add(SONGS);
		result.add(DEFSONG);
		result.add(STEREO);
		result.add(NTSC);
		result.add(TYPE);
		result.add(FASTPLAY);
		result.add(INIT);
		result.add(MUSIC);
		result.add(PLAYER);
		result.add(COVOX);
		result.add(TIME);
		return result;
	}

}