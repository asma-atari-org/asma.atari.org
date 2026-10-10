package org.atari.asma.browser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.util.List;
import java.util.Map;

import org.atari.asma.Composer;

import com.wudsn.tools.base.gui.AttributeTablePreferences;
import com.wudsn.tools.base.gui.ModalDialog;

/**
 * Dialog listing the composers of the ASMA database in an attribute table.
 * OK-only; Escape and the close box close it as well.
 *
 * @author Peter Dell
 */
final class ComposerListDialog extends ModalDialog {

	private final ComposerListPanel composerListPanel;

	ComposerListDialog(Window owner, AttributeTablePreferences preferences, List<Composer> composers,
			Map<String, Integer> fileCountByFolderName) {
		super(owner, Texts.ComposerListDialog_Title, false);
		composerListPanel = new ComposerListPanel(preferences);
		composerListPanel.getTableModel().setComposers(composers, fileCountByFolderName);
		composerListPanel.setPreferredSize(new Dimension(900, 550));
		// The table fills the dialog; the base class keeps the fields pane on top and the buttons below.
		getContentPane().add(composerListPanel, BorderLayout.CENTER);
	}

	void open() {
		showModal(composerListPanel.getTable());
	}
}
