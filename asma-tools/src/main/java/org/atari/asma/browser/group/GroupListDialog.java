package org.atari.asma.browser.group;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.atari.asma.ASMAClient;
import org.atari.asma.Group;
import org.atari.asma.browser.STILSource;
import org.atari.asma.browser.Texts;

import com.wudsn.tools.base.gui.AttributeTablePreferences;
import com.wudsn.tools.base.gui.ModalDialog;

/**
 * Dialog listing the groups of the ASMA database with details and filter fields, in two
 * modes like the composer list dialog:
 * <ul>
 * <li>{@link #browse}: OK only, for browsing; Escape and the close box close it
 * as well.</li>
 * <li>{@link #choose}: OK and Cancel, returns the chosen group. OK is enabled
 * only while a group is selected; a double click chooses.</li>
 * </ul>
 *
 * @author Peter Dell
 */
public final class GroupListDialog extends ModalDialog {

	private final GroupBrowsePanel browsePanel;

	private GroupListDialog(Window owner, AttributeTablePreferences preferences, ASMAClient.Database database,
			boolean chooseMode) {
		super(owner, chooseMode ? Texts.GroupListDialog_ChooseTitle : Texts.GroupListDialog_Title, chooseMode);
		// No STIL yet, see plans/09, section 5.
		browsePanel = new GroupBrowsePanel(preferences, STILSource.NONE);
		browsePanel.setDatabase(database);
		browsePanel.setPreferredSize(new Dimension(900, 600));
		// The panel fills the dialog; the base class keeps the fields pane on top and the buttons below.
		getContentPane().add(browsePanel, BorderLayout.CENTER);

		if (chooseMode) {
			var table = browsePanel.getTable();
			getOKButton().setEnabled(false);
			table.getSelectionModel().addListSelectionListener(
					event -> getOKButton().setEnabled(browsePanel.getSelectedGroup() != null));
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent event) {
					if (event.getClickCount() == 2 && browsePanel.getSelectedGroup() != null) {
						getOKButton().doClick();
					}
				}
			});
		}
	}

	// The table layout goes back into the caller's preferences, see plans/11.
	private void showAndSaveLayout() {
		try {
			showModal(browsePanel.getKeywordField());
		} finally {
			browsePanel.getTable().saveLayout();
		}
	}

	/**
	 * Shows the groups for browsing.
	 *
	 * @param owner
	 *            The owner window or <code>null</code>.
	 * @param preferences
	 *            The table preferences, not <code>null</code>. They receive the
	 *            table layout when the dialog closes; storing them is up to the
	 *            caller.
	 * @param database
	 *            The database, not <code>null</code>.
	 */
	public static void browse(Window owner, AttributeTablePreferences preferences, ASMAClient.Database database) {
		new GroupListDialog(owner, preferences, database, false).showAndSaveLayout();
	}

	/**
	 * Lets the user choose a group, see {@link #browse} for the parameters.
	 *
	 * @return The chosen group, or <code>null</code> if the dialog was
	 *         cancelled.
	 */
	public static Group choose(Window owner, AttributeTablePreferences preferences, ASMAClient.Database database) {
		GroupListDialog dialog = new GroupListDialog(owner, preferences, database, true);
		dialog.showAndSaveLayout();
		return dialog.okPressed ? dialog.browsePanel.getSelectedGroup() : null;
	}
}
