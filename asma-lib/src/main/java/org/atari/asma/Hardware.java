package org.atari.asma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.wudsn.tools.base.repository.ValueSet;

/**
 * The hardware a file plays on. The IDs are the values stored in
 * {@link FileInfo#hardware} and in the JSON files. See plans/13.
 *
 * @author Peter Dell
 */
public final class Hardware extends ValueSet {

	public static final Hardware ATARI800;
	public static final Hardware ATARI2600;

	private static final Map<String, Hardware> values;
	static {
		ATARI800 = new Hardware(FileInfo.ATARI800, 1);
		ATARI2600 = new Hardware(FileInfo.ATARI2600, 2);

		values = new TreeMap<String, Hardware>();
		values.put(ATARI800.id, ATARI800);
		values.put(ATARI2600.id, ATARI2600);

		initializeClass(Hardware.class, ValueSets.class);
	}

	private Hardware(String id, int sortKey) {
		super(id, sortKey);
	}

	/**
	 * Gets the unmodifiable list of all values.
	 *
	 * @return The unmodifiable list of all values, not <code>null</code>.
	 */
	public static List<Hardware> getValues() {
		return Collections.unmodifiableList(new ArrayList<Hardware>(values.values()));
	}

	/**
	 * Gets a value by its ID, the stored value.
	 *
	 * @param id
	 *            The ID; <code>null</code> or empty means {@link #ATARI800},
	 *            the default the JSON files omit.
	 * @return The value or <code>null</code> if the ID is unknown.
	 */
	public static Hardware getInstance(String id) {
		if (id == null || id.isEmpty()) {
			return ATARI800;
		}
		return values.get(id);
	}
}
