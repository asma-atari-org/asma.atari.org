package org.atari.asma.demozoo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.atari.asma.util.MessageQueue;

public class ZipUtility {

	public static Set<String> getZIPFileContentFileExtensions(String url, MessageQueue messageQueue) {

		Set<String> result = new TreeSet<String>();

		InputStream is;
		try {
			is = new URL(url).openConnection().getInputStream();

			var zis = new ZipInputStream(is);
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				var extension = DemozooFileUtility.getNormalizedFileExtension(entry.getName());
				if (!extension.isEmpty()) {
					result.add(extension);
				}
			}
		} catch (IOException e) {
			result.add("ERROR: " + e.getMessage());
		} catch (RuntimeException e) {
			result.add("ERROR: " + e.getMessage());
		}
		messageQueue.sendInfo("DMO-112", "Resolved " + result + " from " + url + ".");
		return result;
	}
}
