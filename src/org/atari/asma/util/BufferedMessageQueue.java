package org.atari.asma.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class BufferedMessageQueue extends MessageQueue {

	private MessageQueue parentMessageQueue;

	private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	private ByteArrayOutputStream errStream = new ByteArrayOutputStream();

	public BufferedMessageQueue(MessageQueue parentMessageQueue) {
		super();
		bind(new PrintStream(outStream), new PrintStream(errStream));
		this.parentMessageQueue = parentMessageQueue;
	}

	public void flush() {
		synchronized (parentMessageQueue) {
				var out = outStream.toString(StandardCharsets.UTF_8);
				if (!out.isEmpty()) {
					parentMessageQueue.sendInfo(out);
				}
				var err = errStream.toString(StandardCharsets.UTF_8);
				if (!err.isEmpty()) {
					parentMessageQueue.sendError(err);
				}
		}
	}
}
