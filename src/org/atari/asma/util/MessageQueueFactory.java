package org.atari.asma.util;

public class MessageQueueFactory {

	public static MessageQueue createSystemInstance() {
		var result = new MessageQueue();
		result.bind(System.out, System.err);
		return result;
	}
}
