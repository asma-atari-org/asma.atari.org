package org.atari.asma;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HTMLTable {

	private Writer writer;

	public HTMLTable(Writer writer) {
		this.writer = writer;
	}

	private String encodeHTML(String text) {

		return text;
	}

	private void write(String html) {
		try {
			writer.write(html);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
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
		write("<td>");
		if (link != null) {
			String encodedLink;

			encodedLink = link;

			write("<a href=\"" + encodedLink + "\" target=\"_blank\">");
		}
		write(encodeHTML(text));
		if (link != null) {
			write("</a>");
		}
		write("</td>");
	}

}
