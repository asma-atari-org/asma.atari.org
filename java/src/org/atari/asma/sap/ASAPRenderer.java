package org.atari.asma.sap;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import org.atari.asma.SAPRFile;
import org.atari.asma.util.FileUtility;

import net.sf.asap.ASAP;
import net.sf.asap.ASAPArgumentException;
import net.sf.asap.ASAPFormatException;
import net.sf.asap.PokeyPairFrame;

public class ASAPRenderer {
	private static int FPS = 50;

	public static void main(String[] args) {

		ASAP asap = new ASAP();
		File inputFile = new java.io.File(
				"C:\\jac\\system\\WWW\\Sites\\asma.atari.org\\java\\test\\RMT\\Raster\\delta.rmt");
		byte[] content = null;
		try {
			content = Files.readAllBytes(inputFile.toPath());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		try {
			asap.load(inputFile.getName(), content, content.length);
		} catch (ASAPFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		try {
			asap.playSong(0, -1);
		} catch (ASAPArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ASAPFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File outputFile = FileUtility.changeFileExtension(inputFile, ".sapr");
		System.out.println(
				"INFO: Rendering " + inputFile.getAbsolutePath() + " to " + outputFile.getAbsolutePath() + ".");
		int seconds = 145;
		int frames = FPS * seconds;
		var saprFile = new SAPRFile();
		var frameList = new ArrayList<PokeyPairFrame>();
		for (int i = 0; i < frames; i++) {
			try {
				asap.doFrameDump(frameList);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		var bos = new ByteArrayOutputStream();
		for (PokeyPairFrame frame : frameList) {
			try {
				frame.write(bos);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		saprFile.setContent(bos.toByteArray());

		try {
			OutputStream os = new FileOutputStream(outputFile);
			saprFile.write(os);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("INFO: Output file created with " + outputFile.length() + " bytes.");
		try {
			Desktop.getDesktop().open(outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

	}

}
