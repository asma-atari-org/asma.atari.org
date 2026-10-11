package org.atari.asma;

import org.atari.asma.demozoo.GroupDemozooID;

import com.wudsn.tools.base.repository.Attribute;

public class Group {

	/**
	 * The filter values of the group list, matched case-insensitively as
	 * "contains", like {@link Composer.Selection}. Empty or <code>null</code>
	 * values match everything. The keyword matches the name and the folder
	 * name. See plans/09.
	 */
	public static record Selection(String keyword, String name) {

		public static final Selection ALL = new Selection("", "");

		public Selection {
			keyword = normalize(keyword);
			name = normalize(name);
		}

		private static String normalize(String value) {
			return (value == null) ? "" : value.toLowerCase();
		}

		private static boolean contains(String value, String part) {
			return value != null && value.toLowerCase().contains(part);
		}

		public boolean matches(Group group) {
			if (group == null) {
				throw new IllegalArgumentException("Parameter 'group' must not be null.");
			}
			if (!keyword.isEmpty() && !(contains(group.name, keyword) || contains(group.folderName, keyword))) {
				return false;
			}
			if (!name.isEmpty() && !contains(group.name, name)) {
				return false;
			}
			return true;
		}
	}

	/**
	 * The attributes of a group, bound to the gson field names, for use with
	 * WUDSN Base's attribute tables. COMPOSERS and FILE_COUNT are derived from
	 * the composers and the file infos, not fields. See plans/09.
	 */
	public static final class Attributes {
		public static final Attribute FOLDER_NAME = new Attribute("folderName", DataTypes.Group_FolderName);
		public static final Attribute NAME = new Attribute("name", DataTypes.Group_Name);
		public static final Attribute DEMOZOO_ID = new Attribute("demozooID", DataTypes.Group_DemozooID);
		public static final Attribute COMPOSERS = new Attribute("composers", DataTypes.Group_Composers);
		public static final Attribute FILE_COUNT = new Attribute("fileCount", DataTypes.Group_FileCount);

		private Attributes() {
		}
	}

	public String folderName = "";
	// The JSON attribute in "Docs/ASMA-Groups.json" is "name".
	public String name = "";
	public String demozooID = "";

	/** The Demozoo ID, or <code>null</code> if there is none. See plans/10. */
	public GroupDemozooID getDemozooID() {
		return GroupDemozooID.parse(demozooID);
	}

	@Override
	public String toString() {
		if (!folderName.isEmpty()) {
			return folderName;
		}
		return name;
	}
}
