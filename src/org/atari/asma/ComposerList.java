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

	public void init() {

		for (Composer composer : composerList) {
			if (!composer.folderName.isEmpty()) {
				Composer previousComposer = composerByFolderName.put(composer.folderName, composer);
				if (previousComposer != null)
					throw new RuntimeException(
							"Composer " + previousComposer.toString() + " already registered for folder name "
									+ previousComposer.folderName + " of composer " + composer.toString());
				String defaultFolderName = composer.getDefaultFolderName();
				if (!composer.folderName.equals(defaultFolderName)) {
					System.err.println(composer.folderName + " of " + composer.toString()
							+ " is different from default folder name " + defaultFolderName);

				}
			} else {
				System.err.println(composer.toString() + " has no folder path");
			}

			if (!composer.demozooID.isEmpty()) {
				Composer previousComposer = composerByDemozooID.put(composer.demozooID, composer);
				if (previousComposer != null)
					throw new RuntimeException(
							"Composer " + previousComposer.toString() + " already registered for Demozoo ID "
									+ previousComposer.demozooID + " of composer " + composer.toString());
			} else {
				System.err.println("Composer " + composer.toString() + " has no Demozoo ID");
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

	public void checkFolders(File sourceFolder) {
		if (sourceFolder == null) {
			throw new IllegalArgumentException("Parameter sourceFolder must not be null.");
		}
		File composersFolder = new File(sourceFolder, "Composers");
		for (Composer composer : composerList) {
			if (!composer.folderName.isEmpty()) {
				File composerFolder = new File(composersFolder, composer.folderName);
				if (!composerFolder.exists()) {
					System.err.println(
							"Folder " + composerFolder.toString() + " of " + composer.toString() + " does not exist");
					continue;

				}
				if (!composerFolder.isDirectory()) {
					System.err.println(
							"Path " + composerFolder.toString() + " of " + composer.toString() + " is not a directory");
					continue;

				}
			}
		}

		File[] composerFolders = composersFolder.listFiles();
		for (File composerFolder : composerFolders) {

			if (!composerFolder.isDirectory()) {
				System.err.println("Path " + composerFolder.toString() + "  is not a directory");
				continue;

			}
			String folderName = composerFolder.getName();
			if (getByFolderName(folderName) == null) {
				System.err.println("Folder " + composerFolder.toString() + " has not entry in composer list");
				continue;

			}
		}
	}

	public void checkGroups(GroupList groupList) {
		for (Composer composer : composerList) {
			if (!composer.groups.isBlank()) {
				String[] groupsFolderNameArray = composer.groups.split(",");
				for (String groupFolderName : groupsFolderNameArray) {
					if (groupList.getByFolderName(groupFolderName) == null) {
						System.err.println("Group folder " + groupFolderName + " of composer " + composer.toString()
								+ " has not entry in group list");
						continue;
					}
				}
			}
		}
	}

}
