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

//		if (args.length > 1) {
//			messageQueue.sendMessage("Usage: SAPEditor <sap file|sap folder>");
//			return;
//		}

		String filePath = args[0];
		File file = new File(filePath);

		runFilesOrFolders(new File[] { file });

	}

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
		File outputFile = null;
		SAPFile sapFile = null;
		messageQueue.sendInfo("Reading '" + inputFile.getAbsolutePath() + "'.");
		final var fileExtension = (FileUtility.getFileExtension(inputFile.getName()).toLowerCase());
		if (fileExtension.equals(".sap")) {
			sapFile = sapFileLogic.loadSAPFile(inputFile, messageQueue);
			outputFile = inputFile;
		} else if (SAPFile.isOriginalModuleFileExtension(fileExtension)) {
			sapFile = sapFileLogic.loadOriginalModuleFile(inputFile, messageQueue);
			if (sapFile != null) {
				outputFile = FileUtility.changeFileExtension(inputFile, ".sap");
				if (outputFile.exists()) {
					messageQueue.sendWarning("Cannot convert '" + inputFile.getName() + "' to '" + outputFile.getName()
							+ "'. Target file already exists.");
					// TODO return;
				} else {
					messageQueue.sendInfo("Converted '" + inputFile.getName() + "' to '" + outputFile.getName() + "'.");
				}
			}
		} else if (fileExtension.equals(".xex")) {
			outputFile = null;
		}

		if (sapFile != null) {
			sapFileDialog.show(inputFile, outputFile, sapFile);
		} else {
			messageQueue.sendInfo("Error reading '" + inputFile.getAbsolutePath() + "'. See above.");
		}
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
			checkSAPFile(file, localMessageQueue);
			localMessageQueue.flush();
		});

		messageQueue.sendInfo("All " + fileList.size() + " files processed.");

	}

	private SAPFile checkSAPFile(File file, MessageQueue messageQueue) {
		var sapFile = sapFileLogic.loadSAPFile(file, messageQueue);
		if (sapFile == null) {
			messageQueue.sendInfo("Error reading '" + file.getAbsolutePath() + "'. See above.");
		}
		return sapFile;
	}

}
