package org.atari.asma.browser;

import java.awt.FlowLayout;
import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.atari.asma.DataTypes;
import org.atari.asma.FileInfo;

import com.wudsn.tools.base.gui.ElementFactory;
import com.wudsn.tools.base.repository.DataType;

/**
 * Filter panel above the file info table, mirroring the search form of the
 * website player (asma.js). Every change is applied immediately and reported
 * as a {@link FileInfo.Selection} to the owner.
 *
 * @author Peter Dell
 */
public final class FileInfoFilterPanel extends JPanel {

	// Combo box entries, index-aligned with the values below.
	private static final String[] HARDWARE_TEXTS = { "All", "Atari 800", "Atari 2600" };
	private static final String[] HARDWARE_VALUES = { null, FileInfo.ATARI800, FileInfo.ATARI2600 };
	private static final String[] CHANNELS_TEXTS = { "All", "Mono (4)", "Stereo (8)" };
	private static final int[] CHANNELS_VALUES = { 0, 1, 2 };

	private final Consumer<FileInfo.Selection> selectionConsumer;
	private final JTextField keywordField;
	private final JTextField titleField;
	private final JTextField authorField;
	private final JTextField dateField;
	private final JComboBox<String> hardwareComboBox;
	private final JComboBox<String> channelsComboBox;
	private final JTextField formatField;
	private final JLabel countLabel;

	public FileInfoFilterPanel(Consumer<FileInfo.Selection> selectionConsumer) {
		if (selectionConsumer == null) {
			throw new IllegalArgumentException("Parameter 'selectionConsumer' must not be null.");
		}
		this.selectionConsumer = selectionConsumer;
		setLayout(new FlowLayout(FlowLayout.LEFT, 8, 2));

		keywordField = addTextField(DataTypes.FileInfoSelection_Keyword, 12);
		titleField = addTextField(DataTypes.FileInfo_Title, 10);
		authorField = addTextField(DataTypes.FileInfo_Author, 10);
		dateField = addTextField(DataTypes.FileInfo_Date, 6);
		hardwareComboBox = addComboBox(DataTypes.FileInfo_Hardware, HARDWARE_TEXTS);
		channelsComboBox = addComboBox(DataTypes.FileInfo_Channels, CHANNELS_TEXTS);
		formatField = addTextField(DataTypes.FileInfo_OriginalModuleExt, 4);

		countLabel = new JLabel();
		add(countLabel);
	}

	private JTextField addTextField(DataType dataType, int columns) {
		JTextField field = new JTextField(columns);
		add(ElementFactory.createLabel(dataType, field));
		add(field);
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent event) {
				fireSelectionChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				fireSelectionChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				fireSelectionChanged();
			}
		});
		return field;
	}

	private JComboBox<String> addComboBox(DataType dataType, String[] texts) {
		JComboBox<String> comboBox = new JComboBox<String>(texts);
		add(ElementFactory.createLabel(dataType, comboBox));
		add(comboBox);
		comboBox.addActionListener(event -> fireSelectionChanged());
		return comboBox;
	}

	void fireSelectionChanged() {
		selectionConsumer.accept(getSelection());
	}

	public FileInfo.Selection getSelection() {
		return new FileInfo.Selection(keywordField.getText(), "", titleField.getText(),
				authorField.getText(), dateField.getText(), HARDWARE_VALUES[hardwareComboBox.getSelectedIndex()],
				CHANNELS_VALUES[channelsComboBox.getSelectedIndex()], formatField.getText());
	}

	public void setCountText(int selectedCount, int totalCount) {
		countLabel.setText(selectedCount + " of " + totalCount + " files");
	}
}
