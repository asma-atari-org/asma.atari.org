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
import com.wudsn.tools.base.common.MessageQueue;

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
				messageQueue.sendMessage(group, null, Messages.E201, group.toString());
			}

			if (!group.demozooID.isEmpty()) {
				Group previousGroup = groupByDemozooID.put(group.demozooID, group);
				if (previousGroup != null)
					throw new RuntimeException(
							"Group " + previousGroup.toString() + " already registered for Demozoo ID "
									+ previousGroup.demozooID + " of group " + group.toString());
			} else {
				if (!group.name.isEmpty()) {
					messageQueue.sendMessage(group, null, Messages.E202, group.toString());
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
					messageQueue.sendMessage(group, null, Messages.E203, groupFolder.toString(), group.toString());
					continue;

				}
				if (!groupFolder.isDirectory()) {
					messageQueue.sendMessage(group, null, Messages.E204, groupFolder.toString(), group.toString());
					continue;

				}
			}
		}

		File[] groupFolders = groupsFolder.listFiles();
		for (File groupFolder : groupFolders) {

			if (!groupFolder.isDirectory()) {
				messageQueue.sendMessage(this, null, Messages.E205, groupFolder.toString());
				continue;

			}
			String folderName = groupFolder.getName();
			if (getByFolderName(folderName) == null) {
				messageQueue.sendMessage(this, null, Messages.E206, groupFolder.toString());
				continue;

			}
		}
	}
}
