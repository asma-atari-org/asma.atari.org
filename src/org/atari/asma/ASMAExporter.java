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
			var htmlWriter = new HTMLWriter(writer);
			var htmlExporter = new ASMAHTMLExporter(htmlWriter);
			htmlExporter.write(asmaDatabase, database, true);
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

		productionList.init();
		asmaDatabase.productionList = productionList;

		FileInfoList fileInfoList = new FileInfoList(stil, new SAPFileLogic());
		var maxFiles = Integer.MAX_VALUE;
		// maxFiles = 10;
		fileInfoList.scanFolder(sourceFolder, messageQueue, maxFiles);
		fileInfoList.checkFileInfos(composerList, productionList);

		productionList.checkReferences(fileInfoList);

		asmaDatabase.fileInfoList = fileInfoList;

		var asmaDatabaseFile = new File(args[1]);
		exportJS(asmaDatabase, asmaDatabaseFile, messageQueue);

		var htmlFile = FileUtility.changeFileExtension(asmaDatabaseFile, ".html");
		exportHTML(asmaDatabase, database, htmlFile, messageQueue);

		messageQueue.printSummary();

	}

}
