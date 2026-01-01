package org.atari.asma;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import org.atari.asma.demozoo.ASMAProductionList;
import org.atari.asma.demozoo.Demozoo;
import org.atari.asma.sap.SAPFileLogic;
import org.atari.asma.util.MessageQueueFactory;
import org.atari.asma.util.JSONWriter;
import org.atari.asma.util.MemoryUtility;
import org.atari.asma.util.MessageQueue;

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

	@SuppressWarnings("static-method")
	private void run(String[] args) {
		MessageQueue messageQueue = MessageQueueFactory.createSystemInstance();

		if (args.length != 2) {
			messageQueue.sendMessage("Usage: ASMAExporter <trunk/asma folder> <asmadb.js file>");
			return;
		}

		var sourceFolderPath = args[0];
		File sourceFolder = new File(sourceFolderPath);

		File stilFile = new File(sourceFolder, "Docs/STIL.txt");
		messageQueue.sendInfo("Loading STIL information '" + stilFile.getPath() + "'.");
		STIL stil = new STIL();
		try {
			stil.load(stilFile);
		} catch (IOException ex) {
			messageQueue.sendError("Cannot read STIL file: " + ex.getMessage());
			return;
		}
		messageQueue.sendInfo(stil.getSize() + " STIL entries loaded.");

		File composersFile = new File(sourceFolder, "Docs/ASMA-Composers.json");
		messageQueue.sendInfo("Loading composers from '" + composersFile.getPath() + "'.");
		ComposerList composerList = ComposerList.load(composersFile);
		composerList.deserialize();
		composerList.init(messageQueue);
		messageQueue.sendInfo(composerList.getEntries().size() + " composers loaded.");
		composerList.checkFolders(sourceFolder, messageQueue);

		var groupsFile = new File(sourceFolder, "Docs/ASMA-Groups.json");
		messageQueue.sendInfo("Loading groups from '" + composersFile.getPath() + "'.");
		GroupList groupList = GroupList.load(groupsFile);
		groupList.deserialize();

		messageQueue.sendInfo(groupList.getEntries().size() + " groups loaded.");
		groupList.init(messageQueue);
		groupList.checkFolders(sourceFolder, messageQueue);

		composerList.checkGroups(groupList, messageQueue);

		var databaseFile = new File(sourceFolder, ASMAPaths.DEMOZOO_DATABASE_JSON);
		messageQueue.sendInfo("Loading Demozoo database from '" + databaseFile.getPath() + "'.");
		var demozoo = new Demozoo();
		var database = demozoo.loadDatabase(databaseFile, messageQueue);
		messageQueue.sendInfo(database.productions.length + " productions loaded.");

		var productionList = new ASMAProductionList(database.productions);

		productionList.init(messageQueue);

		FileInfoList fileInfoList = new FileInfoList(stil, new SAPFileLogic());
		fileInfoList.scanFolder(sourceFolder, messageQueue);
		fileInfoList.checkFiles(composerList, productionList, messageQueue);

		var exportFile = new File(args[1]);

		messageQueue.sendInfo(
				"Exporting " + fileInfoList.getEntries().size() + " song infos to " + exportFile.getPath() + ".");

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(exportFile, Charset.forName("UTF8"));
			fileWriter.write("const asma = ");
			JSONWriter writer = new JSONWriter(fileWriter);
			writer.beginObject();
			writer.writeAttribute("composerInfos");
			fileWriter.write(composerList.getJSONString());
			writer.writeSeparator();

			writer.writeAttribute("groupInfos");
			fileWriter.write(groupList.getJSONString());
			writer.writeSeparator();

			writer.writeAttribute("fileInfos");
			fileInfoList.exportToJSON(writer);
			writer.writeSeparator();

			writer.writeAttribute("demozoo");
			writer.beginObject();
			writer.writeAttribute("productions");
			fileWriter.write(productionList.getJSONString());
			writer.endObject();

			writer.endObject();

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		}
		messageQueue.sendMessage("ASMA exported to " + exportFile.getAbsolutePath() + " completed with "
				+ MemoryUtility.getRoundedMemorySize(exportFile.length()) + ".");
		messageQueue.printSummary();

	}

}
