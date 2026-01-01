package org.atari.asma.demozoo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.atari.asma.demozoo.RestClient.HttpRequest;
import org.atari.asma.demozoo.RestClient.HttpRequest.ParameterValue;
import org.atari.asma.demozoo.model.Database;
import org.atari.asma.demozoo.model.Production;
import org.atari.asma.demozoo.model.ProductionsPage;
import org.atari.asma.util.FileUtility;
import org.atari.asma.util.MemoryUtility;
import org.atari.asma.util.MessageQueue;
import org.atari.asma.util.Serializer;
import org.atari.asma.util.StringUtility;

public class Demozoo {

	public class PlatformDefinition {
		public static final int ATARI_800 = 16;
		public static final int ATARI_2600 = 54;
	};

	private static class ProductionsFetcher {

		private final static int MAX_PAGES = 1000;

		private MessageQueue messageQueue;
		private long startTimeMillis;
		private List<Production> productionList;

		public ProductionsFetcher(MessageQueue messageQueue) {
			this.messageQueue = messageQueue;
		}

		private boolean fetchProduction(ProductionsPage productionsPage, int resultIndex) {

			var productionsPageResult = productionsPage.results[resultIndex];

			var now = System.currentTimeMillis();
			var milliSecondsSinceStart = now - startTimeMillis;
			var productionNumber = productionList.size() + 1;
			var milliSecondsToGo = (milliSecondsSinceStart / (productionNumber)
					* (productionsPage.count - productionNumber));

			if (productionNumber % 10 == 0) {
				messageQueue.sendInfo("Fetching production " + (productionNumber) + " of " + productionsPage.count
						+ " from " + productionsPageResult.url + " ("
						+ StringUtility.getDurationString(milliSecondsSinceStart) + " until now, "
						+ StringUtility.getDurationString(milliSecondsToGo) + " to go)");
			}
			try {
				var response = RestClient.sendGetRequest(productionsPageResult.url);

				if (response.isSuccess()) {
					Production production = Serializer.deserialize(response.content, Production.class);
					productionList.add(production);
					return true;
				} else {
					messageQueue.sendError("Fetch error for '" + productionsPageResult.url + "': " + response.status
							+ " - " + response.content);
					return false;
				}

			} catch (IOException ex) {
				messageQueue.sendError("Fetch error for '" + productionsPageResult.url + "': " + ex.getMessage());
				return false;
			}

		}

		private ProductionsPage fetchPage(String url, List<Production> productionList) {
			try {
				var response = RestClient.sendGetRequest(url);
				ProductionsPage productionsPage = Serializer.deserialize(response.content, ProductionsPage.class);
				for (int i = 0; i < productionsPage.results.length; i++) {
					if (!fetchProduction(productionsPage, i)) {
						return null;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException ignore) {

					}
				}
				return productionsPage;
			} catch (IOException ex) {
				messageQueue.sendError(ex.getMessage());
				return null;
			}
		}

		public List<Production> fetchProductions(String url) {
			int pageNumber = 0;
			startTimeMillis = System.currentTimeMillis();
			productionList = new ArrayList<Production>();

			do {
				pageNumber++;
				if (pageNumber <= MAX_PAGES) {
					messageQueue.sendInfo("Fetching page " + pageNumber + " from " + url);

					var page = this.fetchPage(url, productionList);
					if (page != null) {
						url = page.next;
					}
				} else {
					url = null;
				}
			} while (url != null && !url.isEmpty());
			return productionList;
		}

	}

	private static void fetchProductions(Database database, MessageQueue messageQueue) {
		String baseUrlString = "https://demozoo.org/api/v1/productions";
		var parameters = new ArrayList<ParameterValue>();
		parameters.add(new ParameterValue("format", "json"));
		parameters.add(new ParameterValue("supertype", "music"));
		parameters.add(new ParameterValue("platform", PlatformDefinition.ATARI_800));
		parameters.add(new ParameterValue("platform", PlatformDefinition.ATARI_2600));

		var fetcher = new ProductionsFetcher(messageQueue);
		var productionList = fetcher.fetchProductions(baseUrlString + "?" + HttpRequest.getParmetersString(parameters));
		database.productions = productionList.toArray(new Production[productionList.size()]);
	}

	public void importDatabase(File outputFile, MessageQueue messageQueue) {

		var database = new Database();
		database.updateDateTime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
		fetchProductions(database, messageQueue);

		messageQueue.sendInfo("Saving to '" + outputFile.getAbsolutePath() + "'.");
		try {
			var content = Serializer.serialize(database);
			var writer = new FileWriter(outputFile, StandardCharsets.UTF_8);
			writer.write(content);
			writer.close();
			messageQueue.sendInfo("Demozoo database file written with "
					+ MemoryUtility.getRoundedMemorySize(outputFile.length()) + ".");

		} catch (IOException ex) {
			messageQueue.sendError(ex.getMessage());
		}
	}

	public Database loadDatabase(File inputFile, MessageQueue messageQueue) {

		var content = FileUtility.readAsString(inputFile);
		var database = Serializer.deserialize(content, Database.class);

		return database;
	}

}
