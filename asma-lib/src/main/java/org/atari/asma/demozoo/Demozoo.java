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
import org.atari.asma.util.JSONSerializer;
import org.atari.asma.util.MemoryUtility;
import org.atari.asma.util.StringUtility;

import com.wudsn.tools.base.common.MessageQueue;

public class Demozoo {

	public class PlatformDefinition {
		public static final int ATARI_800 = 16;
		public static final int ATARI_2600 = 54;
	};


	private static class ProductionsFetcher {
		private final int DELAY = 400;

		private final static int MAX_PAGES = 1000;

		private MessageQueue messageQueue;
		private long startTimeMillis;
		private List<Production> productionList;

		public ProductionsFetcher(MessageQueue messageQueue) {
			this.messageQueue = messageQueue;
		}

		private static void normalizeProduction(Production production) {

			// Rewrite URLs to actual download URLs
			final var PREFIX = "https://files.scene.org/view/";
			final var REPLACEMENT = "https://files.scene.org/get/";

			for (var link : production.download_links) {
				if (link.url.startsWith(PREFIX)) {
					link.url = REPLACEMENT + link.url.substring(PREFIX.length());
				}
			}

		}

		private Production fetchProduction(ProductionsPage productionsPage, int resultIndex) {

			var productionsPageResult = productionsPage.results[resultIndex];

			var now = System.currentTimeMillis();
			var milliSecondsSinceStart = now - startTimeMillis;
			var productionNumber = productionList.size() + 1;
			var milliSecondsToGo = (milliSecondsSinceStart / (productionNumber)
					* (productionsPage.count - productionNumber));

			if (productionNumber % 10 == 0) {
				messageQueue.sendMessage(this, null, Messages.I109, String.valueOf(productionNumber),
						String.valueOf(productionsPage.count), productionsPageResult.url,
						StringUtility.getDurationString(milliSecondsSinceStart),
						StringUtility.getDurationString(milliSecondsToGo));
			}
			try {
				var response = RestClient.sendGetRequest(productionsPageResult.url);

				if (response.isSuccess()) {
					Production production = JSONSerializer.deserialize(response.content, Production.class);
					productionList.add(production);
					return production;
				} else {
					messageQueue.sendMessage(this, null, Messages.E110, productionsPageResult.url,
							String.valueOf(response.status), response.content);
					return null;
				}

			} catch (IOException ex) {
				messageQueue.sendMessage(this, null, Messages.E111, productionsPageResult.url, ex.getMessage());
				return null;
			}

		}

		private ProductionsPage fetchPage(String url, List<Production> productionList) {
			try {
				var response = RestClient.sendGetRequest(url);
				ProductionsPage productionsPage = JSONSerializer.deserialize(response.content, ProductionsPage.class);
				for (int i = 0; i < productionsPage.results.length; i++) {
					var production = fetchProduction(productionsPage, i);
					if (production == null) {
						return null;
					}
					var startTime = System.currentTimeMillis();
					normalizeProduction(production);
					production.fileExtensions = LinkUtility.getFileExtensions(production.download_links, messageQueue);

					var duration = System.currentTimeMillis() - startTime;
					if (duration < DELAY) {
						try {
							Thread.sleep(DELAY - duration);
						} catch (InterruptedException ignore) {

						}
					}
				}
				return productionsPage;
			} catch (IOException ex) {
				messageQueue.sendMessage(this, null, Messages.E108, url, ex.getMessage());
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
					messageQueue.sendMessage(this, null, Messages.I107, String.valueOf(pageNumber), url);

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

		messageQueue.sendMessage(this, null, Messages.I104, outputFile.getAbsolutePath());
		try {
			var content = JSONSerializer.serialize(database);
			var writer = new FileWriter(outputFile, StandardCharsets.UTF_8);
			writer.write(content);
			writer.close();
			messageQueue.sendMessage(this, null, Messages.I106,
					MemoryUtility.getRoundedMemorySize(outputFile.length()));

		} catch (IOException ex) {
			messageQueue.sendMessage(this, null, Messages.E105, outputFile.getAbsolutePath(), ex.getMessage());
		}
	}

	public Database loadDatabase(File inputFile, MessageQueue messageQueue) {

		var content = FileUtility.readAsString(inputFile);
		var database = JSONSerializer.deserialize(content, Database.class);

		return database;
	}

}
