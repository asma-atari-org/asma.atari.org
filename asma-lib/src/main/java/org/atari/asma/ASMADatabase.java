package org.atari.asma;

import java.io.Writer;
import java.io.IOException;

import org.atari.asma.demozoo.ASMAProductionList;
import org.atari.asma.util.JSONWriter;

public class ASMADatabase {

	public ComposerList composerList;
	public GroupList groupList;
	public ASMAProductionList productionList;
	public FileInfoList fileInfoList;

	// Write the database as JavaScript file "asmadb.js" for the website.
	public void writeJS(Writer fileWriter) throws IOException {
		fileWriter.write("const asma = ");
		writeJSON(fileWriter);
	}

	// Write the database as plain JSON file "asmadb.json" for API consumers.
	public void writeJSON(Writer fileWriter) throws IOException {
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
	}

	public Object getComposerOrGroup(String folderPath) {
		String prefix = "Composers/";
		if (folderPath.startsWith(prefix)) {
			return composerList.getByFolderName(folderPath.substring(prefix.length()));
		}
		prefix = "Groups/";
		if (folderPath.startsWith(prefix)) {
			return groupList.getByFolderName(folderPath.substring(prefix.length()));
		}
		return null;
	}
}
