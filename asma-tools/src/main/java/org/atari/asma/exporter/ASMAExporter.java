package org.atari.asma.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

import org.atari.asma.ASMADatabase;
import org.atari.asma.ASMAPaths;
import org.atari.asma.ComposerList;
import org.atari.asma.FileInfoList;
import org.atari.asma.GroupList;
import org.atari.asma.STIL;
import org.atari.asma.asap.ASAPFileLogic;
import org.atari.asma.demozoo.ASMAProductionList;
import org.atari.asma.demozoo.Demozoo;
import org.atari.asma.demozoo.model.Database;
import org.atari.asma.util.FileUtility;
import org.atari.asma.util.MemoryUtility;
import org.atari.asma.util.MessageQueueUtility;
import org.atari.asma.util.StringUtility;

import com.wudsn.tools.base.common.MessageQueue;

// Read, check and export all relevant data as "asmadb.js" and "asmadb.json" files.
// TODO: Read the initial file revision from the SVN SQLLite database file using from https://github.com/xerial/sqlite-jdbc
public class ASMAExporter {

	public static void main(String[] args) {

		new ASMAExporter().run(args);
	}

	private ASMAExporter() {
	}

	@SuppressWarnings("static-method")
	private void exportDatabase(ASMADatabase asmaDatabase, File asmaDatabaseFile, MessageQueue messageQueue) {
		messageQueue.sendMessage(this, null, Messages.I310, String.valueOf(asmaDatabase.fileInfoList.getEntries().size()),
				asmaDatabaseFile.getPath());

		Writer writer = null;
		try {
			writer = new FileWriter(asmaDatabaseFile, Charset.forName("UTF8"));
			if (asmaDatabaseFile.getName().endsWith(".json")) {
				asmaDatabase.writeJSON(writer);
			} else {
				asmaDatabase.writeJS(writer);
			}

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
		messageQueue.sendMessage(this, null, Messages.I311, asmaDatabaseFile.getAbsolutePath(),
				MemoryUtility.getRoundedMemorySize(asmaDatabaseFile.length()));
	}

	@SuppressWarnings("static-method")
	private void exportHTML(ASMADatabase asmaDatabase, Database database, File htmlFile, String basePath,
			MessageQueue messageQueue) {
		Writer writer = null;
		try {
			writer = new FileWriter(htmlFile, Charset.forName("UTF8"));
			var htmlWriter = new HTMLWriter(writer);
			var htmlExporter = new ASMAHTMLExporter(htmlWriter);
			htmlExporter.write(asmaDatabase, database, true, basePath);
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

		messageQueue.sendMessage(this, null, Messages.I312, htmlFile.getAbsolutePath(),
				MemoryUtility.getRoundedMemorySize(htmlFile.length()));

	}

	@SuppressWarnings("static-method")
	private void run(String[] args) {
		MessageQueue messageQueue = MessageQueueUtility.createConsoleInstance();

		if (args.length != 2) {
			System.out.println("Usage: ASMAExporter <trunk/asma folder> <asmadb.js file>");
			return;
		}

		var sourceFolderPath = args[0];
		File sourceFolder = new File(sourceFolderPath);

		File stilFile = new File(sourceFolder, ASMAPaths.STIL_TXT);
		messageQueue.sendMessage(this, null, Messages.I302, stilFile.getPath());
		STIL stil = new STIL();
		try {
			stil.load(stilFile);
		} catch (IOException ex) {
			messageQueue.sendMessage(this, null, Messages.E301, ex.getMessage());
			return;
		}
		messageQueue.sendMessage(this, null, Messages.I303, String.valueOf(stil.getSize()));

		var asmaDatabase = new ASMADatabase();

		var composersFile = new File(sourceFolder, ASMAPaths.ASMA_COMPOSERS_JSON);
		messageQueue.sendMessage(this, null, Messages.I304, composersFile.getPath());
		var composerList = ComposerList.load(composersFile);
		composerList.deserialize();
		composerList.init(messageQueue);
		messageQueue.sendMessage(this, null, Messages.I305, String.valueOf(composerList.getEntries().size()));
		composerList.checkFolders(sourceFolder, messageQueue);

		asmaDatabase.composerList = composerList;

		var groupsFile = new File(sourceFolder, ASMAPaths.ASMA_GROUPS_JSON);
		messageQueue.sendMessage(this, null, Messages.I306, groupsFile.getPath());
		var groupList = GroupList.load(groupsFile);
		groupList.deserialize();

		messageQueue.sendMessage(this, null, Messages.I307, String.valueOf(groupList.getEntries().size()));
		groupList.init(messageQueue);
		groupList.checkFolders(sourceFolder, messageQueue);

		composerList.checkGroups(groupList, messageQueue);

		asmaDatabase.groupList = groupList;

		var databaseFile = new File(sourceFolder, ASMAPaths.DEMOZOO_DATABASE_JSON);
		messageQueue.sendMessage(this, null, Messages.I308, databaseFile.getPath());
		var demozoo = new Demozoo();
		var database = demozoo.loadDatabase(databaseFile, messageQueue);
		messageQueue.sendMessage(this, null, Messages.I309, String.valueOf(database.productions.length));

		var productionList = new ASMAProductionList(database.productions);

		productionList.init();
		asmaDatabase.productionList = productionList;

		var fileInfoScanner = new FileInfoScanner(stil, new ASAPFileLogic());
		var maxFiles = Integer.MAX_VALUE;
		// maxFiles = 10;
		FileInfoList fileInfoList = fileInfoScanner.scanFolder(sourceFolder, messageQueue, maxFiles);
		fileInfoList.checkFileInfos(composerList, productionList);

		productionList.checkReferences(fileInfoList);

		asmaDatabase.fileInfoList = fileInfoList;

		var asmaDatabaseFile = new File(args[1]);
		exportDatabase(asmaDatabase, asmaDatabaseFile, messageQueue);

		// Also export as plain JSON for API consumers like the ASMAClient.
		var asmaDatabaseJSONFile = FileUtility.changeFileExtension(asmaDatabaseFile, ".json");
		exportDatabase(asmaDatabase, asmaDatabaseJSONFile, messageQueue);

		var htmlFile = FileUtility.changeFileExtension(asmaDatabaseFile, ".html");
		exportHTML(asmaDatabase, database, htmlFile, "", messageQueue);

		var dateString = StringUtility.getToday();

		final var baseName = "history";
		exportHTML(asmaDatabase, database, htmlFile, "", messageQueue);
		var htmlHistoryFolder = new File(htmlFile.getParentFile(), baseName);

		var htmlHistoryFile = new File(htmlHistoryFolder,
				FileUtility.changeFileExtension(asmaDatabaseFile, "-" + dateString + ".html").getName());
		exportHTML(asmaDatabase, database, htmlHistoryFile, "../", messageQueue);

		System.out.println(MessageQueueUtility.getSummary(messageQueue));

	}

}
