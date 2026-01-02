package org.atari.asma;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.atari.asma.ASMAExporter.FileExtension;
import org.atari.asma.STIL.STILEntry;
import org.atari.asma.demozoo.ASMAProductionList;
import org.atari.asma.sap.SAPFileLogic;
import org.atari.asma.util.FileUtility;
import org.atari.asma.util.JSONWriter;
import org.atari.asma.util.MessageQueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileInfoList {

	private STIL stil;
	private SAPFileLogic sapFileLogic;
	private List<FileInfo> fileInfoList;
	private Gson gson;

	public FileInfoList(STIL stil, SAPFileLogic sapFileLogic) {
		this.stil = stil;
		this.sapFileLogic = sapFileLogic;

		fileInfoList = new ArrayList<FileInfo>();
		gson = new GsonBuilder().create();

	}

	public List<FileInfo> getEntries() {
		return fileInfoList;
	}

	// Default values are not exported but defaulted in the asam.js after loading
	public void exportToJSON(JSONWriter writer) throws IOException {
		boolean first = true;

		writer.beginArray();
		for (var songInfo : fileInfoList) {
			if (first) {
				first = false;
			} else {
				writer.writeSeparator();
			}
			songInfo.write(writer);
		}
		writer.endArray();
	}

	public void scanFolder(File sourceFolder, MessageQueue messageQueue, int maxFiles) {
		messageQueue.sendInfo("Scanning " + sourceFolder.getAbsolutePath());

		var fileList = FileUtility.getRecursiveFileList(sourceFolder, new FileFilter() {

			@Override
			public boolean accept(File file) {
				String fileName = file.getName().toLowerCase();
				if (fileName.endsWith(FileExtension.SAP) || fileName.endsWith(FileExtension.TTT)) {
					return true;
				}
				return false;
			}
		});

		messageQueue.sendInfo(fileList.size() + " matching files found.");

		int index = sourceFolder.getAbsolutePath().length() + 1;
		var count = new AtomicInteger();

		final int blockSize = 100;
		for (int i = 0; i < fileList.size() / blockSize; i++) {
			System.out.print("=");
		}
		System.out.println();

		// During parallel execution we need to decouple from the streams and
		// synchronize.
		var fileListMessageQueue = new MessageQueue();
		var cancelled = new AtomicBoolean();
		fileList.parallelStream().forEach(file -> {

			if (!cancelled.get()) {
				if (count.incrementAndGet() % blockSize == 0) {
					System.out.print("^");
				}
				String filePath = file.getAbsolutePath().substring(index);
				filePath = filePath.replace(File.separator, "/");

				var fileMessageQueue = new MessageQueue();
				var fileInfo = readFile(file, filePath, fileMessageQueue);
				synchronized (messageQueue) {
					fileListMessageQueue.addEntries(fileMessageQueue);

				}
				synchronized (fileInfoList) {
					if (fileInfo != null) {
						fileInfoList.add(fileInfo);
						if (fileInfoList.size() >= maxFiles) {
							cancelled.set(true);
						}
					}
				}
			}
		});
		System.out.println();

		// Parallel process destroyed the order, so sort again.
		fileInfoList.sort(new Comparator<FileInfo>() {

			@Override
			public int compare(FileInfo o1, FileInfo o2) {
				return o1.filePath.compareTo(o2.filePath);
			}
		});

		// Issue which result in broken SAP file files, are reported right away.
		messageQueue.addEntries(fileListMessageQueue);
	}

	private FileInfo readFile(File file, String filePath, MessageQueue messageQueue) {

		// Read binary from the input stream.
		byte[] module = FileUtility.readAsByteArray(file);

		FileInfo fileInfo = new FileInfo();
		fileInfo.filePath = filePath;
		fileInfo.fileSize = file.length();

		if (filePath.toLowerCase().endsWith(FileExtension.SAP)) {
			fileInfo.hardware = FileInfo.ATARI800;
			var sapFile = sapFileLogic.loadSAPFile(file, messageQueue);
			if (sapFile == null) {
				messageQueue.sendInfo("Error reading '" + file.getAbsolutePath() + "'. See above.");
				return null;
			}
			fileInfo.readFromSAPFile(sapFile);

			STILEntry stilEntry = stil.getSTILEntry("/" + filePath);
			if (stilEntry != null) {
				// "TODO";
				// fileInfo.comment = stilEntry.getComment();
			}
		} else if (filePath.toLowerCase().endsWith(FileExtension.TTT)) {
			fileInfo.hardware = FileInfo.ATARI2600;
			String jsonString = new String(module, StandardCharsets.UTF_8);

			TTTFile tttFile = gson.fromJson(jsonString, TTTFile.class);
			fileInfo.title = tttFile.metaName;
			fileInfo.author = tttFile.metaAuthor;
			fileInfo.date = "";
			fileInfo.comment = tttFile.metaComment;
			fileInfo.typeLetter = "T";
			fileInfo.originalModuleExt = "ttt";
			fileInfo.channels = 1;
			fileInfo.songs = 1;
			fileInfo.defaultSongIndex = 0;
		} else {
			throw new IllegalArgumentException("Unsupport file extension in " + filePath);
		}

		return fileInfo;
	}

	public void checkFileInfos(ComposerList composerList, ASMAProductionList productionList) {
		// From the ASMA "Sap.txt" specification.
		final int MAX_FILE_NAME_LENGTH = 26;
		final String FILE_NAME_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";

		final String COMPOSERS = "Composers/";
		int startIndex = COMPOSERS.length();

		for (var fileInfo : fileInfoList) {
			final var filePath = fileInfo.filePath;
			final var messageQueue = fileInfo.getMessageQueue();
			if (filePath.toLowerCase().endsWith(FileExtension.SAP)) {
				if (!filePath.endsWith(FileExtension.SAP)) {
					messageQueue.sendError("SAP-101: File " + fileInfo.filePath
							+ " does not have a lowercase \".sap\" file extension");
				}
				var fileName = filePath;
				var index = filePath.lastIndexOf('/');
				if (index >= 0) {
					fileName = fileName.substring(index + 1);
				}
				fileName = fileName.substring(0, fileName.length() - FileExtension.SAP.length());
				var length = fileName.length();
				if (length > MAX_FILE_NAME_LENGTH) {
					messageQueue.sendError("SAP-102: File " + fileInfo.filePath + " has a file name with more than "
							+ MAX_FILE_NAME_LENGTH + " characters. Use a shorter name.");
				}
				for (int i = 0; i < fileName.length(); i++) {
					var c = fileName.charAt(i);
					if (FILE_NAME_CHARACTERS.indexOf(c) < 0) {
						messageQueue.sendError("SAP-103: File " + fileInfo.filePath
								+ " has a file name with the invalid character \"" + c + "\" at index " + i + ".");

					}
				}

			}
			if (filePath.startsWith(COMPOSERS)) {

				int index = filePath.indexOf("/", startIndex);
				var folderName = filePath.substring(startIndex, index);
				var composer = composerList.getByFolderName(folderName);
				if (composer != null) {
					List<String> authors = composer.getAuthors();
					String[] fileAuthors = fileInfo.author.split(" & ");
					for (String fileAuthor : fileAuthors) {
						final String questionMark = " <?>";
						boolean unsafe = false;
						if (fileAuthor.endsWith(questionMark)) {
							unsafe = true;
							fileAuthor = fileAuthor.substring(0, fileAuthor.length() - questionMark.length());
						}
						if (!authors.contains(fileAuthor)) {
							messageQueue.sendError("SAP-103: File " + fileInfo.filePath + " author " + fileAuthor
									+ " in " + fileInfo.author + " is differnt from all composer authors "
									+ authors.toString());

						} else {
							if (unsafe) {
								messageQueue.sendInfo("SAP-104: File " + fileInfo.filePath + " author " + fileAuthor
										+ " in " + fileInfo.author + " is unsafe");
							}
						}
					}
				} else {
					messageQueue.sendError("SAP-105: File " + fileInfo.filePath + " has unknown composer path");
				}
			}

			for (int songIndex = 0; songIndex < fileInfo.songs; songIndex++) {
				int songNumber = songIndex + 1;
				var urlFilePath = fileInfo.getURLFilePath(songNumber);
				var production = productionList.getByURLFilePath(urlFilePath);
				if (production != null) {
					fileInfo.setDemozooID(production.id);
				} else {
					if (fileInfo.songs == 1) {
						messageQueue.sendError("SAP-106: File " + fileInfo.filePath + " has no Demozoo ID.");
					} else {
						messageQueue.sendError("SAP-107: Song number " + songNumber + " in File " + fileInfo.filePath
								+ " has no Demozoo ID.");
					}
				}
			}

			// TODO Groups
		}

	}
}
