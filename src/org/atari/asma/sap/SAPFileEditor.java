package org.atari.asma.sap;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.atomic.AtomicInteger;

import org.atari.asma.ASMAExporter.FileExtension;
import org.atari.asma.util.BufferedMessageQueue;
import org.atari.asma.util.FileUtility;
import org.atari.asma.util.MessageQueue;
import org.atari.asma.util.MessageQueueFactory;

public class SAPFileEditor {

	private MessageQueue messageQueue;
	private SAPFileLogic sapFileLogic;
	private SAPFileDialog sapFileDialog;

	private SAPFileEditor() {
		messageQueue =  MessageQueueFactory.createSystemInstance();
		sapFileLogic = new SAPFileLogic();
		sapFileDialog = new SAPFileDialog(this);
	}

	public static void main(String[] args) {
		SAPFileEditor instance = new SAPFileEditor();
		instance.run(args);
	};

	private void run(String[] args) {

//		if (args.length > 1) {
//			messageQueue.sendMessage("Usage: SAPEditor <sap file|sap folder>");
//			return;
//		}

		String filePath = args[0];
		File file = new File(filePath);

		runFiles(new File[] { file });

	}

	public void runFiles(File[] files) {
		messageQueue.clear();
		for (File file : files) {
			try {
				if (file.isDirectory()) {
					scanFolder(file);
				} else {
					messageQueue.sendInfo("Reading '" + file.getAbsolutePath() + "'.");
					var sapFile = checkSAPFile(file, messageQueue);
					if (sapFile != null) {
						sapFileDialog.show(file, sapFile);
					}
				}

			} catch (Exception ex) {
				messageQueue.sendError(ex.getMessage());
				ex.printStackTrace();
			}
		}
		messageQueue.printSummary();
	}

	private void scanFolder(File folder) {
		messageQueue.sendInfo("Scanning " + folder.getAbsolutePath());

		var fileList = FileUtility.getRecursiveFileList(folder, new FileFilter() {

			@Override
			public boolean accept(File file) {
				String fileName = file.getName().toLowerCase();
				if (fileName.endsWith(FileExtension.SAP)) {
					return true;
				}
				return false;
			}
		});
		int totalCount = fileList.size();
		messageQueue.sendInfo(totalCount + " files found.");

		var count = new AtomicInteger();
		final var blockSize = 100;
		fileList.parallelStream().forEach(file -> {
			if (count.incrementAndGet() % blockSize == 0) {
				synchronized (messageQueue) {
					messageQueue.sendInfo(count + " files processed.");
				}
			}

	
			var localMessageQueue = new BufferedMessageQueue(messageQueue);
			checkSAPFile(file, localMessageQueue);
			localMessageQueue.flush();
		});

		messageQueue.sendInfo("All " + fileList.size() + " files processed.");

	}

	private SAPFile checkSAPFile(File file, MessageQueue messageQueue) {
		var sapFile = sapFileLogic.readSAPFile(file, messageQueue);
		if (sapFile == null) {
			messageQueue.sendInfo("Error reading '" + file.getAbsolutePath() + "'. See above.");
		}
		return sapFile;
	}

}
