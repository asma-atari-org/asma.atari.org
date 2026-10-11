package org.atari.asma.browser;

import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.atari.asma.DataTypes;
import org.atari.asma.FileInfo;
import org.atari.asma.gui.FilterPanel;

/**
 * Filter panel above the file info table, mirroring the search form of the
 * website player (asma.js). Every change is applied immediately and reported
 * as a {@link FileInfo.Selection} to the owner.
 *
 * @author Peter Dell
 */
public final class FileInfoFilterPanel extends FilterPanel<FileInfo.Selection> {

	// Combo box entries, index-aligned with the values below.
	private static final String[] HARDWARE_TEXTS = { "All", "Atari 800", "Atari 2600" };
	private static final String[] HARDWARE_VALUES = { null, FileInfo.ATARI800, FileInfo.ATARI2600 };
	private static final String[] CHANNELS_TEXTS = { "All", "Mono (4)", "Stereo (8)" };
	private static final int[] CHANNELS_VALUES = { 0, 1, 2 };

	private final JTextField keywordField;
	private final JTextField titleField;
	private final JTextField authorField;
	private final JTextField dateField;
	private final JComboBox<String> hardwareComboBox;
	private final JComboBox<String> channelsComboBox;
	private final JTextField formatField;

	public FileInfoFilterPanel(Consumer<FileInfo.Selection> selectionConsumer) {
		super(selectionConsumer);

		keywordField = addTextField(DataTypes.FileInfoSelection_Keyword, 12);
		titleField = addTextField(DataTypes.FileInfo_Title, 10);
		authorField = addTextField(DataTypes.FileInfo_Author, 10);
		dateField = addTextField(DataTypes.FileInfo_Date, 6);
		hardwareComboBox = addComboBox(DataTypes.FileInfo_Hardware, HARDWARE_TEXTS);
		channelsComboBox = addComboBox(DataTypes.FileInfo_Channels, CHANNELS_TEXTS);
		formatField = addTextField(DataTypes.FileInfo_OriginalModuleExt, 4);
		addCountLabel();
	}

	@Override
	public FileInfo.Selection getSelection() {
		return new FileInfo.Selection(keywordField.getText(), "", titleField.getText(),
				authorField.getText(), dateField.getText(), HARDWARE_VALUES[hardwareComboBox.getSelectedIndex()],
				CHANNELS_VALUES[channelsComboBox.getSelectedIndex()], formatField.getText());
	}

	public void setCountText(int selectedCount, int totalCount) {
		setCountText(selectedCount, totalCount, "files");
	}
}
