package org.atari.asma;

import java.util.ArrayList;
import java.util.List;

import com.wudsn.tools.base.repository.Attribute;

public class Composer {

	/**
	 * The attributes of a composer, bound to the gson field names, for use
	 * with WUDSN Base's attribute tables. FILE_COUNT is derived from the file
	 * infos, not a field. See plans/07.
	 */
	public static final class Attributes {
		public static final Attribute FOLDER_NAME = new Attribute("folderName", DataTypes.Composer_FolderName);
		public static final Attribute LAST_NAME = new Attribute("lastName", DataTypes.Composer_LastName);
		public static final Attribute FIRST_NAME = new Attribute("firstName", DataTypes.Composer_FirstName);
		public static final Attribute HANDLES = new Attribute("handles", DataTypes.Composer_Handles);
		public static final Attribute COUNTRY = new Attribute("country", DataTypes.Composer_Country);
		public static final Attribute DEMOZOO_ID = new Attribute("demozooID", DataTypes.Composer_DemozooID);
		public static final Attribute GROUPS = new Attribute("groups", DataTypes.Composer_Groups);
		public static final Attribute FILE_COUNT = new Attribute("fileCount", DataTypes.Composer_FileCount);

		private Attributes() {
		}
	}

	public String folderName = "";
	public String lastName = "";
	public String firstName = "";
	public String lastNameUnicode = "";
	public String firstNameUnicode = "";
	public String handles = "";
	public String country = "";
	public String demozooID = "";
	public String groups = "";

	@Override
	public String toString() {
		if (!folderName.isEmpty()) {
			return folderName;
		}
		return firstName + " " + lastName + " (" + handles + ")";
	}

	public String[] getHandlesArray() {
		String[] result = handles.split(",");
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i].trim();
		}
		return result;
	}

	public String getDefaultFolderName() {
		if (lastName.equals("<?>")) {
			return normalize(getHandlesArray()[0]);
		}
		return normalize(lastName) + "_" + normalize(firstName);
	}

	private static String normalize(String name) {
		name = name.replaceAll(" ", "_");
		name = name.replaceAll("-", "_");
		name = name.replaceAll("\\.", "");
		return name;
	}

	// TODO: Return all possible combinations using no or one handle
	public List<String> getAuthors() {
		List<String> result = new ArrayList<String>();
		var handles = this.handles.split(",");
		if (handles.length == 0) {
			result.add(getAuthor(""));
		} else {
			for (String handle : handles) {
				result.add(getAuthor(handle.trim()));
			}
		}
		return result;
	}

	private String getAuthor(String handle) {
		if (handle.equals("Mr. Holub")) {
			handle="Mr. Holub";
		}
		StringBuilder result = new StringBuilder();
		if (!firstName.isEmpty()) {
			result.append(firstName);
			result.append(" ");
		}
		result.append(lastName);
		if (!handle.isEmpty()) {
			if (!result.isEmpty()) {
				result.append(" ");
			}
			result.append("(").append(handle).append(")");

		}
		return result.toString();
	}
}