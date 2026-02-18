package org.atari.asma;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.atari.asma.demozoo.model.Database;
import org.atari.asma.util.MessageQueue;
import org.atari.asma.util.StringUtility;

public class ASMAHTMLExporter {

	private HTMLWriter html;

	public ASMAHTMLExporter(HTMLWriter htmlWriter) {
		this.html = htmlWriter;
	}

	private static String getDemozooIDHTML(int demozooID) {
		final var link = "https://demozoo.org/music/" + demozooID;
		final var htmlString = HTMLWriter.getTextWithLink(Long.toString(demozooID), link);
		return htmlString;
	}

	private static String getASMAPlayerHTML(String urlFilePath) {
		urlFilePath = urlFilePath.replace('#', '/');
		final var link = "https://asma.atari.org/asmadb/#/" + urlFilePath;
		final var htmlString = HTMLWriter.getTextWithLink(urlFilePath, link);
		return htmlString;
	}

	private void writeMessageQueueCell(MessageQueue messageQueue) {
		var htmlBuilder = new StringBuilder();
		var entries = messageQueue.getEntries();
		for (var entry : entries) {
			var color = "";
			switch (entry.getType()) {
			case ERROR:
				color = "color:red;";
				break;
			case WARNING:
				color = "color:#FF8C00;";

				break;
			case INFO:
				break;
			}
			htmlBuilder.append("<div style=\"" + color + "\">");
			htmlBuilder.append(entry.getType().toString()).append(" : ").append(entry.getMessage());
			htmlBuilder.append("</div>");

		}
		html.writeHTMLCell(htmlBuilder.toString());
	}

	private void writeHeader(String basePath) {
		var htmlBuilder = new StringBuilder();
		htmlBuilder.append("<!DOCTYPE html>");
		htmlBuilder.append("<html lang=\"en\">");
		htmlBuilder.append("<head>");
		htmlBuilder.append("<title>Atari SAP Music Overview</title>");

		htmlBuilder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		htmlBuilder.append("<link href=\"+" + basePath
				+ "../img/favicon.ico\" rel=\"shortcut icon\" type=\"image/vnd.microsoft.icon\">");
		htmlBuilder.append("<link rel=\"stylesheet\" href=\"" + basePath + "asma.css\" type=\"text/css\">");

		htmlBuilder.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		htmlBuilder.append("<script src=\"" + basePath + "sorttable.js\"></script>");
		htmlBuilder.append("</head>");
		htmlBuilder.append("\n");
		html.write(htmlBuilder.toString());
	}

	private void writeMessageSummary(String dateTime, List<MessageQueue> messageQueueList) {
		int fileCount = 0;
		int fileWithIssuesCount = 0;
		int errorCount = 0;
		int warningCount = 0;
		for (var messageQueue : messageQueueList) {
			fileCount++;
			if (hasIssues(messageQueue)) {
				fileWithIssuesCount++;
				errorCount += messageQueue.getErrorCount();
				warningCount += messageQueue.getWarningCount();
			}
		}
		html.beginTable();
		html.beginRow();
		html.writeHeaderTextCell("Date");
		html.writeHeaderTextCell("Entries");
		html.writeHeaderTextCell("Entries with Issues");
		html.writeHeaderTextCell("Errors");
		html.writeHeaderTextCell("Warnings");
		html.endRow();
		html.beginRow();
		html.writeTextCell(dateTime);
		html.writeLongCell(fileCount);
		html.writeLongCell(fileWithIssuesCount);
		html.writeLongCell(errorCount);
		html.writeLongCell(warningCount);
		html.endRow();
		html.endTable();
	}

	private void writeASMASummary(ASMADatabase asmaDatabase) {

		List<MessageQueue> messageQueueList = new ArrayList<MessageQueue>();
		for (var fileInfo : asmaDatabase.fileInfoList.getEntries()) {
			messageQueueList.add(fileInfo.getMessageQueue());
		}
		writeMessageSummary(StringUtility.getToday(), messageQueueList);
	}

	private void writeASMA(ASMADatabase asmaDatabase, boolean onlyIssues) {
		html.beginTable();
		html.beginRow();
		html.writeHeaderTextCell("Line");
		html.writeHeaderTextCell("File Path");
		html.writeHeaderTextCell("Title");
		html.writeHeaderTextCell("Author");
		html.writeHeaderTextCell("Date");
		html.writeHeaderTextCell("Channels");
		html.writeHeaderTextCell("Hardware");
		html.writeHeaderTextCell("Downloads");
		html.writeHeaderTextCell("Songs");
		html.writeHeaderTextCell("Demozoo ID");
		html.writeHeaderTextCell("Demozoo Title");
		html.writeHeaderTextCell("Demozoo Author");
		html.writeHeaderTextCell("Messages");
		html.endRow();

		var line = 1;
		for (var fileInfo : asmaDatabase.fileInfoList.getEntries()) {
			if (onlyIssues && !hasIssues(fileInfo.getMessageQueue())) {
				continue;
			}
			html.beginRow();
			html.writeLongCell(line);
			html.writeHTMLCell(getASMAPlayerHTML(fileInfo.filePath));
			html.writeTextCell(fileInfo.title);
			html.writeTextCell(fileInfo.author);
			html.writeTextCell(fileInfo.date);
			html.writeLongCell(fileInfo.channels);
			html.writeTextCell(fileInfo.hardware);
			html.writeTextCell(fileInfo.originalModuleExt);
			html.writeLongCell(fileInfo.songs);
			var demozooIDBuilder = new StringBuilder();
			var demozootTitleBuilder = new StringBuilder();
			var demozooAuthorBuilder = new StringBuilder();
			for (int songNumber = 1; songNumber <= fileInfo.songs; songNumber++) {

				final var urlFilePath = fileInfo.getURLFilePath(songNumber);
				final var asmaProduction = asmaDatabase.productionList.getByURLFilePath(urlFilePath);
				if (asmaProduction != null) {
					demozooIDBuilder.append("<div>").append(getDemozooIDHTML(asmaProduction.id));
					demozootTitleBuilder.append("<div>").append(asmaProduction.title);
					demozooAuthorBuilder.append("<div>"); // TODO

				} else {
					demozooIDBuilder.append("<div style=\"color:red;\">Missing</span>");
					demozootTitleBuilder.append("<div>");
					demozooAuthorBuilder.append("<div>");

				}
				demozooIDBuilder.append("</div>");
				demozootTitleBuilder.append("</div>");
				demozooAuthorBuilder.append("</div>");
			}
			html.writeHTMLCell(demozooIDBuilder.toString());
			html.writeHTMLCell(demozootTitleBuilder.toString());
			html.writeHTMLCell(demozooAuthorBuilder.toString());
			writeMessageQueueCell(fileInfo.getMessageQueue());
			html.endRow();
			line++;
		}
		html.endTable();
	}

