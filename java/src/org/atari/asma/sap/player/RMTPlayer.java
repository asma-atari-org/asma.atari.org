package org.atari.asma.sap.player;

import java.util.ArrayList;
import java.util.List;

import org.atari.asma.sap.ASAPFile;
import org.atari.asma.sap.SegmentList;
import org.atari.asma.util.MessageQueue;
import org.atari.asma.util.StringUtility;

public abstract class RMTPlayer extends Player {

	private static String getProposedDate(String proposedTitle) {
		var firstIndex = proposedTitle.indexOf("/");
		if (firstIndex >= 1) {
			var secondIndex = proposedTitle.indexOf("/", firstIndex + 1);
			if (secondIndex >= 0) {
				firstIndex--;
				if (Character.isDigit(proposedTitle.charAt(firstIndex))) {
					firstIndex--;
				}
				secondIndex++;
				for (int i = 0; i < 4; i++) {
					if (!Character.isDigit(proposedTitle.charAt(secondIndex))) {
						break;
					}
					secondIndex++;

				}
				return proposedTitle.substring(firstIndex, secondIndex);
			}
		}
		return "";
	}

	protected boolean fillSAPFileInternal(ASAPFile asapFile, SegmentList segmentList, MessageQueue messageQueue) {

		List<String> texts = new ArrayList<String>();
		getTexts(segmentList, texts);
		var titleComplete = false;
		var titleBuilder = new StringBuilder();
		for (var text : texts) {
			var lower = text.toLowerCase().trim();

			if (lower.startsWith("author:")) {
				titleComplete = true;
				if (!lower.contains("press shift key")) {

					if (ASAPFile.isStringEmpty(asapFile.getAuthor())) {
						var proposedAuthor = StringUtility.condense(text.substring(7));
						asapFile.setAuthor(proposedAuthor);
					}
				}
			}
			if (!titleComplete && !lower.startsWith("MONO") && !lower.startsWith("STEREO")) {
				titleBuilder.append(text).append(" ");
			}
		}
		var proposedTitle = StringUtility.condense(titleBuilder.toString());
		if (ASAPFile.isStringEmpty(asapFile.getTitle())) {
			asapFile.setTitle(proposedTitle);
		}
		var proposedDate = getProposedDate(proposedTitle);
		if (ASAPFile.isStringEmpty(asapFile.getDate())) {
			asapFile.setDate(proposedDate);
		}

		return true;
	}

}
