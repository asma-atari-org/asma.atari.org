package org.atari.asma.browser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
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
	private final JTextField filePathField;
	private final JTextField fileSizeField;
	private final JTextField demozooIDField;

	// Fields are laid out in two label/field column pairs, filled row by row.
	private int fieldIndex;
	// Full-width rows below the fields, for the replay controls.
	private int extraRowCount;

	/**
	 * @param songsComponent
	 *            The component displayed for the songs, the song buttons of
	 *            the replay controls, not <code>null</code>.
	 * @param playingTimeComponent
	 *            The playing time display of the replay controls, not
	 *            <code>null</code>.
	 */
	public FileInfoDetailsPanel(JComponent songsComponent, JComponent playingTimeComponent) {
		if (songsComponent == null) {
			throw new IllegalArgumentException("Parameter 'songsComponent' must not be null.");
		}
		if (playingTimeComponent == null) {
			throw new IllegalArgumentException("Parameter 'playingTimeComponent' must not be null.");
		}
		setLayout(new GridBagLayout());
		fieldIndex = 0;
		titleField = addField(DataTypes.FileInfo_Title);
		authorField = addField(DataTypes.FileInfo_Author);
		dateField = addField(DataTypes.FileInfo_Date);
		channelsField = addField(DataTypes.FileInfo_Channels);
		addComponent(ElementFactory.createLabel(DataTypes.FileInfo_Songs, songsComponent), songsComponent);
		addComponent(new JLabel(Texts.Replay_PlayingTime), playingTimeComponent);
		hardwareField = addField(DataTypes.FileInfo_Hardware);
		formatField = addField(DataTypes.FileInfo_OriginalModuleExt);
		filePathField = addField(DataTypes.FileInfo_FilePath);
		fileSizeField = addField(DataTypes.FileInfo_FileSize);
		demozooIDField = addField(DataTypes.FileInfo_DemozooID);
		setFileInfo(null);
	}

	private JTextField addField(DataType dataType) {
		JTextField field = new JTextField();
		field.setEditable(false);
		addComponent(ElementFactory.createLabel(dataType, field), field);
		return field;
	}

	private void addComponent(JLabel label, JComponent field) {
		int gridx = (fieldIndex % 2) * 2;
		int gridy = fieldIndex / 2;

		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridx = gridx;
		labelConstraints.gridy = gridy;
		labelConstraints.anchor = GridBagConstraints.WEST;
		labelConstraints.insets = new Insets(2, 4, 2, 8);
		add(label, labelConstraints);

		GridBagConstraints fieldConstraints = new GridBagConstraints();
		fieldConstraints.gridx = gridx + 1;
		fieldConstraints.gridy = gridy;
		fieldConstraints.weightx = 1.0;
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldConstraints.insets = new Insets(2, 0, 2, gridx == 0 ? 16 : 4);
		add(createFixedWidthCell(field), fieldConstraints);

		fieldIndex++;
	}

	/**
	 * Wraps a value component into a cell with a constant preferred width, so
	 * both value columns share the panel width equally and keep their width
	 * when the content changes (a longer title, more song buttons, the playing
	 * time text). The height stays the component's natural height.
	 */
	private static JComponent createFixedWidthCell(JComponent component) {
		JPanel cell = new JPanel(new BorderLayout()) {
			@Override
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				size.width = 1;
				return size;
			}

			@Override
			public Dimension getMinimumSize() {
				Dimension size = super.getMinimumSize();
				size.width = 1;
				return size;
			}
		};
		cell.setOpaque(false);
		cell.add(component, BorderLayout.CENTER);
		return cell;
	}

	private int getNextExtraRow() {
		return (fieldIndex + 1) / 2 + extraRowCount++;
	}

	/** Adds an unlabeled row below the fields, spanning all columns. */
	public void addFullWidthRow(JComponent component) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = getNextExtraRow();
		constraints.gridwidth = 4;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(2, 0, 2, 4);
		add(component, constraints);
	}

	public void setFileInfo(FileInfo fileInfo) {
		if (fileInfo == null) {
			titleField.setText("");
			authorField.setText("");
			dateField.setText("");
			hardwareField.setText("");
			formatField.setText("");
			channelsField.setText("");
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
		channelsField.setText(fileInfo.getChannelsText());
		filePathField.setText(fileInfo.filePath);
		fileSizeField.setText(NumberFormat.getIntegerInstance().format(fileInfo.fileSize) + " bytes");
		demozooIDField.setText(fileInfo.getDemozooID() == 0 ? "" : String.valueOf(fileInfo.getDemozooID()));
	}
}
