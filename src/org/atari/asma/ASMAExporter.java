package org.atari.asma;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

import org.atari.asma.demozoo.ASMAProductionList;
import org.atari.asma.demozoo.Demozoo;
import org.atari.asma.demozoo.model.Database;
import org.atari.asma.sap.SAPFileLogic;
import org.atari.asma.util.FileUtility;
import org.atari.asma.util.MemoryUtility;
import org.atari.asma.util.MessageQueue;
import org.atari.asma.util.MessageQueueFactory;

// Read, check and export all relevant data as "asmadb.js" file.
// TODO: Read the initial file revision from the SVN SQLLite database file using from https://github.com/xerial/sqlite-jdbc
public class ASMAExporter {

	public static final class FileExtension {
		public static final String SAP = ".sap";
		public static final String TTT = ".ttt";
	}

	public static void main(String[] args) {
		new ASMAExporter().run(args);
	}

	private ASMAExporter() {
	}

	private void exportJS(ASMADatabase asmaDatabase, File asmaDatabaseFile, MessageQueue messageQueue) {
		messageQueue.sendInfo("Exporting " + asmaDatabase.fileInfoList.getEntries().size() + " file infos to "
				+ asmaDatabaseFile.getPath() + ".");

		Writer writer = null;
		try {
			writer = new FileWriter(asmaDatabaseFile, Charset.forName("UTF8"));
			asmaDatabase.write(writer);

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		messageQueue.sendInfo("ASMA database exported as JS to " + asmaDatabaseFile.getAbsolutePath() + " with "
				+ MemoryUtility.getRoundedMemorySize(asmaDatabaseFile.length()) + ".");
	}

	@SuppressWarnings("static-method")
	private void exportHTML(ASMADatabase asmaDatabase, Database database, File htmlFile, MessageQueue messageQueue) {
		Writer writer = null;
		try {
			writer = new FileWriter(htmlFile, Charset.forName("UTF8"));
			var table = new HTMLTable(writer);
			table.beginTable();
			table.beginRow();
			table.writeHeaderTextCell("FilePath");
			table.writeHeaderTextCell("Title");
			table.writeHeaderTextCell("Author");
			table.writeHeaderTextCell("Date");
			table.writeHeaderTextCell("Channels");
			table.writeHeaderTextCell("Format");
			table.writeHeaderTextCell("Songs");
			table.writeHeaderTextCell("Demozoo ID");
			table.writeHeaderTextCell("Demozoo Title");
			table.writeHeaderTextCell("Demozoo Author");
			table.writeHeaderTextCell("Messages");
			table.endRow();

			for (var fileInfo : asmaDatabase.fileInfoList.getEntries()) {
				table.beginRow();
				table.writeTextCell(fileInfo.filePath);
				table.writeTextCell(fileInfo.title);
				table.writeTextCell(fileInfo.author);
				table.writeTextCell(fileInfo.date);
				table.writeLongCell(fileInfo.channels);
				table.writeTextCell(fileInfo.originalModuleExt);
				table.writeLongCell(fileInfo.songs);
				if (fileInfo.getDemozooID() > 0) {
					final var link = "https://demozoo.org/music/" + fileInfo.getDemozooID();
					final var demozooID = fileInfo.getDemozooID();
					table.writeLongCell(demozooID, link);

					final var asmaProduction = asmaDatabase.productionList.getByID(demozooID);
					table.writeTextCell(asmaProduction.title);

				} else {
					table.writeTextCell("TODO: Missing");
					table.writeTextCell("");
				}
				var htmlBuilder = new StringBuffer();
				var entries = fileInfo.getMessageQueue().getEntries();
				for (var entry : entries) {
					htmlBuilder.append(entry.getType().toString() + ":" + entry.getMessage() + "<br>");

				}
				table.writeHTMLCell(htmlBuilder.toString());
				table.endRow();
			}
			table.endTable();
			writer.close();

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		messageQueue.sendInfo("ASMA database exported as HTML to " + htmlFile.getAbsolutePath() + " with "
				+ MemoryUtility.getRoundedMemorySize(htmlFile.length()) + ".");

	}

	@SuppressWarnings("static-method")
	private void run(String[] args) {
		MessageQueue messageQueue = MessageQueueFactory.createSystemInstance();

		if (args.length != 2) {
			messageQueue.sendInfo("Usage: ASMAExporter <trunk/asma folder> <asmadb.js file>");
			return;
		}

		var sourceFolderPath = args[0];
		File sourceFolder = new File(sourceFolderPath);

		File stilFile = new File(sourceFolder, ASMAPaths.STIL_TXT);
		messageQueue.sendInfo("Loading STIL information '" + stilFile.getPath() + "'.");
		STIL stil = new STIL();
		try {
			stil.load(stilFile);
		} catch (IOException ex) {
			messageQueue.sendError("Cannot read STIL file: " + ex.getMessage());
			return;
		}
		messageQueue.sendInfo(stil.getSize() + " STIL entries loaded.");

		var asmaDatabase = new ASMADatabase();

		var composersFile = new File(sourceFolder, ASMAPaths.ASMA_COMPOSERS_JSON);
		messageQueue.sendInfo("Loading composers from '" + composersFile.getPath() + "'.");
		var composerList = ComposerList.load(composersFile);
		composerList.deserialize();
		composerList.init(messageQueue);
		messageQueue.sendInfo(composerList.getEntries().size() + " composers loaded.");
		composerList.checkFolders(sourceFolder, messageQueue);

		asmaDatabase.composerList = composerList;

		var groupsFile = new File(sourceFolder, ASMAPaths.ASMA_GROUPS_JSON);
		messageQueue.sendInfo("Loading groups from '" + composersFile.getPath() + "'.");
		var groupList = GroupList.load(groupsFile);
		groupList.deserialize();

		messageQueue.sendInfo(groupList.getEntries().size() + " groups loaded.");
		groupList.init(messageQueue);
		groupList.checkFolders(sourceFolder, messageQueue);

		composerList.checkGroups(groupList, messageQueue);

		asmaDatabase.groupList = groupList;

		var databaseFile = new File(sourceFolder, ASMAPaths.DEMOZOO_DATABASE_JSON);
		messageQueue.sendInfo("Loading Demozoo database from '" + databaseFile.getPath() + "'.");
		var demozoo = new Demozoo();
		var database = demozoo.loadDatabase(databaseFile, messageQueue);
		messageQueue.sendInfo(database.productions.length + " productions loaded.");

		var productionList = new ASMAProductionList(database.productions);

		productionList.init(messageQueue);
		asmaDatabase.productionList = productionList;

		FileInfoList fileInfoList = new FileInfoList(stil, new SAPFileLogic());
		var maxFiles = 1000;
		fileInfoList.scanFolder(sourceFolder, messageQueue, maxFiles);
		fileInfoList.checkFiles(composerList, productionList);

		asmaDatabase.fileInfoList = fileInfoList;

		var asmaDatabaseFile = new File(args[1]);
		exportJS(asmaDatabase, asmaDatabaseFile, messageQueue);

		var htmlFile = FileUtility.changeFileExtension(asmaDatabaseFile, ".html");
		exportHTML(asmaDatabase, database, htmlFile, messageQueue);

		messageQueue.printSummary();

	}

}
