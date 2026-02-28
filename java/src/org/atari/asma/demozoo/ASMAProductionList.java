package org.atari.asma.demozoo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.atari.asma.FileInfo;
import org.atari.asma.FileInfoList;
import org.atari.asma.demozoo.model.Production;
import org.atari.asma.util.Serializer;

public class ASMAProductionList {

	private Production[] productions;
	private List<ASMAProduction> productionList;
	private Map<String, ASMAProduction> productionByURLFilePathMap;
	private Map<Integer, ASMAProduction> productionByIDMap;

	public ASMAProductionList(Production[] productions) {
		this.productions = productions;
		productionList = new ArrayList<ASMAProduction>();
		productionByIDMap = new TreeMap<>();
		productionByURLFilePathMap = new TreeMap<>();
	}

	// Initialize list and validate all entries.
	public void init() {

		for (int i = 0; i < productions.length; i++) {
			final var production = productions[i];
			final var messageQueue = production.getMessageQueue();
			final var urlFilePaths = production.getASMAURLFilePaths();
			final var fileExtensions = production.getFileExtensions();
			if (production.download_links.length == 0) {
				messageQueue.sendWarning("DMO-001", "Music has no download link.");
			} else {
				switch (urlFilePaths.size()) {
				case 0:
					if (production.getHardware().equals("ATARI2600") && !fileExtensions.contains("ttt")) {
						messageQueue.sendInfo("DMO-007",
								"Atari 2600 is currently not supported by ASMA, except for TIA-Tracker files.");

					} else {
						messageQueue.sendError("DMO-002", "Music has no ASMA download link.");
					}
					break;

				case 1:
					if (productionByIDMap.containsKey(production.id)) {
						continue;
					}

					var urlFilePath = urlFilePaths.get(0);
					var asmaProduction = new ASMAProduction(production);
					productionList.add(asmaProduction);
					productionByIDMap.put(asmaProduction.id, asmaProduction);

					ASMAProduction previousASMAProduction = productionByURLFilePathMap.put(urlFilePath, asmaProduction);
					if (previousASMAProduction != null) {
						String message = "Production " + previousASMAProduction.toString()
								+ " already registered for file path " + previousASMAProduction.urlFilePath
								+ " of production " + asmaProduction.toString()
								+ ", Check if this is a multi-song module and adapt the URLs.";
						messageQueue.sendError("DMO-004", message);
					} else {
						productionByURLFilePathMap.put(urlFilePath, asmaProduction);

					}

					break;

				default:
					messageQueue.sendError("DMO-003", "Music has more than one ASMA download link.");
					break;
				}
			}

			if (fileExtensions.contains("sap") && !production.hasTag("sap")) {
				messageQueue.sendWarning("DMO-005", "Music has file extension \".sap\", but no tag \"sap\".");
			}
			if (production.hasTag("sap") && !fileExtensions.contains("sap")) {
				messageQueue.sendWarning("DMO-006", "Music has tag \"sap\", but not file extension \".sap\".");
			}

//				String defaultFolderName = production.getDefaultFolderName();
//				if (!production.folderName.equals(defaultFolderName)) {
//					messageQueue.sendError("PRD-004: "+production.folderName + " of " + production.toString()
//							+ " is different from default folder name " + defaultFolderName);
//
//				}

		}
	}

	public void checkReferences(FileInfoList fileInfoList) {
		Map<String, FileInfo> fileInfoMap = new TreeMap<String, FileInfo>();
		for (var fileInfo : fileInfoList.getEntries()) {
			fileInfoMap.put(fileInfo.filePath, fileInfo);
		}
		for (Production production : productions) {
			final var messageQueue = production.getMessageQueue();
			final var urlFilePaths = production.getASMAURLFilePaths();
			if (urlFilePaths.size() == 1) {
				var urlFilePath = urlFilePaths.get(0);
				var index = urlFilePath.indexOf('#');
				if (index > 0) {
					urlFilePath = urlFilePath.substring(0, index);
				}
				if (!fileInfoMap.containsKey(urlFilePath)) {
					messageQueue.sendError("DMO-006", "Music download URL contains non-existing file path \""
							+ urlFilePath + "\". Check if ASMA path has changed.");
				}
			}
		}
	}

	public List<ASMAProduction> getEntries() {
		return productionList;
	}

	public ASMAProduction getByID(int id) {
		return productionByIDMap.get(id);

	}

	public ASMAProduction getByURLFilePath(String urlFilePath) {
		return productionByURLFilePathMap.get(urlFilePath);

	}

	public String getJSONString() {
		var productionArray = productionList.toArray(new ASMAProduction[productionList.size()]);
		return Serializer.serialize(productionArray);
	}
}
