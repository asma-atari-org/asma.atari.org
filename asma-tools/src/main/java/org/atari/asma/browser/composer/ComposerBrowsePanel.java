package org.atari.asma.browser.composer;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.atari.asma.ASMAClient;
import org.atari.asma.Composer;
import org.atari.asma.browser.STILSource;

import com.wudsn.tools.base.gui.AttributeTablePreferences;

/**
 * The composer list with the details panel on top, like the file player
 * panel. Used by the Composers tab of the ASMABrowser and by the
 * {@link ComposerListDialog}. See plans/09, section 5.
 *
 * @author Peter Dell
 */
public final class ComposerBrowsePanel extends JPanel {

	private final STILSource stilSource;
	private final ComposerDetailsPanel detailsPanel;
	private final ComposerSelectionPanel selectionPanel;
	private Map<String, Integer> fileCountByFolderName;

	/**
	 * @param preferences
	 *            The table preferences, not <code>null</code>.
	 * @param stilSource
	 *            The STIL source, not <code>null</code>, e.g.
	 *            {@link STILSource#NONE}.
	 */
	public ComposerBrowsePanel(AttributeTablePreferences preferences, STILSource stilSource) {
		super(new BorderLayout());
		if (stilSource == null) {
			throw new IllegalArgumentException("Parameter 'stilSource' must not be null.");
		}
		this.stilSource = stilSource;
		fileCountByFolderName = new HashMap<String, Integer>();
		detailsPanel = new ComposerDetailsPanel();
		selectionPanel = new ComposerSelectionPanel(preferences);
		selectionPanel.addSelectionListener(this::updateDetailsPanel);
		add(detailsPanel, BorderLayout.NORTH);
		add(selectionPanel, BorderLayout.CENTER);
	}

	/**
	 * @param database
	 *            The database, not <code>null</code>.
	 */
	public void setDatabase(ASMAClient.Database database) {
		if (database == null) {
			throw new IllegalArgumentException("Parameter 'database' must not be null.");
		}
		fileCountByFolderName = database.getFileCountByComposerFolderName();
		selectionPanel.setDatabase(database);
	}

	private void updateDetailsPanel() {
		Composer composer = selectionPanel.getSelectedComposer();
		if (composer == null) {
			detailsPanel.setComposer(null, 0, "");
			return;
		}
		detailsPanel.setComposer(composer, fileCountByFolderName.getOrDefault(composer.folderName, 0),
				stilSource.getComment("/Composers/" + composer.folderName + "/"));
	}

	/** The selected composer, or <code>null</code>. */
	public Composer getSelectedComposer() {
		return selectionPanel.getSelectedComposer();
	}

	public JTable getTable() {
		return selectionPanel.getTable();
	}

	/** The field to focus first. */
	public JTextField getKeywordField() {
		return selectionPanel.getKeywordField();
	}
}
