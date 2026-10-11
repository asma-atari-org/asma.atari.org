package org.atari.asma.browser.composer;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.atari.asma.ASMAClient;
import org.atari.asma.Composer;

import com.wudsn.tools.base.gui.AttributeTablePreferences;

/**
 * Reusable composer list: the {@link ComposerFilterPanel} above the
 * {@link ComposerListPanel}, usable without the ASMABrowser main window, so that other
 * tools (e.g. a submission tool) can let a composer find and choose
 * themselves. See plans/08 and plans/09.
 *
 * @author Peter Dell
 */
public final class ComposerSelectionPanel extends JPanel {

	private final ComposerFilterPanel filterPanel;
	private final ComposerListPanel listPanel;
	private List<Composer> allComposers;
	private Map<String, Integer> fileCountByFolderName;

	public ComposerSelectionPanel(AttributeTablePreferences preferences) {
		super(new BorderLayout());
		allComposers = new ArrayList<Composer>();
		filterPanel = new ComposerFilterPanel(selection -> applySelection(selection));
		listPanel = new ComposerListPanel(preferences);
		listPanel.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(filterPanel, BorderLayout.NORTH);
		add(listPanel, BorderLayout.CENTER);
		applySelection(filterPanel.getSelection());
	}

	/**
	 * Shows the composers of the database, with their file counts.
	 *
	 * @param database
	 *            The database, not <code>null</code>.
	 */
	public void setDatabase(ASMAClient.Database database) {
		if (database == null) {
			throw new IllegalArgumentException("Parameter 'database' must not be null.");
		}
		this.allComposers = database.composerInfos;
		this.fileCountByFolderName = database.getFileCountByComposerFolderName();
		applySelection(filterPanel.getSelection());
	}

	private void applySelection(Composer.Selection selection) {
		List<Composer> selectedComposers = new ArrayList<Composer>(allComposers.size());
		for (Composer composer : allComposers) {
			if (selection.matches(composer)) {
				selectedComposers.add(composer);
			}
		}
		listPanel.getTableModel().setComposers(selectedComposers, fileCountByFolderName);
		filterPanel.setCountText(selectedComposers.size(), allComposers.size());
	}

	/** The selected composer, or <code>null</code>. */
	public Composer getSelectedComposer() {
		JTable table = listPanel.getTable();
		int viewRow = table.getSelectedRow();
		if (viewRow < 0) {
			return null;
		}
		return listPanel.getTableModel().getComposer(table.convertRowIndexToModel(viewRow));
	}

	public JTable getTable() {
		return listPanel.getTable();
	}

	/** The field to focus first. */
	public JTextField getKeywordField() {
		return filterPanel.getKeywordField();
	}
}
