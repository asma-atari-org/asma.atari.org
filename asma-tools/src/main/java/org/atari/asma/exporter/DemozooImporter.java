package org.atari.asma.exporter;
import org.atari.asma.demozoo.Demozoo;

import java.io.File;

import org.atari.asma.ASMAPaths;
import org.atari.asma.util.MessageQueueUtility;

import com.wudsn.tools.base.common.MessageQueue;

public class DemozooImporter {

	public static void main(String[] args) {
		System.exit(new DemozooImporter().run(args));
	}

	private DemozooImporter() {
	}

	@SuppressWarnings("static-method")
	private int run(String[] args) {
		MessageQueue messageQueue = MessageQueueUtility.createConsoleInstance();

		if (args.length != 1) {
			System.out.println("Usage: DemozooImporter <trunk/asma folder>");
			return 0;
		}

		var sourceFolderPath = args[0];
		File sourceFolder = new File(sourceFolderPath);

		var databaseFile = new File(sourceFolder, ASMAPaths.DEMOZOO_DATABASE_JSON);

		var demozoo = new Demozoo();
		messageQueue.sendMessage(this, null, org.atari.asma.demozoo.Messages.I101, databaseFile.getPath());

		if (!databaseFile.exists()) {
			messageQueue.sendMessage(this, null, org.atari.asma.demozoo.Messages.E102, databaseFile.getPath());
			return 1;
		}
		if (!databaseFile.canWrite()) {
			messageQueue.sendMessage(this, null, org.atari.asma.demozoo.Messages.E103, databaseFile.getPath());
			return 1;
		}
		demozoo.importDatabase(databaseFile, messageQueue);
		System.out.println(MessageQueueUtility.getSummary(messageQueue));
		return 0;

	}

}
