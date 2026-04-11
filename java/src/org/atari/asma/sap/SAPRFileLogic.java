package org.atari.asma.sap;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.atari.asma.SAPRFile;

import net.sf.asap.ASAP;
import net.sf.asap.ASAPArgumentException;
import net.sf.asap.ASAPFormatException;
import net.sf.asap.PokeyStatesList;

public class SAPRFileLogic {

	public SAPRFile loadASAP(File inputFile, byte[] content, int seconds) throws IOException {

		var saprFile = new SAPRFile();
		var fps = -1;
		var asap = new ASAP();
		try {
			asap.load(inputFile.getName(), content, content.length);
			var info = asap.getInfo();
			saprFile.stereo = (info.getChannels() > 1);
			fps = info.isNtsc() ? 60 : 50; // TODO: Handle Fastplay
		} catch (ASAPFormatException e) {
			throw new IOException(e.getMessage());
		}
		try {
			asap.playSong(0, -1);
		} catch (ASAPArgumentException e) {
			throw new IOException(e.getMessage());
		} catch (ASAPFormatException e) {
			throw new IOException(e.getMessage());

		}

		int frames = fps * seconds;
		for (int i = 0; i < frames; i++) {
			asap.doFrameDump(saprFile.statesList.statesList);
		}
		return saprFile;

	}

	public SAPRFile loadSAPR(File inputFile, byte[] content) throws IOException {

		var result = new SAPRFile();

		InputStream is = new ByteArrayInputStream(content);
		boolean text = true;
		List<String> lines = new ArrayList<String>();
		var builder = new StringBuilder();
		int length = 0;
		while (text) {
			int c = is.read();

			if (c < 0) {
				break;
			}
			builder.append((char) c);
			var line = builder.toString();
			if (line.endsWith(SAPRFile.CRLF)) {
				var currentLine = line.substring(0, line.length() - 2);

				if (currentLine.equals("STEREO")) {
					result.stereo = true;
				}
				if (currentLine.equals("NTSC")) {
					result.ntsc = true;
				}
				builder.setLength(0);
				if (currentLine.isEmpty()) {
					text = false;
				} else {
					lines.add(currentLine);
				}
			}
			length++;
			if (length == 5 && !line.equals(SAPRFile.HEADER)) {
				throw new IOException("Not a SAP file. Magic bytes are "+line+".");
			}
		}
		result.statesList = new PokeyStatesList(result.stereo ? 2 : 1);
		result.statesList.read(is);

		// log("INFO: Loaded SAP-R " + lines + " with " +
		// result.frameList.size() + " lines.");
		return result;
	}
}