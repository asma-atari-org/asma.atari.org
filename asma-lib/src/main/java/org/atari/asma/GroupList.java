package org.atari.asma;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.atari.asma.util.FileUtility;
import org.atari.asma.util.MessageQueue;

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

	public void init(MessageQueue messageQueue) {

		for (Group group : groupList) {
			if (!group.folderName.isEmpty()) {
				Group previousGroup = groupByFolderName.put(group.folderName, group);
				if (previousGroup != null)
					throw new RuntimeException(
							"Group " + previousGroup.toString() + " already registered for folder name "
									+ previousGroup.folderName + " of group " + group.toString());
			} else {
				messageQueue.sendError("GRP-001", "Group " + group.toString() + " has no folder path");
			}

			if (!group.demozooID.isEmpty()) {
				Group previousGroup = groupByDemozooID.put(group.demozooID, group);
				if (previousGroup != null)
					throw new RuntimeException(
							"Group " + previousGroup.toString() + " already registered for Demozoo ID "
									+ previousGroup.demozooID + " of group " + group.toString());
			} else {
				if (!group.handle.isEmpty()) {
					messageQueue.sendError("GRP-002", "Group " + group.toString() + " has no Demozoo ID");
				}
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

	public void checkFolders(File sourceFolder, MessageQueue messageQueue) {

		if (sourceFolder == null) {
			throw new IllegalArgumentException("Parameter 'sourceFolder' must not be null.");
		}
		if (messageQueue == null) {
			throw new IllegalArgumentException("Parameter 'messageQueue' must not be null.");
		}

		File groupsFolder = new File(sourceFolder, "Groups");
		for (Group group : groupList) {
			if (!group.folderName.isEmpty()) {
				File groupFolder = new File(groupsFolder, group.folderName);
				if (!groupFolder.exists()) {
					messageQueue.sendError("GRP-003", "Folder " + groupFolder.toString() + " of group\""
							+ group.toString() + "\" does not exist");
					continue;

				}
				if (!groupFolder.isDirectory()) {
					messageQueue.sendError("GRP-004", "Path " + groupFolder.toString() + " of group \"" + group.toString()
							+ "\" is not a directory");
					continue;

				}
			}
		}

		File[] groupFolders = groupsFolder.listFiles();
		for (File groupFolder : groupFolders) {

			if (!groupFolder.isDirectory()) {
				messageQueue.sendError("GRP-005", "Path " + groupFolder.toString() + " is not a directory");
				continue;

			}
			String folderName = groupFolder.getName();
			if (getByFolderName(folderName) == null) {
				messageQueue.sendError("GRP-006", "Folder " + groupFolder.toString() + " has no entry in the group list");
				continue;

			}
		}
	}
}
