package org.atari.asma.demozoo;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.atari.asma.util.FileUtility;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ProductionList {

	private String jsonString;
	private List<Production> productionList;
	private Map<String, Production> productionByFilePath;
	private Map<Integer, Production> productionByID;

	private ProductionList(String jsonString) {
		this.jsonString = jsonString;
		productionList = new ArrayList<>();
		productionByID = new TreeMap<>();
		productionByFilePath = new TreeMap<>();
	}

	public static ProductionList load(File file) {
		String jsonString = FileUtility.readAsString(file);
		ProductionList result = new ProductionList(jsonString);

		return result;
	}

	public String getJSONString() {
		return jsonString;
	}

	public void deserialize() {
		var gson = new GsonBuilder().create();

		Type productionListType = new TypeToken<ArrayList<Production>>() {
		}.getType();
		productionList = gson.fromJson(jsonString, productionListType);
	}

	public void init() {

		for (Production production : productionList) {
			if (production.filePath != null && !production.filePath.isEmpty()) {
				Production previousProduction = productionByFilePath.put(production.filePath, production);
				if (previousProduction != null) {
					String message = "Production " + previousProduction.toString()
							+ " already registered for file path " + previousProduction.filePath + " of production "
							+ production.toString();
					System.err.println(message);
//					throw new RuntimeException(message);
				}
//				String defaultFolderName = production.getDefaultFolderName();
//				if (!production.folderName.equals(defaultFolderName)) {
//					System.err.println(production.folderName + " of " + production.toString()
//							+ " is different from default folder name " + defaultFolderName);
//
//				}
			} else {
				// System.err.println(production.toString() + " has no file path");
			}

			if (production.id != 0) {
				Production previousProduction = productionByID.put(production.id, production);
				if (previousProduction != null)
					throw new RuntimeException(
							"Production " + previousProduction.toString() + " already registered for Demozoo ID "
									+ previousProduction.id + " of production " + production.toString());
			} else {
				System.err.println(production.toString() + " has no Demozoo ID");
			}
		}
	}

	public List<Production> getEntries() {
		return productionList;
	}

	public Production getByFilePath(String folderName) {
		return productionByFilePath.get(folderName);

	}

	public Production getByID(int id) {
		return productionByID.get(id);

	}
}
