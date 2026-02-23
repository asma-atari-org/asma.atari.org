package org.atari.asma;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.atari.asma.demozoo.DemozooLink;
import org.atari.asma.demozoo.model.Database;
import org.atari.asma.demozoo.model.Production;
import org.atari.asma.util.MessageQueue;
import org.atari.asma.util.MessageQueue.Entry;
import org.atari.asma.util.StringUtility;

public class ASMAHTMLExporter {

	private HTMLWriter html;

	public ASMAHTMLExporter(HTMLWriter htmlWriter) {
		this.html = htmlWriter;
	}

	private static String getDemozooMusicHTML(int productionID) {
		String link = null;
		if (productionID > 0) {
			link = DemozooLink.getMusic(productionID);
		}
		final var htmlString = HTMLWriter.getTextWithLink(Long.toString(productionID), link);
		return htmlString;
	}

	private static String getDemozooScenerHTML(String text, int scenerID) {
		String link = null;
		if (scenerID > 0) {
			link = DemozooLink.getScener(scenerID);
		}
		final var htmlString = HTMLWriter.getTextWithLink(text, link);
		return htmlString;
	}

	private static String getASMAPlayerHTML(String urlFilePath) {
		urlFilePath = urlFilePath.replace('#', '/');
		final var link = "https://asma.atari.org/asmadb/#/" + urlFilePath;
		final var htmlString = HTMLWriter.getTextWithLink(urlFilePath, link);
		return htmlString;
	}

	private static final class Link {
		public final String href;
		public final String html;

		public Link(String html, String href) {
			this.html = html;
			this.href = href;

		}

		public String toHTML() {
			return "<a href=\"" + href + "\" target=\"_blank\">" + html + "</a>";
		}
	}

	private static void formatMessageQueueEntry(StringBuilder htmlBuilder, Entry entry, List<Link> linkList) {
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

		htmlBuilder.append(entry.getType().toString()).append(" : ");
		if (!entry.getID().isEmpty()) {
			htmlBuilder.append(entry.getID()).append(" - ");
		}
		htmlBuilder.append(HTMLWriter.encodeHTML(entry.getMessage()));

		for (var link : linkList) {
			htmlBuilder.append(" ").append(link.toHTML());
		}
		htmlBuilder.append("</div>");
	}

	private void writeMessageQueueCell(FileInfo fileInfo, int scenerID) {
		var htmlBuilder = new StringBuilder();
		var entries = fileInfo.getMessageQueue().getEntries();
		for (var entry : entries) {

			List<Link> linkList = new ArrayList<Link>();
			// ERROR : SAP-107 - File Composers/Mega9man/Heat_Squared.sap has no Demozoo ID.
			if (fileInfo.getMessageQueue().containsMessage("SAP-107")) {
				try {
					linkList.add(new Link("Find Music",
							"https://demozoo.org/search/?q=" + URLEncoder.encode(fileInfo.title.trim(), "UTF-8")
									+ "+type%3Amusic+platform%3A%22Atari+8+Bit%22"));
				} catch (UnsupportedEncodingException ex) {
					throw new RuntimeException(ex);
				}
				String url = "https://demozoo.org/music/new/";
				if (scenerID > 0) {
					url += "?releaser_id=" + scenerID;
				}
				linkList.add(new Link("Create Music", url));

			}

			formatMessageQueueEntry(htmlBuilder, entry, linkList);

		}
		html.writeHTMLCell(htmlBuilder.toString());
	}

	private void writeMessageQueueCell(Production production) {
		var htmlBuilder = new StringBuilder();
		var entries = production.getMessageQueue().getEntries();
		for (var entry : entries) {

			List<Link> linkList = new ArrayList<Link>();
			// ERROR : DMO-006 - Music download URL contains non-existing file path
			// "Composers/Krix_Mario/Acidjazzed_Evening.sap". Check if ASMA path has
			// changed.
			if (production.getMessageQueue().containsMessage("DMO-006")) {
				linkList.add(new Link("Edit Download Links", DemozooLink.getEditDownloadLinks(production.id)));

			}

			formatMessageQueueEntry(htmlBuilder, entry, linkList);

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
		html.write("\n");

		var line = 1;
		for (var fileInfo : asmaDatabase.fileInfoList.getEntries()) {
			if (onlyIssues && !hasIssues(fileInfo.getMessageQueue())) {
				continue;
			}
			html.beginRow();
			html.writeLongCell(line);
			html.writeHTMLCell(getASMAPlayerHTML(fileInfo.filePath));
			html.writeTextCell(fileInfo.title);

			int composerID = 0;
			var composerOrGroup = asmaDatabase.getComposerOrGroup(fileInfo.getFolderPath());
			if (composerOrGroup instanceof Composer) {
				var composer = (Composer) composerOrGroup;
				composerID = Integer.valueOf(composer.demozooID);
				html.writeHTMLCell(getDemozooScenerHTML(fileInfo.author, composerID));
			} else {
				html.writeTextCell(fileInfo.author);
			}
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
					demozooIDBuilder.append("<div>").append(getDemozooMusicHTML(asmaProduction.id));
					demozootTitleBuilder.append("<div>").append(asmaProduction.title);
					demozooAuthorBuilder.append("<div>");

				} else {
					demozooIDBuilder.append("<div style=\"color:orange;\">Missing</span>");
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
			writeMessageQueueCell(fileInfo, composerID);
			html.endRow();
			html.write("\n");
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

			// Music download URL contains non-existing file path ?
			String idHTML;
			idHTML = getDemozooMusicHTML(production.id);
			html.writeHTMLCell(idHTML);
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

			writeMessageQueueCell(production);
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
