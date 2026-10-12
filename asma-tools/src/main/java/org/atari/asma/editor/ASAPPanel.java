package org.atari.asma.editor;

import java.awt.GridBagConstraints;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.atari.asma.DataTypes;
import org.atari.asma.asap.ASAPFile;
import org.atari.asma.browser.DetailsPanel;

/**
 * The editable SAP header fields author, title and date.
 */
public final class ASAPPanel extends DetailsPanel {

	private static final long serialVersionUID = 1L;

	private final JTextField authorField;
	private final JTextField titleField;
	private final JTextField dateField;

	public ASAPPanel() {
		authorField = addFullWidthField(DataTypes.FileInfo_Author);
		titleField = addFullWidthField(DataTypes.FileInfo_Title);
		dateField = addFullWidthField(DataTypes.FileInfo_Date);

		// Keeps the fields at the top.
		var filler = new GridBagConstraints();
		filler.gridy = 3;
		filler.weighty = 1.0;
		add(new JPanel(), filler);
	}

	private void setEditable(boolean editable) {
		authorField.setEditable(editable);
		titleField.setEditable(editable);
		dateField.setEditable(editable);
	}

	public void dataToUI(ASAPFile asapFile, File outputFile) {
		authorField.setText(asapFile.getAuthor());
		titleField.setText(asapFile.getTitle());
		dateField.setText(asapFile.getDate());
		setEditable(outputFile != null);
	}

	public void dataFromUI(ASAPFile asapFile) {
		asapFile.setAuthor(authorField.getText());
		asapFile.setTitle(titleField.getText());
		asapFile.setDate(dateField.getText());
	}
}
