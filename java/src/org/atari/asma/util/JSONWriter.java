package org.atari.asma.util;

import java.io.IOException;
import java.io.Writer;

public class JSONWriter {

	private Writer writer;

	public JSONWriter(Writer writer) {
		if (writer == null) {
			throw new IllegalArgumentException("Parameter writer must not be null.");
		}
		this.writer = writer;
	}

	private static String escapeJSON(String text) {
		text = text.replace("\"", "\\\"");
		text = text.replace("\n", "\\n");
		return text;
	}

	public void writeAttribute(String attribute) throws IOException  {
		writer.write("\"" + attribute + "\": ");
	}
	public void writeJSONString(String attribute, String value) throws IOException {
		writer.write("\"" + attribute + "\": \"" + escapeJSON(value) + "\"");
	}

	public void writeJSONLong(String attribute, long value) throws IOException {
		writer.write("\"" + attribute + "\": " + Long.toString(value));
	}

	public void beginObject() throws IOException {
		writer.write('{');
	}

	public void endObject() throws IOException {
		writer.write("}\n");
	}

	public void beginArray() throws IOException {
		writer.write("[\n");
	}

	public void endArray() throws IOException {
		writer.write("]\n");
	}
	
	public void writeSeparator() throws IOException {
		writer.write(", ");
	}

}
