package org.atari.asma.sap;

import java.io.File;

public interface SAPFileProcessor {
	public void processFile(File inputFile);

	public void closeFile(File inputFile);

	public void processFolder(File folder);
}
