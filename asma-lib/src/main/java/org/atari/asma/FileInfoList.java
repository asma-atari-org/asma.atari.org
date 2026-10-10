package org.atari.asma;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.atari.asma.demozoo.ASMAProductionList;
import org.atari.asma.util.JSONWriter;

public class FileInfoList {

	private List<FileInfo> fileInfoList;

	public FileInfoList() {
		fileInfoList = new ArrayList<FileInfo>();
	}

	public void add(FileInfo fileInfo) {
		if (fileInfo == null) {
			throw new IllegalArgumentException("Parameter fileInfo must not be null.");
		}
		fileInfoList.add(fileInfo);
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

	// Checks a date against the ASMA date patterns: DD/MM/YYYY (preferred),
	// DD-DD/MM/YYYY, MM/YYYY, MM-MM/YYYY, YYYY, YYYY-YYYY, each optionally
	// followed by " <?>" for unsure dates. Throws IllegalArgumentException
	// with the reason if the date is invalid. Ported from the former check
	// in the website's file.js.
	public static void checkDate(String date) {
		final String UNSURE = " <?>";
		if (date == null || date.isEmpty()) {
			return;
		}
		if (date.endsWith(UNSURE)) {
			date = date.substring(0, date.lastIndexOf(UNSURE));
		}

		String pattern = date.replaceAll("[0-9]", "?");

		switch (pattern) {
		case "??/??/????": { // DD/MM/YYYY (preferred)
			String dayFrom = date.substring(0, 2);
			String monthFrom = date.substring(3, 5);
			String yearFrom = date.substring(6, 10);
			checkDayOfMonth(dayFrom, monthFrom, yearFrom);
			break;
		}
		case "??-??/??/????": { // DD-DD/MM/YYYY
			String dayFrom = date.substring(0, 2);
			String dayTo = date.substring(3, 5);
			if (dayFrom.compareTo(dayTo) > 0) {
				throw new IllegalArgumentException("Day-from " + dayFrom + " is after day-to " + dayTo + ".");
			}
			String monthFrom = date.substring(6, 8);
			String yearFrom = date.substring(9, 13);
			checkDayOfMonth(dayFrom, monthFrom, yearFrom);
			checkDayOfMonth(dayTo, monthFrom, yearFrom);
			break;
		}
		case "??/????": { // MM/YYYY
			String monthFrom = date.substring(0, 2);
			String yearFrom = date.substring(3, 7);
			checkMonthOfYear(monthFrom, yearFrom);
			break;
		}
		case "??-??/????": { // MM-MM/YYYY
			String monthFrom = date.substring(0, 2);
			String monthTo = date.substring(3, 5);
			if (monthFrom.compareTo(monthTo) > 0) {
				throw new IllegalArgumentException("Month-from " + monthFrom + " is after month-to " + monthTo + ".");
			}
			String yearFrom = date.substring(6, 10);
			checkMonthOfYear(monthFrom, yearFrom);
			checkMonthOfYear(monthTo, yearFrom);
			break;
		}
		case "????": { // YYYY
			checkYear(date);
			break;
		}
		case "????-????": { // YYYY-YYYY
			String yearFrom = date.substring(0, 4);
			String yearTo = date.substring(5, 9);
			if (yearFrom.compareTo(yearTo) > 0) {
				throw new IllegalArgumentException("Year-from " + yearFrom + " is after year-to " + yearTo + ".");
			}
			checkYear(yearFrom);
			checkYear(yearTo);
			break;
		}
		default:
			throw new IllegalArgumentException("Invalid pattern \"" + pattern + "\".");
		}
	}

	private static void checkDayOfMonth(String day, String month, String year) {
		checkMonthOfYear(month, year);
		int dayInt = Integer.parseInt(day);
		if (dayInt < 1 || dayInt > 31) {
			throw new IllegalArgumentException("Day " + day + " is invalid.");
		}
		// Leap years are deliberately ignored, so day 29 is always allowed.
		if (dayInt == 30 && month.equals("02")) {
			throw new IllegalArgumentException("Day " + day + " is invalid for month " + month + ".");
		}
		if (dayInt == 31 && (month.equals("02") || month.equals("04") || month.equals("06") || month.equals("09")
				|| month.equals("11"))) {
			throw new IllegalArgumentException("Day " + day + " is invalid for month " + month + ".");
		}
	}

	private static void checkMonthOfYear(String month, String year) {
		checkYear(year);
		int monthInt = Integer.parseInt(month);
		if (monthInt < 1 || monthInt > 12) {
			throw new IllegalArgumentException("Month " + month + " is invalid.");
		}
	}

	private static void checkYear(String year) {
		if (Integer.parseInt(year) > java.time.Year.now().getValue()) {
			throw new IllegalArgumentException("Year " + year + " is in the future.");
		}
	}

	public void checkFileInfos(ComposerList composerList, ASMAProductionList productionList) {
	
		// According to the STIL specification there are 9 characters for a tag must at must 90 characters for a value.
		// Therefore we limit all lines, including the file path, to 99 characters.
		final int MAX_FILE_PATH_LENGTH = 99;
		
		// From the ASMA "Sap.txt" specification.
		// The rules introduced in this chapter are not obligatory, however, they are used in the Atari SAP Music Archive.
		// The filenames must not be longer than 26 characters (plus ".sap" extension).
		// The reason was the 30 characters filename limit in Amiga's Fast File System (https://en.wikipedia.org/wiki/Amiga_Fast_File_System).
		// In 2026, we dropped that limit.
		// final int MAX_FILE_NAME_LENGTH = 26;
		final String FILE_NAME_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";
		final String FOLDER_PATH_CHARACTERS = "/"+FILE_NAME_CHARACTERS;
		
		final String COMPOSERS = "Composers/";
		int startIndex = COMPOSERS.length();

		for (var fileInfo : fileInfoList) {
			final var messageQueue = fileInfo.getMessageQueue();
			final var folderPath=fileInfo.getFolderPath();
			
			for (int i = 0; i < folderPath.length(); i++) {
				var c = folderPath.charAt(i);
				if (FOLDER_PATH_CHARACTERS.indexOf(c) < 0) {
					messageQueue.sendError("SAP-103", "File " + fileInfo.filePath
							+ " has a folder path with the invalid character \"" + c + "\" (0x"+Long.toHexString(c)+") at index " + i + ".");

				}
			}

			try {
				checkDate(fileInfo.date);
			} catch (IllegalArgumentException ex) {
				messageQueue.sendError("SAP-110",
						"File " + fileInfo.filePath + " has an invalid date \"" + fileInfo.date + "\". " + ex.getMessage());
			}

			final var filePath = fileInfo.filePath;
			if (filePath.length()>MAX_FILE_PATH_LENGTH) {
					messageQueue.sendError("SAP-109", "File " + fileInfo.filePath
							+ " has a relative file path that exceeds the allowed limit of " + MAX_FILE_PATH_LENGTH + " characters. Use a short file path.");

			}
	
			
			if (filePath.toLowerCase().endsWith(FileExtension.SAP)) {
				if (!filePath.endsWith(FileExtension.SAP)) {
					messageQueue.sendError("SAP-101",
							"File " + fileInfo.filePath + " does not have a lowercase \".sap\" file extension");
				}
				var fileName = filePath;
				var index = filePath.lastIndexOf('/');
				if (index >= 0) {
					fileName = fileName.substring(index + 1);
				}
				
				fileName = fileName.substring(0, fileName.length() - FileExtension.SAP.length());
				/*
				var length = fileName.length();
				if (length > MAX_FILE_NAME_LENGTH) {
					messageQueue.sendError("SAP-102",
							"File " + fileInfo.filePath + " has a file name with " + length + " characters. This is "
									+ (length - MAX_FILE_NAME_LENGTH)
									+ " characters more than the recommended maximum of " + MAX_FILE_NAME_LENGTH
									+ " characters. Use a shorter name.");
				}*/
				for (int i = 0; i < fileName.length(); i++) {
					var c = fileName.charAt(i);
					if (FILE_NAME_CHARACTERS.indexOf(c) < 0) {
						messageQueue.sendError("SAP-103", "File " + fileInfo.filePath
								+ " has a file name with the invalid character \"" + c + "\" (0x"+Long.toHexString(c)+") at index " + i + ".");

					}
				}


			}
			if (filePath.startsWith(COMPOSERS)) {

				int index = filePath.indexOf("/", startIndex);
				var folderName = filePath.substring(startIndex, index);
				var composer = composerList.getByFolderName(folderName);
				if (composer != null) {

					// Only for ATAR800/SAP files, the following works.
					if (fileInfo.hardware.equals(FileInfo.ATARI800)) {
						List<String> authors = composer.getAuthors();
						String[] fileAuthors = fileInfo.author.split(" & ");
						for (String fileAuthor : fileAuthors) {
							final String questionMark = " <?>";
							boolean unsafe = false;
							String trimmedFileAuthor;
							if (fileAuthor.endsWith(questionMark)) {
								unsafe = true;
								trimmedFileAuthor = fileAuthor.substring(0,
										fileAuthor.length() - questionMark.length());
								trimmedFileAuthor = trimmedFileAuthor.trim();
							} else {
								trimmedFileAuthor = fileAuthor;
							}
							// TODO: Currently deactivated
							boolean WARN_DIFFRENT_COMPOSERS=false;
							if (!authors.contains(trimmedFileAuthor)) {
								if (WARN_DIFFRENT_COMPOSERS) {
								messageQueue.sendWarning("SAP-104",
										"File " + fileInfo.filePath + " author \"" + trimmedFileAuthor + "\" in \""
												+ fileInfo.author + "\" is different from all composer authors \""
												+ authors.toString() + "\"");
								}

							} else {
								if (unsafe) {
									messageQueue.sendInfo("SAP-105", "File " + fileInfo.filePath + " author \""
											+ fileAuthor + "\" in \"" + fileInfo.author + "\" is unsafe.");
								}
							}
						}
					}
				} else {
					messageQueue.sendError("SAP-106", "File " + fileInfo.filePath + " has an unknown composer path.");
				}
			}

			int foundSongNumbers = 0;
			List<Integer> missingSongNumbers = new ArrayList<Integer>();
			for (int songIndex = 0; songIndex < fileInfo.songs; songIndex++) {
				int songNumber = songIndex + 1;
				// Check with "#<songNumber> suffix for default song
				var urlFilePath = fileInfo.getURLFilePath(songNumber, true);
				var production = productionList.getByURLFilePath(urlFilePath);
				if (production==null) {
					 // Check without "#<songNumber> suffix for default song
					 urlFilePath = fileInfo.getURLFilePath(songNumber, false);
					 production = productionList.getByURLFilePath(urlFilePath);
				}
				if (production != null) {
					fileInfo.setDemozooID(production.id);
					foundSongNumbers++;
				} else {
					if (fileInfo.songs == 1) {

						if (fileInfo.hardware.equals(FileInfo.ATARI800)) {
							messageQueue.sendError("SAP-107", "File " + fileInfo.filePath + " has no Demozoo ID.");
						}
					} else {
						missingSongNumbers.add(songNumber);

					}
				}

			}
			// Having separate productions for subsong is optional.
			// But if there are more than one URL, all songs must have URLs.
			if (fileInfo.songs > 1 && (foundSongNumbers > 1) && !missingSongNumbers.isEmpty()) {
				messageQueue.sendWarning("SAP-108", "Song numbers " + missingSongNumbers.toString() + " in File "
						+ fileInfo.filePath + " have no Demozoo ID.");

			}

			// TODO Groups
		}

	}
}
