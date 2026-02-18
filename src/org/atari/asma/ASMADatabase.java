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

	public void write(Writer fileWriter) throws IOException {
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
	}
}
