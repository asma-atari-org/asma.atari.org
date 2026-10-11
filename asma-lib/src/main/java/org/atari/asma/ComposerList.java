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

public class ComposerList {

	private String jsonString;
	private List<Composer> composerList;
	private Map<String, Composer> composerByFolderName;
	private Map<String, Composer> composerByDemozooID;

	private ComposerList(String jsonString) {
		this.jsonString = jsonString;
		composerList = new ArrayList<>();
		composerByFolderName = new TreeMap<>();
		composerByDemozooID = new TreeMap<>();
	}

	public static ComposerList load(File file) {
		String jsonString = FileUtility.readAsString(file);
		ComposerList result = new ComposerList(jsonString);

		return result;
	}

	public String getJSONString() {
		return jsonString;
	}

	public void deserialize() {
		var gson = new GsonBuilder().create();

		Type composerListType = new TypeToken<ArrayList<Composer>>() {
		}.getType();
		composerList = gson.fromJson(jsonString, composerListType);
	}

	public void init(MessageQueue messageQueue) {

		for (Composer composer : composerList) {
			if (!composer.folderName.isEmpty()) {
				Composer previousComposer = composerByFolderName.put(composer.folderName, composer);
				if (previousComposer != null)
					throw new RuntimeException("Composer \\\"" + previousComposer.toString()
							+ "\" is already registered for folder name \"" + previousComposer.folderName
							+ "\" of composer \"" + composer.toString() + "\"");
				String defaultFolderName = composer.getDefaultFolderName();
				if (!composer.folderName.equals(defaultFolderName)) {
					messageQueue.sendMessage(composer, null, Messages.E101, composer.folderName, composer.toString(),
							defaultFolderName);

				}
			} else {
				messageQueue.sendMessage(composer, null, Messages.E102, composer.toString());
			}

			if (!composer.demozooID.isEmpty()) {
				Composer previousComposer = composerByDemozooID.put(composer.demozooID, composer);
				if (previousComposer != null)
					throw new RuntimeException(
							"Composer \"" + previousComposer.toString() + "\" is already registered for Demozoo ID "
									+ previousComposer.demozooID + " of composer \"" + composer.toString() + "\"");
			} else {
				messageQueue.sendMessage(composer, null, Messages.E103, composer.toString());
			}
		}
	}

	public List<Composer> getEntries() {
		return composerList;
	}

	public Composer getByFolderName(String folderName) {
		return composerByFolderName.get(folderName);

	}

	public Composer getByDemozooID(String demozooID) {
		return composerByDemozooID.get(demozooID);

	}

	public void checkFolders(File sourceFolder, MessageQueue messageQueue) {
		if (sourceFolder == null) {
			throw new IllegalArgumentException("Parameter sourceFolder must not be null.");
		}
		File composersFolder = new File(sourceFolder, "Composers");
		for (Composer composer : composerList) {
			if (!composer.folderName.isEmpty()) {
				File composerFolder = new File(composersFolder, composer.folderName);
				if (!composerFolder.exists()) {
					messageQueue.sendMessage(composer, null, Messages.E104, composerFolder.toString(),
							composer.toString());
					continue;

				}
				if (!composerFolder.isDirectory()) {
					messageQueue.sendMessage(composer, null, Messages.E105, composerFolder.toString(),
							composer.toString());
					continue;

				}
			}
		}

		File[] composerFolders = composersFolder.listFiles();
		for (File composerFolder : composerFolders) {

			if (!composerFolder.isDirectory()) {
				messageQueue.sendMessage(this, null, Messages.E106, composerFolder.toString());
				continue;

			}
			String folderName = composerFolder.getName();
			if (getByFolderName(folderName) == null) {
				messageQueue.sendMessage(this, null, Messages.E107, composerFolder.toString());
				continue;

			}
		}
	}

	public void checkGroups(GroupList groupList, MessageQueue messageQueue) {
		for (Composer composer : composerList) {
			if (!composer.groups.isBlank()) {
				String[] groupsFolderNameArray = composer.groups.split(",");
				for (String groupFolderName : groupsFolderNameArray) {
					if (groupList.getByFolderName(groupFolderName) == null) {
						messageQueue.sendMessage(composer, null, Messages.E108, groupFolderName, composer.toString());
						continue;
					}
				}
			}
		}
	}

}
