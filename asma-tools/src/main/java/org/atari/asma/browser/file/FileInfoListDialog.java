package org.atari.asma.browser.file;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import org.atari.asma.ASMAClient;
import org.atari.asma.FileInfo;
import org.atari.asma.browser.Messages;
import org.atari.asma.browser.Texts;

import com.wudsn.tools.base.common.MessageQueueEntry;
import com.wudsn.tools.base.gui.AttributeTablePreferences;
import com.wudsn.tools.base.gui.ModalDialog;

/**
 * Dialog listing the files of the ASMA database with filter fields, details
 * and replay controls, in two modes:
 * <ul>
 * <li>{@link #browse}: OK only; a double click plays, like in the
 * ASMABrowser.</li>
 * <li>{@link #choose}: OK and Cancel, returns the chosen file info. OK is
 * enabled only while a file is selected; a double click chooses.</li>
 * </ul>
 * Playing stops when the dialog closes. Files are downloaded from the website
 * of the given client.
 *
 * @author Peter Dell
 */
public final class FileInfoListDialog extends ModalDialog {

	private final FileInfoPlayerPanel playerPanel;

	private FileInfoListDialog(Window owner, AttributeTablePreferences preferences, ASMAClient client,
			ASMAClient.Database database, boolean chooseMode) {
		super(owner, chooseMode ? Texts.FileInfoListDialog_ChooseTitle : Texts.FileInfoListDialog_Title, chooseMode);
		if (client == null) {
			throw new IllegalArgumentException("Parameter 'client' must not be null.");
		}
		playerPanel = new FileInfoPlayerPanel(preferences, client::downloadFile, this::showPlaybackError);
		playerPanel.setDatabase(database);
		playerPanel.setPreferredSize(new Dimension(1000, 750));
		getContentPane().add(playerPanel, BorderLayout.CENTER);

		var table = playerPanel.getTable();
		if (chooseMode) {
			getOKButton().setEnabled(false);
			table.getSelectionModel().addListSelectionListener(
					event -> getOKButton().setEnabled(playerPanel.getSelectedFileInfo() != null));
		}
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2 && playerPanel.getSelectedFileInfo() != null) {
					if (chooseMode) {
						getOKButton().doClick();
					} else {
						playerPanel.playSelected();
					}
				}
			}
		});
	}

	private void showPlaybackError(FileInfo fileInfo, String reason) {
		JOptionPane.showMessageDialog(this,
				new MessageQueueEntry(this, null, Messages.E004, fileInfo == null ? "" : fileInfo.filePath, reason)
						.getMessageText(),
				getTitle(), JOptionPane.ERROR_MESSAGE);
	}

	private void showAndDispose() {
		try {
			showModal(playerPanel.getKeywordField());
		} finally {
			playerPanel.dispose();
			// The table layout goes back into the caller's preferences, see plans/11.
			playerPanel.getTable().saveLayout();
		}
	}

	/**
	 * Shows the files for browsing and listening.
	 *
	 * @param owner
	 *            The owner window or <code>null</code>.
	 * @param preferences
	 *            The table preferences, not <code>null</code>. They receive the
	 *            table layout when the dialog closes; storing them is up to the
	 *            caller.
	 * @param client
	 *            The client to download the files with, not <code>null</code>.
	 * @param database
	 *            The database loaded with the client, not <code>null</code>.
	 */
	public static void browse(Window owner, AttributeTablePreferences preferences, ASMAClient client,
			ASMAClient.Database database) {
		new FileInfoListDialog(owner, preferences, client, database, false).showAndDispose();
	}

	/**
	 * Lets the user choose a file, see {@link #browse} for the parameters.
	 *
	 * @return The chosen file info, or <code>null</code> if the dialog was
	 *         cancelled.
	 */
	public static FileInfo choose(Window owner, AttributeTablePreferences preferences, ASMAClient client,
			ASMAClient.Database database) {
		FileInfoListDialog dialog = new FileInfoListDialog(owner, preferences, client, database, true);
		dialog.showAndDispose();
		return dialog.okPressed ? dialog.playerPanel.getSelectedFileInfo() : null;
	}
}
