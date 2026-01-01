package org.atari.asma;

import java.io.IOException;
import java.io.Writer;

public class HTMLWriter {

	private Writer writer;
	private int id;
	private int headingLevel;

	public HTMLWriter(Writer writer) {
		this.writer = writer;
		id = 0;
		headingLevel = 0;
	}

	public static String encodeHTML(String text) {
		text = text.replace("\"", "&quot;");
		return text;
	}

	public static String getTextWithLink(String text, String link) {
		if (text == null) {
			text = "";
		}
		var builder = new StringBuilder();
		if (link != null) {
			String encodedLink;

			encodedLink = link;

			builder.append("<a href=\"" + encodedLink + "\" target=\"_blank\">");
		}
		builder.append(encodeHTML(text));
		if (link != null) {
			builder.append("</a>");
		}
		return builder.toString();
	}

	private String getNextID() {
		return "id" + id++;
	}

	public void write(String html) {
		try {
			writer.write(html);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public String beginHeading(String id) {
		// String id = getNextID();
		headingLevel++;
		write("<h" + headingLevel + " id=\"" + id + "\">");
		return id;
	}

	public void writeText(String text) {
		writeText(text, null);
	}

	public void writeText(String text, String link) {
		var html = getTextWithLink(text, link);
		write(html);
	}

	public void endHeading() {
		write("</h" + headingLevel + ">");
		headingLevel--;
	}

	public void beginParagraph() {
		write("<p>");
	}

	public void endParagraph() {
		write("</p>");
	}

	public void beginTable() {
		write("<table border=\"1\" style=\"border-collapse:collapse\">");
	}

	public void endTable() {
		write("</table>");
	}

	public void beginRow() {
		write("<tr>");
	}

	public void endRow() {
		write("</tr>");
	}

	public void writeHeaderTextCell(String text) {
		write("<th>");
		write(encodeHTML(text));
		write("</th>");
	}

	public void writeHTMLCell(String html) {
		write("<td>");
		write(html);
		write("</td>");
	}

	public void writeLongCell(long value) {
		writeLongCell(value, null);
	}

	public void writeLongCell(long value, String link) {
		writeTextCell(Long.toString(value), link);
	}

	public void writeTextCell(String text) {
		writeTextCell(text, null);
	}

	public void writeTextCell(String text, String link) {
		var html = getTextWithLink(text, link);
		writeHTMLCell(html);
	}

}
