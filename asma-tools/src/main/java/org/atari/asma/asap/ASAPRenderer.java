package org.atari.asma.asap;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import org.atari.asma.util.FileUtility;

import net.sf.asap.PokeyStates;
import net.sf.asap.PokeyStatesList;

public class ASAPRenderer {

	private static void log(String message) {
		System.out.println(message);
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("USAGE: <inputfile_1> ... <inputfile_n>");
		}
		boolean open = (args.length == 1);
		for (int i = 0; i < args.length; i++) {
			File inputFile = new File(args[i]);
			inputFile = inputFile.getAbsoluteFile();
			processFile(inputFile, open);
		}
	}

	private static void processFile(File inputFile, boolean open) {

		byte[] content = null;
		try {
			content = Files.readAllBytes(inputFile.toPath());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		File outputFile;
		SAPRFile saprInputFile;
		var saprFileLogic = new SAPRFileLogic();
		if (FileUtility.getFileExtension(inputFile.getName().toLowerCase()).equals(".sapr")) {

			try {
				saprInputFile = saprFileLogic.loadSAPR(inputFile, content);
			} catch (IOException e) {
				log("ERROR: Error for "+inputFile.getAbsolutePath());
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			log("INFO: Loaded SAP-R " + saprInputFile.toString() + " with " + saprInputFile.statesList.size()
					+ " frames.");

			outputFile = FileUtility.changeFileExtension(inputFile, "-trimmed.sapr");
		} else {
			log("INFO: Rendering " + inputFile.getAbsolutePath() + " to SAP-R.");
			try {

				saprInputFile = saprFileLogic.loadASAP(inputFile, content, 145);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			outputFile = FileUtility.changeFileExtension(inputFile, ".sapr");

		}

		var renderResult = trim(saprInputFile.statesList);

		var saprFile = new SAPRFile();
		saprFile.setStatesList(renderResult.statesList);

		log("INFO: Saving " + outputFile.getAbsolutePath() + " with " + renderResult.statesList.size() + " frames in "
				+ (saprFile.stereo ? "STEREO" : "MONO") + ".");
		if (renderResult.loopIndex >= 0) {
			log("INFO: Song loops a frame " + renderResult.loopIndex + ".");
		}
		try {
			OutputStream os = new FileOutputStream(outputFile);
			saprFile.write(os);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		if (open) {
			log("INFO: Output file created with " + outputFile.length() + " bytes.");
			try {
				Desktop.getDesktop().open(outputFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}

	}

	public static class ASAPRenderResult {
		public PokeyStatesList statesList;
		public int loopIndex;
	}

	private static ASAPRenderResult trim(PokeyStatesList statesList) {
		ASAPRenderResult result = new ASAPRenderResult();
		result.statesList = null;
		result.loopIndex = -1;

		int size = statesList.size();
		final int extra = 1;
		boolean stereo = false;
		if (statesList.size() > 1) {
			for (int i = 0; i < size; i++) {
				var frame = statesList.get(i);
				if (!frame.get(extra).isSilent()) {
					stereo = true;
					break;
				}
			}
			if (!stereo) {
				log("INFO: Extra POKEY is not used. Conversion to MONO is possible.");

			}
		}
		if (statesList.getPokeys() > 1 && !stereo) {
			log("INFO: Converting STEREO to MONO.");

			var tempStatesList = new PokeyStatesList();
			tempStatesList.init(1);
			for (int i = 0; i < size; i++) {
				var states = new PokeyStates();
				states.init(1);
				states.get(0).fillFrom(statesList.get(i).get(0));
				tempStatesList.add(states);
			}
			statesList = tempStatesList;
		}

		// Skip silent frames a the start.
		int startIndex = 0;
		for (; startIndex < size; startIndex++) {
			if (!statesList.get(startIndex).isSilent()) {
				break;
			}
		}
		int endIndex = size - 1;
		for (; endIndex > startIndex; endIndex--) {
			if (!statesList.get(endIndex).isSilent()) {
				break;
			}
		}
		log("INFO: Non-Silent frames [" + startIndex + "," + endIndex + "] of [0," + (size - 1) + "].");

		boolean complete = false;
		for (int i = startIndex; i < endIndex && !complete; i++) {
			var one = statesList.get(i);
			for (int j = i + 1; j < endIndex && !complete; j++) {
				if (statesList.get(j).contentEquals(one)) {
					log("INFO: Matching frames [" + i + "] and [" + j + "].");
					int i1 = i;
					boolean match = true;
					int k = j;
					for ( ; k < endIndex; k++) {
						if (!statesList.get(k).contentEquals(statesList.get(i1))) {
							match = false;
							break;
						}
						i1++;
						if (i1 == j) { // Wrap start index to beginning of block
							i1 = i;
						}

					}
					if (match) {
						double times = 1;
						int blockLength = j - i;
						int repeatLength = endIndex + 1 - j;
						times = ((double) repeatLength) / blockLength;
						log("INFO: Matching " + blockLength + " frames [" + i + "," + (j - 1) + "] are repeated as "
								+ repeatLength + " frames " + times + " times from [" + j + "," + endIndex + "].");
						endIndex = j - 1;
						result.loopIndex = i;
						complete = true;
					} else {
						log("INFO: Match breaks at [" + i1 + "] and [" + k + "].");
					}

				}
			}
		}

		result.statesList=new PokeyStatesList();
		result.statesList.init(statesList.getPokeys());
		for (int i = startIndex; i <= endIndex; i++) {
			result.statesList.add(statesList.get(i));
		}
		return result;
	}

}
