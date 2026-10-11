package org.atari.asma.browser.composer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.atari.asma.ASMAClient;
import org.atari.asma.Composer;
import org.atari.asma.browser.Texts;

import com.wudsn.tools.base.gui.AttributeTablePreferences;
import com.wudsn.tools.base.gui.ModalDialog;

/**
 * Dialog listing the composers of the ASMA database with filter fields, in
 * two modes:
 * <ul>
 * <li>{@link #browse}: OK only, for browsing; Escape and the close box close it
 * as well.</li>
 * <li>{@link #choose}: OK and Cancel, returns the chosen composer, e.g. for a
 * submission. OK is enabled only while a composer is selected; a double click
 * chooses.</li>
 * </ul>
 *
 * @author Peter Dell
 */
public final class ComposerListDialog extends ModalDialog {

	private final ComposerSelectionPanel selectionPanel;

	private ComposerListDialog(Window owner, AttributeTablePreferences preferences, ASMAClient.Database database,
			boolean chooseMode) {
		super(owner, chooseMode ? Texts.ComposerListDialog_ChooseTitle : Texts.ComposerListDialog_Title, chooseMode);
		selectionPanel = new ComposerSelectionPanel(preferences);
		selectionPanel.setDatabase(database);
		selectionPanel.setPreferredSize(new Dimension(900, 550));
		// The panel fills the dialog; the base class keeps the fields pane on top and the buttons below.
		getContentPane().add(selectionPanel, BorderLayout.CENTER);

		if (chooseMode) {
			var table = selectionPanel.getTable();
			getOKButton().setEnabled(false);
			table.getSelectionModel().addListSelectionListener(
					event -> getOKButton().setEnabled(selectionPanel.getSelectedComposer() != null));
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent event) {
					if (event.getClickCount() == 2 && selectionPanel.getSelectedComposer() != null) {
						getOKButton().doClick();
					}
				}
			});
		}
	}

	/**
	 * Shows the composers for browsing.
	 *
	 * @param owner
	 *            The owner window or <code>null</code>.
	 * @param preferences
	 *            The table preferences, not <code>null</code>.
	 * @param database
	 *            The database, not <code>null</code>.
	 */
	public static void browse(Window owner, AttributeTablePreferences preferences, ASMAClient.Database database) {
		ComposerListDialog dialog = new ComposerListDialog(owner, preferences, database, false);
		dialog.showModal(dialog.selectionPanel.getKeywordField());
	}

	/**
	 * Lets the user choose a composer, see {@link #browse} for the parameters.
	 *
	 * @return The chosen composer, or <code>null</code> if the dialog was
	 *         cancelled.
	 */
	public static Composer choose(Window owner, AttributeTablePreferences preferences, ASMAClient.Database database) {
		ComposerListDialog dialog = new ComposerListDialog(owner, preferences, database, true);
		dialog.showModal(dialog.selectionPanel.getKeywordField());
		return dialog.okPressed ? dialog.selectionPanel.getSelectedComposer() : null;
	}
}
