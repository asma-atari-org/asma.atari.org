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
		messageQueue = MessageQueueFactory.createSystemInstance();
		sapFileLogic = new SAPFileLogic();
		sapFileDialog = new SAPFileDialog(this, sapFileLogic, messageQueue);
	}

	public static void main(String[] args) {
		SAPFileEditor instance = new SAPFileEditor();
		instance.run(args);
	};

	private void run(String[] args) {
		var files = new File[args.length];
		for (int i = 0; i < args.length; i++) {
			files[i] = new File(args[i]);
		}

		runFilesOrFolders(files);

	};

	private void runFilesOrFolders(File[] files) {
		messageQueue.clear();
		for (File file : files) {
			try {
				if (file.isDirectory()) {
					processFolder(file);
				} else {
					processFile(file);
				}

			} catch (Exception ex) {
				messageQueue.sendError(ex.getMessage());
				ex.printStackTrace();
			}
		}
		messageQueue.printSummary();
	}

	public void processFile(File inputFile) {

		sapFileDialog.show(inputFile);
	}

	private void processFolder(File folder) {
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
			var sapFile = sapFileLogic.loadSAPFile(file, localMessageQueue);
			if (sapFile == null) {
				messageQueue.sendInfo("Error reading '" + file.getAbsolutePath() + "'. See above.");
			}
			localMessageQueue.flush();
		});

		messageQueue.sendInfo("All " + fileList.size() + " files processed.");

	}
}
