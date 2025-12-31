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
		productionList=new ArrayList<ASMAProduction>();
		productionByIDMap = new TreeMap<>();
		productionByURLFilePathMap = new TreeMap<>();
	}

	public void init(MessageQueue messageQueue) {

		for (Production production : productions) {
			final var urlFilePath = production.getURLFilePath();
			if (!urlFilePath.isEmpty()) {
				var asmaProduction = new ASMAProduction(production);
				productionList.add(asmaProduction);
				
				ASMAProduction previousProduction = productionByURLFilePathMap.put(urlFilePath, asmaProduction);
				if (previousProduction != null) {
					String message = "PRD-001: Production " + previousProduction.toString()
							+ " already registered for file path " + previousProduction.urlFilePath + " of production "
							+ production.toString();
					messageQueue.sendError(message);
//					throw new RuntimeException(message);
				}
//				String defaultFolderName = production.getDefaultFolderName();
//				if (!production.folderName.equals(defaultFolderName)) {
//					messageQueue.sendError("PRD-004: "+production.folderName + " of " + production.toString()
//							+ " is different from default folder name " + defaultFolderName);
//
//				}
			} else {
				// messageQueue.sendError("PRD-003: "+production.toString() + " has no file
				// path");
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
