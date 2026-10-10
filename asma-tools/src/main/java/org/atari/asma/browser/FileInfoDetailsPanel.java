package org.atari.asma.browser;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.atari.asma.DataTypes;
import org.atari.asma.FileInfo;

import com.wudsn.tools.base.gui.ElementFactory;
import com.wudsn.tools.base.repository.DataType;

/**
 * Details panel displaying the properties of the selected {@link FileInfo},
 * like the player section of the web player displays them above the search
 * result.
 *
 * @author Peter Dell
 */
public final class FileInfoDetailsPanel extends JPanel {

	private final JTextField titleField;
	private final JTextField authorField;
	private final JTextField dateField;
	private final JTextField hardwareField;
	private final JTextField formatField;
	private final JTextField channelsField;
	private final JTextField songsField;
	private final JTextField filePathField;
	private final JTextField fileSizeField;
	private final JTextField demozooIDField;

	// Fields are laid out in two label/field column pairs, filled row by row.
	private int fieldIndex;

	public FileInfoDetailsPanel() {
		setLayout(new GridBagLayout());
		fieldIndex = 0;
		titleField = addField(DataTypes.FileInfo_Title);
		authorField = addField(DataTypes.FileInfo_Author);
		dateField = addField(DataTypes.FileInfo_Date);
		demozooIDField = addField(DataTypes.FileInfo_DemozooID);
		hardwareField = addField(DataTypes.FileInfo_Hardware);
		formatField = addField(DataTypes.FileInfo_OriginalModuleExt);
		channelsField = addField(DataTypes.FileInfo_Channels);
		songsField = addField(DataTypes.FileInfo_Songs);
		filePathField = addField(DataTypes.FileInfo_FilePath);
		fileSizeField = addField(DataTypes.FileInfo_FileSize);
		setFileInfo(null);
	}

	private JTextField addField(DataType dataType) {
		JTextField field = new JTextField();
		field.setEditable(false);

		int gridx = (fieldIndex % 2) * 2;
		int gridy = fieldIndex / 2;

		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridx = gridx;
		labelConstraints.gridy = gridy;
		labelConstraints.anchor = GridBagConstraints.WEST;
		labelConstraints.insets = new Insets(2, 4, 2, 8);
		add(ElementFactory.createLabel(dataType, field), labelConstraints);

		GridBagConstraints fieldConstraints = new GridBagConstraints();
		fieldConstraints.gridx = gridx + 1;
		fieldConstraints.gridy = gridy;
		fieldConstraints.weightx = 1.0;
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldConstraints.insets = new Insets(2, 0, 2, gridx == 0 ? 16 : 4);
		add(field, fieldConstraints);

		fieldIndex++;
		return field;
	}

	public void setFileInfo(FileInfo fileInfo) {
		if (fileInfo == null) {
			titleField.setText("");
			authorField.setText("");
			dateField.setText("");
			hardwareField.setText("");
			formatField.setText("");
			channelsField.setText("");
			songsField.setText("");
			filePathField.setText("");
			fileSizeField.setText("");
			demozooIDField.setText("");
			return;
		}
		titleField.setText(fileInfo.title);
		authorField.setText(fileInfo.author);
		dateField.setText(fileInfo.date);
		hardwareField.setText(fileInfo.hardware);
		formatField.setText(fileInfo.originalModuleExt);
		channelsField.setText(String.valueOf(fileInfo.channels));
		String songsText = String.valueOf(fileInfo.songs);
		if (fileInfo.songs > 1) {
			songsText += " (default " + (fileInfo.defaultSongIndex + 1) + ")";
		}
		songsField.setText(songsText);
		filePathField.setText(fileInfo.filePath);
		fileSizeField.setText(fileInfo.fileSize + " bytes");
		demozooIDField.setText(fileInfo.getDemozooID() == 0 ? "" : String.valueOf(fileInfo.getDemozooID()));
	}
}
