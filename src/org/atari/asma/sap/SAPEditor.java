package org.atari.asma.sap;

import java.io.File;
import java.io.FileFilter;

import org.atari.asma.ASMAExporter.FileExtension;
import org.atari.asma.MessageQueue;
import org.atari.asma.util.FileUtility;

public class SAPEditor {

	private MessageQueue messageQueue;
	private SAPFileLogic sapFileLogic;
	private SAPFileDialog sapFileDialog;

	private SAPEditor() {
		messageQueue = new MessageQueue(System.out, System.err);
		sapFileLogic = new SAPFileLogic();
		sapFileDialog = new SAPFileDialog();

	}

	public static void main(String[] args) {
		SAPEditor instance = new SAPEditor();
		instance.run(args);
	};

	private void run(String[] args) {

		if (args.length != 1) {
			messageQueue.sendMessage("Usage: SAPEditor <sap file|sap folder>");
			return;
		}

		String filePath = args[0];

		try {
			File file = new File(filePath);
			if (file.isDirectory()) {
				scanFolder(file);
			} else {
				messageQueue.sendInfo("Reading '" + file.getAbsolutePath() + "'.");
				checkSAPFile(file, true);
			}

		} catch (Exception ex) {
			messageQueue.sendError(ex.getMessage());
			ex.printStackTrace();
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

		int count = 0;
		for (File file : fileList) {
			checkSAPFile(file, false);
			count++;
			if (count % 100 == 0) {
				messageQueue.sendInfo(count + " files processed.");
			}
		}
		messageQueue.sendInfo("All " + fileList.size() + " files processed.");

	}

	private void checkSAPFile(File file, boolean details) {
		var sapFile = sapFileLogic.readSAPFile(file, messageQueue);
		if (sapFile != null) {
			if (details) {
				messageQueue.sendMessage(sapFile.toString());
				sapFileDialog.show(file, sapFile);
			}
		} else {
			messageQueue.sendInfo("Error reading '" + file.getAbsolutePath() + "'. See above.");

		}
	}

}
