package org.atari.asma.browser.file;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.atari.asma.ASMAClient;
import org.atari.asma.FileInfo;

import com.wudsn.tools.base.gui.AttributeTablePreferences;

/**
 * The file list with the details panel and the replay controls on top, like
 * the website player. Used by the Files tab of the ASMABrowser and by the
 * {@link FileInfoListDialog}. See plans/09.
 *
 * @author Peter Dell
 */
public final class FileInfoPlayerPanel extends JPanel {

	/** Provides the content of a file, e.g. from a local archive or the website. */
	public interface ContentLoader {
		byte[] loadContent(FileInfo fileInfo) throws IOException;
	}

	/** Displays a playback error, e.g. in a status bar. */
	public interface ErrorHandler {
		void showPlaybackError(FileInfo fileInfo, String reason);
	}

	private final FileInfoSelectionPanel selectionPanel;
	private final FileInfoDetailsPanel detailsPanel;
	private final ReplayControls replayControls;

	/**
	 * @param preferences
	 *            The table preferences, not <code>null</code>.
	 * @param contentLoader
	 *            The content loader, not <code>null</code>.
	 * @param errorHandler
	 *            The error handler, not <code>null</code>.
	 */
	public FileInfoPlayerPanel(AttributeTablePreferences preferences, ContentLoader contentLoader,
			ErrorHandler errorHandler) {
		super(new BorderLayout());
		if (contentLoader == null) {
			throw new IllegalArgumentException("Parameter 'contentLoader' must not be null.");
		}
		if (errorHandler == null) {
			throw new IllegalArgumentException("Parameter 'errorHandler' must not be null.");
		}
		selectionPanel = new FileInfoSelectionPanel(preferences);
		replayControls = new ReplayControls(new ReplayControls.Host() {
			@Override
			public byte[] loadContent(FileInfo fileInfo) throws IOException {
				return contentLoader.loadContent(fileInfo);
			}

			@Override
			public FileInfo selectRandomFileInfo() {
				return selectionPanel.selectRandomFileInfo();
			}

			@Override
			public void showPlaybackError(FileInfo fileInfo, String reason) {
				errorHandler.showPlaybackError(fileInfo, reason);
			}
		});
		detailsPanel = new FileInfoDetailsPanel(replayControls.getSongButtonsPanel(),
				replayControls.getPlayingTimePanel());
		detailsPanel.addFullWidthRow(replayControls.getButtonPanel());
		selectionPanel.addSelectionListener(this::updateDetailsPanel);

		add(detailsPanel, BorderLayout.NORTH);
		add(selectionPanel, BorderLayout.CENTER);
		updateDetailsPanel();
	}

	private void updateDetailsPanel() {
		FileInfo fileInfo = selectionPanel.getSelectedFileInfo();
		detailsPanel.setFileInfo(fileInfo);
		replayControls.setSelectedFileInfo(fileInfo);
	}

	/**
	 * @param database
	 *            The database, not <code>null</code>.
	 */
	public void setDatabase(ASMAClient.Database database) {
		if (database == null) {
			throw new IllegalArgumentException("Parameter 'database' must not be null.");
		}
		selectionPanel.setFileInfos(database.fileInfos);
	}

	/** The selected file info, or <code>null</code>. */
	public FileInfo getSelectedFileInfo() {
		return selectionPanel.getSelectedFileInfo();
	}

	/** Plays the selected file, like the Play button. */
	public void playSelected() {
		replayControls.playSelected();
	}

	/** Stops playing, like the Stop button. */
	public void stop() {
		replayControls.stop();
	}

	/** Stops playing and the timers, once the panel is no longer used. */
	public void dispose() {
		replayControls.dispose();
	}

	public JTable getTable() {
		return selectionPanel.getTable();
	}

	/** The field to focus first. */
	public JTextField getKeywordField() {
		return selectionPanel.getKeywordField();
	}
}
