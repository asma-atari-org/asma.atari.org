package org.atari.asma.browser;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.atari.asma.Composer;

import com.wudsn.tools.base.gui.AttributeTablePreferences;

/**
 * Reusable composer list: the {@link ComposerFilterPanel} above the
 * {@link ComposerListPanel}, usable without the ASMABrowser main window, so that other
 * tools (e.g. a submission tool) can let a composer find and choose
 * themselves. See plans/08.
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
	 * @param composers
	 *            The composers, not <code>null</code>.
	 * @param fileCountByFolderName
	 *            The number of files per composer folder name, or
	 *            <code>null</code> if unknown.
	 */
	public void setComposers(List<Composer> composers, Map<String, Integer> fileCountByFolderName) {
		if (composers == null) {
			throw new IllegalArgumentException("Parameter 'composers' must not be null.");
		}
		this.allComposers = composers;
		this.fileCountByFolderName = fileCountByFolderName;
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
