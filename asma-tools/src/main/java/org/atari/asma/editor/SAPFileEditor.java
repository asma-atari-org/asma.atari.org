package org.atari.asma.editor;
import org.atari.asma.asap.ASAPFileLogic;
import org.atari.asma.sap.SAPFileProcessor;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;

import org.atari.asma.FileExtension;
import org.atari.asma.util.FileUtility;
import org.atari.asma.util.MessageQueueUtility;

import com.wudsn.tools.base.common.MessageQueue;
import com.wudsn.tools.base.gui.FileDrop;
import com.wudsn.tools.base.gui.UIManager;

public class SAPFileEditor implements SAPFileProcessor {

	private MessageQueue messageQueue;
	private ASAPFileLogic sapFileLogic;

	private SAPFileList fileList;

	private SAPFileEditor() {
		messageQueue = MessageQueueUtility.createConsoleInstance();
		sapFileLogic = new ASAPFileLogic();

		fileList = new SAPFileList();
		addFileDropListener(fileList.getFrame());
	}

	private void addFileDropListener(JFrame frame) {
		new FileDrop(frame, true, new FileDrop.Listener() {
			@Override
			public boolean isDropAllowed() {
				return true;
			}

			@Override
			public void filesDropped(File[] files) {
				for (var file : files) {
					processFile(file);
				}
			}
		});
	}

	public static void main(String[] args) {
		SAPFileEditor instance = new SAPFileEditor();
		instance.run(args);
	}

	private void run(String[] args) {
		UIManager.init();

		var files = new File[args.length];
		for (int i = 0; i < args.length; i++) {
			files[i] = new File(args[i]);
		}
		runFilesOrFolders(files);
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
				messageQueue.sendMessage(this, null, Messages.E401, file.getAbsolutePath(), ex.getMessage());
				ex.printStackTrace();
			}
		}
		System.out.println(MessageQueueUtility.getSummary(messageQueue));
		messageQueue.clear();
	}

	public void processFile(File inputFile) {

		var path = inputFile.getAbsolutePath();
		var dialog = fileList.getDialog(path);
		if (dialog == null) {
			dialog = new SAPFileDialog(this);
			addFileDropListener(dialog.getFrame());
			fileList.putDialog(path, dialog);
		}
		dialog.show(inputFile);
	}

	public void closeFile(File inputFile) {
		fileList.removeDialog(inputFile.getAbsolutePath());
	}

	public void processFolder(File folder) {
		messageQueue.sendMessage(this, null, Messages.I404, folder.getAbsolutePath());

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
		messageQueue.sendMessage(this, null, Messages.I405, String.valueOf(totalCount));

		var count = new AtomicInteger();
		final var blockSize = 100;
		fileList.parallelStream().forEach(file -> {
			if (count.incrementAndGet() % blockSize == 0) {
				synchronized (messageQueue) {
					messageQueue.sendMessage(this, null, Messages.I406, count.toString());
				}
			}

			// Each worker collects its messages, which are then added in one piece.
			var localMessageQueue = new MessageQueue();
			var sapFile = sapFileLogic.loadSAPFile(file, localMessageQueue);
			if (sapFile == null) {
				localMessageQueue.sendMessage(this, null, Messages.I407, file.getAbsolutePath());
			}
			synchronized (messageQueue) {
				messageQueue.addAll(localMessageQueue);
			}
		});

		messageQueue.sendMessage(this, null, Messages.I408, String.valueOf(fileList.size()));

	}

}
