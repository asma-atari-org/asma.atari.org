package org.atari.asma;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.atari.asma.ASMAExporter.FileExtension;
import org.atari.asma.demozoo.ProductionList;
import org.atari.asma.util.FileUtility;
import org.atari.asma.util.JSONWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileInfoList {

	private List<FileInfo> fileInfoList;
	private Gson gson;

	public FileInfoList() {
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

	public void scanFolder(File sourceFolder) {
		System.out.println("Scanning " + sourceFolder.getAbsolutePath());

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

		System.out.println(fileList.size() + " matching files found.");

		int index = sourceFolder.getAbsolutePath().length() + 1;
		for (File file : fileList) {
			String filePath = file.getAbsolutePath().substring(index);
			filePath = filePath.replace(File.separator, "/");
			var songInfo = readFile(file, filePath);
			fileInfoList.add(songInfo);
		}
	}

	private FileInfo readFile(File file, String filePath) {

		// Read binary from the input stream.
		byte[] module = FileUtility.readAsByteArray(file);

		FileInfo fileInfo = new FileInfo();
		fileInfo.filePath = filePath;
		fileInfo.fileSize = file.length();

		if (filePath.toLowerCase().endsWith(FileExtension.SAP)) {
			fileInfo.hardware = FileInfo.ATARI800;
			fileInfo.readFromSAPFile(filePath, module);
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
			fileInfo.defaultSong = 0;

		} else {
			throw new IllegalArgumentException("Unsupport file extension in " + filePath);
		}

		return fileInfo;
	}

	public void checkFiles(ComposerList composerList, ProductionList productionList) {
		final String COMPOSERS = "Composers/";
		int startIndex = COMPOSERS.length();
		for (var fileInfo : fileInfoList) {
			String filePath = fileInfo.filePath;
			if (filePath.startsWith(COMPOSERS)) {

				int index = filePath.indexOf("/", startIndex);
				String folderName = filePath.substring(startIndex, index);
				var composer = composerList.getByFolderName(folderName);
				if (composer != null) {
					List<String> authors = composer.getAuthors();
					String[] fileAuthors = fileInfo.author.split(" & ");
					for (String fileAuthor : fileAuthors) {
						final String questionMark = " <?>";
						 boolean unsafe = false;
						if (fileAuthor.endsWith(questionMark)){
							unsafe = true;
							fileAuthor=fileAuthor.substring(0,fileAuthor.length()-questionMark.length());
						}
						if (!authors.contains(fileAuthor)) {
							System.err.println("File " + fileInfo.filePath + " author " + fileAuthor + " in "+fileInfo.author
									+ " is differnt from all composer authors " + authors.toString());

						} else {
							if (unsafe) {
								System.out.println("File " + fileInfo.filePath + " author " + fileAuthor + " in "+fileInfo.author
										+ " is unsafe");
							}
						}
					}
				} else {
					System.err.println("File " + fileInfo.filePath + " has unknown composer path");
				}
			}
			// TODO Groups
		}

	}
}
