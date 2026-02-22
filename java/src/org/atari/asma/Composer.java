package org.atari.asma;

import java.util.ArrayList;
import java.util.List;

public class Composer {

	public String folderName = "";
	public String lastName = "";
	public String firstName = "";
	public String lastnNameUnicode = "";
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