	private void writeDemozooSummary(Database database) {
		List<MessageQueue> messageQueueList = new ArrayList<MessageQueue>();
		for (var production : database.productions) {
			messageQueueList.add(production.getMessageQueue());
		}
		writeMessageSummary(database.updateDateTime.substring(0, 10), messageQueueList);
	}

	private void writeDemozoo(Database database, boolean onlyIssues) {
		html.beginTable();
		html.beginRow();
		html.writeHeaderTextCell("Line");
		html.writeHeaderTextCell("ID");
		html.writeHeaderTextCell("Title");
		html.writeHeaderTextCell("Author");
		html.writeHeaderTextCell("Release Date");
		html.writeHeaderTextCell("Hardware");
		html.writeHeaderTextCell("Downloads");
		html.writeHeaderTextCell("File Extensions");
		html.writeHeaderTextCell("Tags");
		html.writeHeaderTextCell("URL File Path");

		html.writeHeaderTextCell("Messages");
		html.endRow();

		var line = 1;
		for (var production : database.productions) {
			if (onlyIssues && !hasIssues(production.getMessageQueue())) {
				continue;
			}
			html.beginRow();
			html.writeLongCell(line);
			html.writeHTMLCell(getDemozooIDHTML(production.id));
			html.writeTextCell(production.title);
			var authorHTMLBuilder = new StringBuilder();
			for (var authorNick : production.author_nicks) {
				authorHTMLBuilder.append(authorNick.releaser.id).append("<br>");
			}
			html.writeTextCell(authorHTMLBuilder.toString());
			html.writeTextCell(production.release_date);
			html.writeTextCell(production.getHardware());

			Set<String> downloadLinks = new TreeSet<String>();
			for (var link : production.download_links) {
				downloadLinks.add(link.url);
			}
			var downloadLinkHTMLBuilder = new StringBuilder();
			for (String link : downloadLinks) {
				downloadLinkHTMLBuilder.append("<p>");
				var text = "";
				try {
					var uri = new URI(link);
					text = uri.getHost();
				} catch (URISyntaxException ex) {
					text = ex.toString();
				}
				downloadLinkHTMLBuilder.append("<a href=\"" + link + "\">" + text + "</a>");
				downloadLinkHTMLBuilder.append("<p/>");
			}
			html.writeHTMLCell(downloadLinkHTMLBuilder.toString());

			var fileExtensions = production.getFileExtensions();
			html.writeTextCell(StringUtility.toString(fileExtensions));

			html.writeTextCell(production.getTags());
			var urlFilePaths = production.getASMAURLFilePaths();
			var urlFilePathsHTMLBuilder = new StringBuilder();
			for (var urlFilePath : urlFilePaths) {
				urlFilePathsHTMLBuilder.append("<div>");
				urlFilePathsHTMLBuilder.append(getASMAPlayerHTML(urlFilePath));
				urlFilePathsHTMLBuilder.append("</div>");
			}
			html.writeHTMLCell(urlFilePathsHTMLBuilder.toString());

			writeMessageQueueCell(production.getMessageQueue());
			html.endRow();
			line++;
		}
		html.endTable();
	}

	private static boolean hasIssues(MessageQueue messageQueue) {
		return (messageQueue.getErrorCount() > 0 || messageQueue.getWarningCount() > 0);
	}

	public void write(ASMADatabase asmaDatabase, Database database, boolean onlyIssues, String basePath) {

		writeHeader(basePath);

		html.beginHeading("toc");
		html.writeText("Table of Contents");
		html.endHeading();
		html.beginParagraph();
		html.writeText("ASMA", "#asma");
		html.endParagraph();
		writeASMASummary(asmaDatabase);

		html.beginParagraph();
		html.writeText("Demozoo", "#demozoo");
		html.endParagraph();
		writeDemozooSummary(database);

		html.beginHeading("asma");
		html.writeText("ASMA");
		html.endHeading();
		writeASMA(asmaDatabase, onlyIssues);

		html.beginHeading("demozoo");
		html.writeText("Demozoo");
		html.endHeading();
		writeDemozoo(database, onlyIssues);

	}

}
