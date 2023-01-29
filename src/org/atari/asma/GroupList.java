package org.atari.asma;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.atari.asma.util.FileUtility;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class GroupList {
	private String jsonString;
	private List<Group> groupList;
	private Map<String, Group> groupByFolderName;
	private Map<String, Group> groupByDemozooID;

	private GroupList(String jsonString) {
		this.jsonString = jsonString;
		groupList = new ArrayList<>();
		groupByFolderName = new TreeMap<>();
		groupByDemozooID = new TreeMap<>();
	}

	public String getJSONString() {
		return jsonString;
	}

	public static GroupList load(File file) {

		String jsonString = FileUtility.readAsString(file);
		GroupList result = new GroupList(jsonString);
		return result;
	}

	public void deserialize() {
		var gson = new GsonBuilder().create();

		Type groupListType = new TypeToken<ArrayList<Group>>() {
		}.getType();

		groupList = gson.fromJson(jsonString, groupListType);
	}
	
	public void init() {

		for (Group group : groupList) {
			if (!group.folderName.isEmpty()) {
				Group previousGroup = groupByFolderName.put(group.folderName, group);
				if (previousGroup != null)
					throw new RuntimeException(
							"Group " + previousGroup.toString() + " already registered for folder name "
									+ previousGroup.folderName + " of group " + group.toString());
			} else {
				System.err.println(group.toString() + " has no folder path");
			}

			if (!group.demozooID.isEmpty()) {
				Group previousGroup = groupByDemozooID.put(group.demozooID, group);
				if (previousGroup != null)
					throw new RuntimeException(
							"Group " + previousGroup.toString() + " already registered for Demozoo ID "
									+ previousGroup.demozooID + " of group " + group.toString());
			} else {
				System.err.println("Group "+group.toString() + " has no Demozoo ID");
			}
		}
	}

	public List<Group> getEntries() {
		return groupList;
	}

	public Group getByFolderName(String folderName) {
		return groupByFolderName.get(folderName);

	}

	public Group getByDemozooID(String demozooID) {
		return groupByDemozooID.get(demozooID);

	}

	public void checkFolders(File sourceFolder) {

		if (sourceFolder == null) {
			throw new IllegalArgumentException("Parameter sourceFolder must not be null.");
		}
		File groupsFolder = new File(sourceFolder, "Groups");
		for (Group group : groupList) {
			if (!group.folderName.isEmpty()) {
				File groupFolder = new File(groupsFolder, group.folderName);
				if (!groupFolder.exists()) {
					System.err.println(
							"Folder " + groupFolder.toString() + " of " + group.toString() + " does not exist");
					continue;

				}
				if (!groupFolder.isDirectory()) {
					System.err.println(
							"Path " + groupFolder.toString() + " of " + group.toString() + " is not a directory");
					continue;

				}
			}
		}

		File[] groupFolders = groupsFolder.listFiles();
		for (File groupFolder : groupFolders) {

			if (!groupFolder.isDirectory()) {
				System.err.println("Path " + groupFolder.toString() + "  is not a directory");
				continue;

			}
			String folderName = groupFolder.getName();
			if (getByFolderName(folderName) == null) {
				System.err.println("Folder " + groupFolder.toString() + " has not entry in group list");
				continue;

			}
		}
	}
}
