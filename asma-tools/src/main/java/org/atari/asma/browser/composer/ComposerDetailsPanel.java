package org.atari.asma.browser.composer;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.atari.asma.Composer;
import org.atari.asma.DataTypes;
import org.atari.asma.browser.DemozooIDField;
import org.atari.asma.browser.DetailsPanel;
import org.atari.asma.demozoo.DemozooID;

/**
 * Details panel displaying the properties of the selected {@link Composer},
 * like the website's author details, plus the STIL comment of the composer's
 * folder. See plans/09, section 5.
 *
 * @author Peter Dell
 */
public final class ComposerDetailsPanel extends DetailsPanel {

	private final JTextField firstNameField;
	private final JTextField lastNameField;
	private final JTextField handlesField;
	private final JTextField folderNameField;
	private final JTextField countryField;
	private final DemozooIDField demozooIDField;
	private final JTextField groupsField;
	private final JTextField fileCountField;
	private final JTextArea stilArea;

	public ComposerDetailsPanel() {
		firstNameField = addField(DataTypes.Composer_FirstName);
		lastNameField = addField(DataTypes.Composer_LastName);
		handlesField = addField(DataTypes.Composer_Handles);
		folderNameField = addField(DataTypes.Composer_FolderName);
		countryField = addField(DataTypes.Composer_Country);
		demozooIDField = addDemozooIDField(DataTypes.Composer_DemozooID, DemozooID.Kind.SCENER);
		groupsField = addField(DataTypes.Composer_Groups);
		fileCountField = addField(DataTypes.Composer_FileCount);
		stilArea = addSTILArea();
		setComposer(null, 0, "");
	}

	// Like the table: the Unicode spelling when there is one.
	private static String getUnicodeOrDefault(String unicodeValue, String value) {
		return (unicodeValue != null && !unicodeValue.isEmpty()) ? unicodeValue : value;
	}

	/**
	 * @param composer
	 *            The composer or <code>null</code>.
	 * @param fileCount
	 *            The number of files in the composer's folder.
	 * @param stilComment
	 *            The STIL comment of the composer's folder, not
	 *            <code>null</code>.
	 */
	public void setComposer(Composer composer, int fileCount, String stilComment) {
		if (stilComment == null) {
			throw new IllegalArgumentException("Parameter 'stilComment' must not be null.");
		}
		if (composer == null) {
			firstNameField.setText("");
			lastNameField.setText("");
			handlesField.setText("");
			folderNameField.setText("");
			countryField.setText("");
			demozooIDField.clear();
			groupsField.setText("");
			fileCountField.setText("");
			stilArea.setText("");
			return;
		}
		firstNameField.setText(getUnicodeOrDefault(composer.firstNameUnicode, composer.firstName));
		lastNameField.setText(getUnicodeOrDefault(composer.lastNameUnicode, composer.lastName));
		handlesField.setText(composer.handles);
		folderNameField.setText(composer.folderName);
		countryField.setText(composer.country);
		demozooIDField.setDemozooID(DemozooID.parse(DemozooID.Kind.SCENER, composer.demozooID),
				composer.getDisplayName());
		groupsField.setText(composer.groups);
		fileCountField.setText(String.valueOf(fileCount));
		stilArea.setText(stilComment);
		stilArea.setCaretPosition(0);
	}
}
