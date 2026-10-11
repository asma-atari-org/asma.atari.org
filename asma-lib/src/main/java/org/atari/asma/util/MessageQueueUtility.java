package org.atari.asma.util;

import com.wudsn.tools.base.common.MessageQueue;
import com.wudsn.tools.base.console.Console;
import com.wudsn.tools.base.repository.Message;

/**
 * ASMA's policies on top of WUDSN Base's {@link MessageQueue}: the console
 * queue of the command line tools, the summary line and the order of the
 * issues report. See plans/06, phase 2.
 *
 * @author Peter Dell
 */
public final class MessageQueueUtility {

	private MessageQueueUtility() {
	}

	/**
	 * Creates a queue that prints every message on the console when it is sent,
	 * for the command line tools.
	 *
	 * @return The queue, not <code>null</code>.
	 */
	public static MessageQueue createConsoleInstance() {
		var result = new MessageQueue();
		result.setMessageQueueListener(new Console());
		return result;
	}

	/** Determines whether the queue contains errors or warnings. */
	public static boolean hasIssues(MessageQueue messageQueue) {
		return messageQueue.containsError() || messageQueue.containsWarning();
	}

	/**
	 * Compares two queues by their number of errors, then warnings, then
	 * infos.
	 */
	public static int compare(MessageQueue messageQueue1, MessageQueue messageQueue2) {
		var result = Integer.compare(messageQueue1.getCount(Message.ERROR), messageQueue2.getCount(Message.ERROR));
		if (result == 0) {
			result = Integer.compare(messageQueue1.getCount(Message.WARNING),
					messageQueue2.getCount(Message.WARNING));
		}
		if (result == 0) {
			result = Integer.compare(messageQueue1.getCount(Message.INFO), messageQueue2.getCount(Message.INFO));
		}
		return result;
	}

	/** The summary line, e.g. "0 errors, 7 warnings, 17 infos". */
	public static String getSummary(MessageQueue messageQueue) {
		return messageQueue.getCount(Message.ERROR) + " errors, " + messageQueue.getCount(Message.WARNING)
				+ " warnings, " + messageQueue.getCount(Message.INFO) + " infos";
	}

	/** The severity as upper-case text, e.g. "WARNING"; status messages count as "INFO". */
	public static String getSeverityText(Message message) {
		switch (message.getSeverity()) {
		case Message.ERROR:
			return "ERROR";
		case Message.WARNING:
			return "WARNING";
		default:
			return "INFO";
		}
	}
}
