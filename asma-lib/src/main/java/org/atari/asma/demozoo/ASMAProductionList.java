package org.atari.asma.demozoo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.atari.asma.FileInfo;
import org.atari.asma.FileInfoList;
import org.atari.asma.demozoo.model.Production;
import org.atari.asma.util.JSONSerializer;

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
			final var fileExtensions = production.getFileExtensionsSet();
			final var tags=production.getTagsSet();
			if (production.download_links.length == 0) {
				messageQueue.sendMessage(production, null, Messages.I001);
			} else {
				switch (urlFilePaths.size()) {
				case 0:
					if (production.getHardware().equals("ATARI2600") && !fileExtensions.contains("ttt")) {
						messageQueue.sendMessage(production, null, Messages.I007);

					} else {
						if (fileExtensions.contains("cmc") || fileExtensions.contains("cms")
								|| fileExtensions.contains("rmt") || fileExtensions.contains("sap")) {
							messageQueue.sendMessage(production, null, Messages.E008);
						} else {
							messageQueue.sendMessage(production, null, Messages.W002);
						}
						
						
						if (fileExtensions.contains("neo") && !tags.contains("neo-tracker")) {
							messageQueue.sendMessage(production, null, Messages.W009);
	
						}
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
						messageQueue.sendMessage(production, null, Messages.E004, previousASMAProduction.toString(),
								previousASMAProduction.urlFilePath, asmaProduction.toString());
					} else {
						productionByURLFilePathMap.put(urlFilePath, asmaProduction);

					}

					break;

				default:
					messageQueue.sendMessage(production, null, Messages.E003);
					break;
				}
			}

			if (fileExtensions.contains("sap") && !production.hasTag("sap")) {
				messageQueue.sendMessage(production, null, Messages.W005);
			}
			if (production.hasTag("sap") && !fileExtensions.contains("sap")) {
				messageQueue.sendMessage(production, null, Messages.W006);
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
					messageQueue.sendMessage(production, null, Messages.E010, urlFilePath);
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
		return JSONSerializer.serialize(productionArray);
	}
}
