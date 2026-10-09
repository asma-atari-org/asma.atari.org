package org.atari.asma.demozoo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.TreeSet;

import org.atari.asma.demozoo.model.Link;
import org.atari.asma.util.MessageQueue;

public class LinkUtility {

	public static String[] getFileExtensions(Link[] links, MessageQueue messageQueue) {
		var set = retrieveFileExtensionsSet(links, false, messageQueue);
		if (!set.contains("sap") && !set.contains("ttt")) {
			set = retrieveFileExtensionsSet(links, true, messageQueue);
		}
		return (String[]) set.toArray(new String[set.size()]);
	}

	private static Set<String> retrieveFileExtensionsSet(Link[] links, boolean resolveZip, MessageQueue messageQeue) {
		Set<String> result = new TreeSet<String>();
		for (var link : links) {

			try {
				var uri = new URI(link.url);
				var fileExtension = DemozooFileUtility.getNormalizedFileExtension(uri.getPath());
				if (!fileExtension.isEmpty()) {

					result.add(fileExtension);
				}

				if (resolveZip) {
					if (fileExtension.equals("zip")) {
						var innerExtensions = ZipUtility.getZIPFileContentFileExtensions(link.url, messageQeue);
						result.addAll(innerExtensions);
					}
				}
			} catch (URISyntaxException ex) {

			}

		}
		return result;
	}
}
