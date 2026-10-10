package org.atari.asma.exporter;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.atari.asma.FileExtension;
import org.atari.asma.FileInfo;
import org.atari.asma.FileInfoList;
import org.atari.asma.STIL;
import org.atari.asma.STIL.STILEntry;
import org.atari.asma.TTTFile;
import org.atari.asma.asap.ASAPFile;
import org.atari.asma.asap.ASAPFileLogic;
import org.atari.asma.util.FileUtility;
import org.atari.asma.util.MessageQueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

// Scans the ASMA archive folder and builds the FileInfoList by reading
// every SAP file via ASAP and every TTT file via Gson.
public class FileInfoScanner {

	private final STIL stil;
	private final ASAPFileLogic sapFileLogic;
	private final Gson gson;

	public FileInfoScanner(STIL stil, ASAPFileLogic sapFileLogic) {
		this.stil = stil;
		this.sapFileLogic = sapFileLogic;
		gson = new GsonBuilder().create();
	}

	public FileInfoList scanFolder(File sourceFolder, MessageQueue messageQueue, int maxFiles) {
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
		List<FileInfo> fileInfos = new ArrayList<FileInfo>();
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
				synchronized (fileInfos) {
					if (fileInfo != null) {
						fileInfos.add(fileInfo);
						if (fileInfos.size() >= maxFiles) {
							cancelled.set(true);
						}
					}
				}
			}
		});
		System.out.println();

		// Parallel process destroyed the order, so sort again.
		fileInfos.sort(new Comparator<FileInfo>() {

			@Override
			public int compare(FileInfo o1, FileInfo o2) {
				return o1.filePath.compareTo(o2.filePath);
			}
		});

		// Issue which result in broken SAP file files, are reported right away.
		messageQueue.addEntries(fileListMessageQueue);

		var fileInfoList = new FileInfoList();
		for (var fileInfo : fileInfos) {
			fileInfoList.add(fileInfo);
		}
		return fileInfoList;
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
			readFromSAPFile(fileInfo, sapFile, messageQueue);

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

	// Formerly FileInfo.readFromSAPFile(ASAPFile), moved here to keep the
	// FileInfo model class free of the ASAP dependency.
	private static void readFromSAPFile(FileInfo fileInfo, ASAPFile sapFile, MessageQueue messageQueue) {

		var asapInfo = sapFile.getASAPInfo();
		fileInfo.title = asapInfo.getTitleOrFilename();
		fileInfo.author = asapInfo.getAuthor();
		fileInfo.date = asapInfo.getDate();

		// Determine the original file type in the container.
		// Only if it cannot be determined, the file extension is used.
		fileInfo.typeLetter = String.valueOf((char) asapInfo.getTypeLetter());
		fileInfo.originalModuleExt = asapInfo.getOriginalModuleExt();
		if (fileInfo.originalModuleExt == null) {
			fileInfo.originalModuleExt = fileInfo.filePath.substring(fileInfo.filePath.lastIndexOf('.') + 1);
		}

		// Translate the file type to a human readable text.
		try {
			net.sf.asap.ASAPInfo.getExtDescription(fileInfo.originalModuleExt);
		} catch (net.sf.asap.ASAPFormatException ex) {
			throw new RuntimeException(ex);
		}

		fileInfo.channels = asapInfo.getChannels();
		fileInfo.songs = asapInfo.getSongs();
		fileInfo.defaultSongIndex = asapInfo.getDefaultSong();

		// Without a TIME tag the website player cannot show a total time or a
		// seek slider, and shuffle mode falls back to a fixed maximum.
		// A warning only: endlessly looping tunes are legitimate.
		if (asapInfo.getDuration(asapInfo.getDefaultSong()) <= 0) {
			messageQueue.sendWarning("SAP-111",
					"File " + fileInfo.filePath + " has no TIME tag for the default song.");
		}

		fileInfo.setDemozooID(0);
	}
}
