package org.atari.asma;

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

	private void writeHeader() {
		var htmlBuilder = new StringBuilder();
		htmlBuilder.append("<!DOCTYPE html>");
		htmlBuilder.append("<html lang=\"en\">");
		htmlBuilder.append("<head>");
		htmlBuilder.append("<title>Atari SAP Music Overview</title>");

		htmlBuilder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		htmlBuilder
				.append("<link href=\"../img/favicon.ico\" rel=\"shortcut icon\" type=\"image/vnd.microsoft.icon\">");
		htmlBuilder.append("<link rel=\"stylesheet\" href=\"asma.css\" type=\"text/css\">");

		htmlBuilder.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		htmlBuilder.append("<script src=\"sorttable.js\"></script>");
		htmlBuilder.append("</head>");
		html.write(htmlBuilder.toString());
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
		html.writeHeaderTextCell("Format");
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

	private void writeDemozoo(Database database, boolean onlyIssues) {
		html.beginTable();
		html.beginRow();
		html.writeHeaderTextCell("Line");
		html.writeHeaderTextCell("ID");
		html.writeHeaderTextCell("Title");
		html.writeHeaderTextCell("Author");
		html.writeHeaderTextCell("Release Date");
		html.writeHeaderTextCell("Hardware");
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

	public void write(ASMADatabase asmaDatabase, Database database, boolean onlyIssues) {

		writeHeader();

		html.beginHeading("toc");
		html.writeText("Table of Contents");
		html.endHeading();
		html.beginParagraph();
		html.writeText("ASMA", "#asma");
		html.endParagraph();
		html.beginParagraph();
		html.writeText("Demozoo", "#demozoo");
		html.endParagraph();

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
