package org.atari.asma.browser.file;

import java.text.NumberFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.atari.asma.DataTypes;
import org.atari.asma.FileInfo;
import org.atari.asma.browser.DetailsPanel;
import org.atari.asma.browser.MusicDemozooIDField;
import org.atari.asma.browser.Texts;

import com.wudsn.tools.base.gui.ElementFactory;

/**
 * Details panel displaying the properties of the selected {@link FileInfo},
 * like the player section of the web player displays them above the search
 * result.
 *
 * @author Peter Dell
 */
public final class FileInfoDetailsPanel extends DetailsPanel {

	private final JTextField titleField;
	private final JTextField authorField;
	private final JTextField dateField;
	private final JTextField hardwareField;
	private final JTextField formatField;
	private final JTextField channelsField;
	private final JTextField filePathField;
	private final JTextField fileSizeField;
	private final MusicDemozooIDField demozooIDField;

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
		demozooIDField = addDemozooIDField(DataTypes.FileInfo_DemozooID, new MusicDemozooIDField());
		setFileInfo(null);
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
			demozooIDField.clear();
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
		demozooIDField.setDemozooID(fileInfo.getMusicDemozooID(), fileInfo.title == null ? "" : fileInfo.title);
	}
}
