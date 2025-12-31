package org.atari.asma.util;

public class ConsoleMessageQueue {

	public static MessageQueue createInstance() {
		return new MessageQueue(System.out, System.err);
	}
}
