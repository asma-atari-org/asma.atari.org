package org.atari.asma.browser.file;

import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.atari.asma.Channels;
import org.atari.asma.DataTypes;
import org.atari.asma.FileInfo;
import org.atari.asma.Hardware;
import org.atari.asma.browser.FilterPanel;

/**
 * Filter panel above the file info table, mirroring the search form of the
 * website player (asma.js). Every change is applied immediately and reported
 * as a {@link FileInfo.Selection} to the owner.
 *
 * @author Peter Dell
 */
public final class FileInfoFilterPanel extends FilterPanel<FileInfo.Selection> {

	private final JTextField keywordField;
	private final JTextField titleField;
	private final JTextField authorField;
	private final JTextField dateField;
	private final JComboBox<Object> hardwareComboBox;
	private final JComboBox<Object> channelsComboBox;
	private final JTextField formatField;

	public FileInfoFilterPanel(Consumer<FileInfo.Selection> selectionConsumer) {
		super(selectionConsumer);

		keywordField = addTextField(DataTypes.FileInfoSelection_Keyword, 12);
		titleField = addTextField(DataTypes.FileInfo_Title, 10);
		authorField = addTextField(DataTypes.FileInfo_Author, 10);
		dateField = addTextField(DataTypes.FileInfo_Date, 6);
		formatField = addTextField(DataTypes.FileInfo_OriginalModuleExt, 4);
		channelsComboBox = addValueSetComboBox(DataTypes.FileInfo_Channels, Channels.class);
		hardwareComboBox = addValueSetComboBox(DataTypes.FileInfo_Hardware, Hardware.class);
		addCountLabel();
	}

	/** The field to focus first. */
	public JTextField getKeywordField() {
		return keywordField;
	}

	@Override
	public FileInfo.Selection getSelection() {
		return new FileInfo.Selection(keywordField.getText(), "", titleField.getText(),
				authorField.getText(), dateField.getText(), getSelectedValue(hardwareComboBox, Hardware.class),
				getSelectedValue(channelsComboBox, Channels.class), formatField.getText());
	}

	public void setCountText(int selectedCount, int totalCount) {
		setCountText(selectedCount, totalCount, "files");
	}
}
