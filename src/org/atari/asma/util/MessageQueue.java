package org.atari.asma.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageQueue {

	public enum MessagType {
		INFO, WARNING, ERROR
	};

	public static final class Entry {

		private MessagType type;
		private String id;
		private String message;

		public Entry(MessagType type, String id, String message) {
			this.type = type;
			this.id = id;
			this.message = message;
		}

		public MessagType getType() {
			return type;
		}

		public String getID() {
			return id;
		}

		public String getMessage() {
			return message;
		}
	}

	private PrintStream out;
	private PrintStream err;

	private List<Entry> entries;
	private int infoCount;
	private int warningCount;
	private int errorCount;

	public MessageQueue() {

		entries = new ArrayList<Entry>();
		clear();
	}

	public void bind(PrintStream out, PrintStream err) {
		this.out = out;
		this.err = err;
	}

	public List<Entry> getEntries() {
		return Collections.unmodifiableList(entries);
	}

	public void addEntries(MessageQueue otherMessageQueue) {
		for (var entry : otherMessageQueue.getEntries()) {
			sendMessage(entry.type, entry.id, entry.message);
		}
	}

	public void clear() {
		entries.clear();
		infoCount = 0;
		warningCount = 0;
		errorCount = 0;

	}

	public long getInfoCount() {
		return infoCount;
	}

	public long getWarningCount() {
		return warningCount;
	}

	public long getErrorCount() {
		return errorCount;
	}

	public void printSummary() {
		if (out != null) {
			out.println(errorCount + " errors, " + warningCount + " warnings, " + infoCount + " infos");
			out.flush();
		}
		clear();
	}

	private void sendMessage(MessagType type, String id, String message) {
		entries.add(new Entry(type, id, message));

		switch (type) {

		case INFO: {
			if (out != null) {
				out.println("INFO:    " + message);
				out.flush();
			}
			infoCount++;
			break;
		}
		case WARNING: {
			if (err != null) {
				err.println("WARNING: " + message);
				err.flush();
			}
			warningCount++;
			break;
		}
		case ERROR: {

			if (err != null) {
				err.println("ERROR:   " + message);
				err.flush();
			}
			errorCount++;
			break;
		}
		}
	}

	public void sendInfo(String message) {
		sendMessage(MessagType.INFO, "", message);

	}

	public void sendWarning(String message) {
		sendMessage(MessagType.WARNING, "", message);

	}

	public void sendError(String message) {
		sendMessage(MessagType.ERROR, "", message);

	}
}
