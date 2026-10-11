package org.atari.asma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.wudsn.tools.base.repository.ValueSet;

/**
 * The channels of a file, as the website shows them: the sound chip's
 * channels, 2 for the TIA of the Atari 2600 and 4 per POKEY of the Atari 800.
 * Derived from the stored {@link FileInfo#hardware} and
 * {@link FileInfo#channels}, see {@link #of(Hardware, int)}. See plans/13.
 *
 * @author Peter Dell
 */
public final class Channels extends ValueSet {

	public static final Channels MONO_2;
	public static final Channels MONO_4;
	public static final Channels STEREO_8;

	private static final Map<String, Channels> values;
	static {
		MONO_2 = new Channels("MONO_2", 1);
		MONO_4 = new Channels("MONO_4", 2);
		STEREO_8 = new Channels("STEREO_8", 3);

		values = new TreeMap<String, Channels>();
		values.put(MONO_2.id, MONO_2);
		values.put(MONO_4.id, MONO_4);
		values.put(STEREO_8.id, STEREO_8);

		initializeClass(Channels.class, ValueSets.class);
	}

	private Channels(String id, int sortKey) {
		super(id, sortKey);
	}

	/**
	 * Gets the unmodifiable list of all values.
	 *
	 * @return The unmodifiable list of all values, not <code>null</code>.
	 */
	public static List<Channels> getValues() {
		return Collections.unmodifiableList(new ArrayList<Channels>(values.values()));
	}

	/**
	 * Gets a value by its ID.
	 *
	 * @param id
	 *            The ID, not <code>null</code>.
	 * @return The value or <code>null</code> if the ID is unknown.
	 */
	public static Channels getInstance(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Parameter 'id' must not be null.");
		}
		return values.get(id);
	}

	/**
	 * Maps the stored values to the channels, like getChannelsText() in the
	 * website's file.js.
	 *
	 * @param hardware
	 *            The hardware, not <code>null</code>.
	 * @param channels
	 *            The stored number of channels, 1 for mono, 2 for stereo.
	 * @return The channels, not <code>null</code>.
	 */
	public static Channels of(Hardware hardware, int channels) {
		if (hardware == null) {
			throw new IllegalArgumentException("Parameter 'hardware' must not be null.");
		}
		if (hardware == Hardware.ATARI2600) {
			return MONO_2;
		}
		return (channels == 1) ? MONO_4 : STEREO_8;
	}
}
