package org.atari.asma.demozoo;

import java.io.File;

import org.atari.asma.ASMAPaths;
import org.atari.asma.util.MessageQueueFactory;
import org.atari.asma.util.MessageQueue;

public class DemozooImporter {

	public static void main(String[] args) {
		System.exit(new DemozooImporter().run(args));
	}

	private DemozooImporter() {
	}

	@SuppressWarnings("static-method")
	private int run(String[] args) {
		MessageQueue messageQueue = MessageQueueFactory.createSystemInstance();

		if (args.length != 1) {
			messageQueue.sendMessage("Usage: DemozooImporter <trunk/asma folder>");
			return 0;
		}

		var sourceFolderPath = args[0];
		File sourceFolder = new File(sourceFolderPath);

		var databaseFile = new File(sourceFolder, ASMAPaths.DEMOZOO_DATABASE_JSON);

		var demozoo = new Demozoo();
		messageQueue.sendMessage("Fetching Demozoo database to '" + databaseFile.getPath() + "'.");

		if (!databaseFile.exists()) {
			messageQueue.sendError("Output file '" + databaseFile.getPath() + "' does not exist.");
			return 1;
		}
		if (!databaseFile.canWrite()) {
			messageQueue.sendError("Output file '" + databaseFile.getPath() + "' is not writeable.");
			return 1;
		}
		demozoo.importDatabase(databaseFile, messageQueue);
		messageQueue.printSummary();
		return 0;

	}

}
