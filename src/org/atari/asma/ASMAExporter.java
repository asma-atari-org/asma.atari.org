package org.atari.asma;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import org.atari.asma.demozoo.ProductionList;
import org.atari.asma.util.JSONWriter;
import org.atari.asma.util.MessageQueue;

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
		MessageQueue messageQueue = new MessageQueue(System.out, System.err);

		if (args.length != 2) {
			messageQueue.sendMessage("Usage: ASMAExporter <trunk/asma folder> <asmadb.js file>");
			return;
		}
		var sourceFolderPath = args[0];
		File sourceFolder = new File(sourceFolderPath);

		File composersFile = new File(sourceFolder, "Docs/ASMA-Composers.json");
		messageQueue.sendMessage("Loading composers from " + composersFile.getPath() + ".");
		ComposerList composerList = ComposerList.load(composersFile);
		composerList.deserialize();
		composerList.init(messageQueue);
		messageQueue.sendMessage(composerList.getEntries().size() + " composers loaded.");
		composerList.checkFolders(sourceFolder, messageQueue);

		File groupsFile = new File(sourceFolder, "Docs/ASMA-Groups.json");
		messageQueue.sendMessage("Loading groups from " + composersFile.getPath() + ".");
		GroupList groupList = GroupList.load(groupsFile);
		groupList.deserialize();

		messageQueue.sendMessage(groupList.getEntries().size() + " groups loaded.");
		groupList.init(messageQueue);
		groupList.checkFolders(sourceFolder, messageQueue);

		composerList.checkGroups(groupList, messageQueue);

		File productionsFile = new File(sourceFolder, "Docs/Demozoo-Productions.json");
		messageQueue.sendMessage("Loading Demozoo productions from " + composersFile.getPath() + ".");
		ProductionList productionList = ProductionList.load(productionsFile);
		productionList.deserialize();
		messageQueue.sendMessage(productionList.getEntries().size() + " productions loaded.");

		productionList.init(messageQueue);

		FileInfoList fileInfoList = new FileInfoList();
		fileInfoList.scanFolder(sourceFolder);
		fileInfoList.checkFiles(composerList, productionList, messageQueue);

		var exportFile = new File(args[1]);

		messageQueue.sendMessage(
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
				+ exportFile.length() + " bytes.");
		messageQueue.printSummary();

	}

}
