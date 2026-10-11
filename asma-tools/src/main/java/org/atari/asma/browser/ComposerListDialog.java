package org.atari.asma.browser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import org.atari.asma.Composer;

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

	private ComposerListDialog(Window owner, AttributeTablePreferences preferences, List<Composer> composers,
			Map<String, Integer> fileCountByFolderName, boolean chooseMode) {
		super(owner, chooseMode ? Texts.ComposerListDialog_ChooseTitle : Texts.ComposerListDialog_Title, chooseMode);
		selectionPanel = new ComposerSelectionPanel(preferences);
		selectionPanel.setComposers(composers, fileCountByFolderName);
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
	 * @param composers
	 *            The composers, not <code>null</code>.
	 * @param fileCountByFolderName
	 *            The number of files per composer folder name, or
	 *            <code>null</code> if unknown.
	 */
	public static void browse(Window owner, AttributeTablePreferences preferences, List<Composer> composers,
			Map<String, Integer> fileCountByFolderName) {
		ComposerListDialog dialog = new ComposerListDialog(owner, preferences, composers, fileCountByFolderName,
				false);
		dialog.showModal(dialog.selectionPanel.getKeywordField());
	}

	/**
	 * Lets the user choose a composer, see {@link #browse} for the parameters.
	 *
	 * @return The chosen composer, or <code>null</code> if the dialog was
	 *         cancelled.
	 */
	public static Composer choose(Window owner, AttributeTablePreferences preferences, List<Composer> composers,
			Map<String, Integer> fileCountByFolderName) {
		ComposerListDialog dialog = new ComposerListDialog(owner, preferences, composers, fileCountByFolderName,
				true);
		dialog.showModal(dialog.selectionPanel.getKeywordField());
		return dialog.okPressed ? dialog.selectionPanel.getSelectedComposer() : null;
	}
}
