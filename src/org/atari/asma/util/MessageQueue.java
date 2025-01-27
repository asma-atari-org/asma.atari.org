package org.atari.asma.util;

import java.io.PrintStream;

public final class MessageQueue {

	private PrintStream out;
	private PrintStream err;

	private int infoCount;
	private int warningCount;
	private int errorCount;

	public MessageQueue(PrintStream out, PrintStream err) {
		if (out == null) {
			throw new IllegalArgumentException("Parameter 'out' must not be null.");
		}
		if (err == null) {
			throw new IllegalArgumentException("Parameter 'err' must not be null.");
		}
		this.out = out;
		this.err = err;
		clear();
	}

	public void clear() {
		infoCount = 0;
		warningCount = 0;
		errorCount = 0;

	}

	public void printSummary() {
		out.println(errorCount + " errors, " + warningCount + " warnings, " + infoCount + " infos");
		out.flush();
		clear();
	}

	public void sendMessage(String message) {
		out.println(message);
		out.flush();
	}

	public void sendInfo(String message) {
		out.println("INFO:    " + message);
		out.flush();
		infoCount++;
	}

	public void sendWarning(String message) {
		err.println("WARNING: " + message);
		err.flush();
		warningCount++;
	}

	public void sendError(String message) {
		err.println("ERROR:   " + message);
		err.flush();
		errorCount++;
	}
}
