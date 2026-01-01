package org.atari.asma.demozoo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.atari.asma.demozoo.model.Production;
import org.atari.asma.util.MessageQueue;
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

		for (Production production : productions) {
			final var messageQueue = production.getMessageQueue();
			final var urlFilePaths = production.getASMAURLFilePaths();
			final var fileExtensions = production.getFileExtensions();
			if (production.download_links.length == 0) {
				messageQueue.sendWarning("DMO-001 - Music has no download link.");
			} else {
				switch (urlFilePaths.size()) {
				case 0:
					if (production.getHardware().equals("ATARI2600") && !fileExtensions.contains("ttt")) {
						messageQueue.sendInfo("DMO-004 - Atari 2600 is currently not supported by ASMA.");

					} else {
						messageQueue.sendError("DMO-002 - Music has no ASMA download link.");
					}
					break;

				case 1:
					var urlFilePath = urlFilePaths.get(0);
					var asmaProduction = new ASMAProduction(production);
					productionList.add(asmaProduction);
					productionByIDMap.put(asmaProduction.id, asmaProduction);

					ASMAProduction previousProduction = productionByURLFilePathMap.put(urlFilePath, asmaProduction);
					if (previousProduction != null) {
						String message = "DMO-004: Production " + previousProduction.toString()
								+ " already registered for file path " + previousProduction.urlFilePath
								+ " of production " + asmaProduction.toString();
						messageQueue.sendError(message);
//						throw new RuntimeException(message);
					} else {
						productionByURLFilePathMap.put(urlFilePath, asmaProduction);

					}
					break;

				default:
					messageQueue.sendError("DMO-003 - Music has more than one ASMA download link.");
					break;
				}
			}

			if (fileExtensions.contains("sap") && !production.hasTag("sap")) {
				messageQueue.sendWarning("DMO-005 - Music has file extension \".sap\", but no tag \"sap\".");
			}
			if (production.hasTag("sap") && !fileExtensions.contains("sap")) {
				messageQueue.sendWarning("DMO-006 - Music has tag \"sap\", but not file extension \".sap\".");
			}


//				String defaultFolderName = production.getDefaultFolderName();
//				if (!production.folderName.equals(defaultFolderName)) {
//					messageQueue.sendError("PRD-004: "+production.folderName + " of " + production.toString()
//							+ " is different from default folder name " + defaultFolderName);
//
//				}
	
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
