package org.atari.asma.browser.group;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.atari.asma.ASMAClient;
import org.atari.asma.Composer;
import org.atari.asma.Group;
import org.atari.asma.browser.STILSource;

import com.wudsn.tools.base.gui.AttributeTable;
import com.wudsn.tools.base.gui.AttributeTablePreferences;

/**
 * The group list with the details panel on top, like the composer browse
 * panel. Used by the Groups tab of the ASMABrowser and by the
 * {@link GroupListDialog}. See plans/09, section 5.
 *
 * @author Peter Dell
 */
public final class GroupBrowsePanel extends JPanel {

	private final STILSource stilSource;
	private final GroupDetailsPanel detailsPanel;
	private final GroupSelectionPanel selectionPanel;
	private Map<String, List<Composer>> composersByFolderName;
	private Map<String, Integer> fileCountByFolderName;

	/**
	 * @param preferences
	 *            The table preferences, not <code>null</code>.
	 * @param stilSource
	 *            The STIL source, not <code>null</code>, e.g.
	 *            {@link STILSource#NONE}.
	 */
	public GroupBrowsePanel(AttributeTablePreferences preferences, STILSource stilSource) {
		super(new BorderLayout());
		if (stilSource == null) {
			throw new IllegalArgumentException("Parameter 'stilSource' must not be null.");
		}
		this.stilSource = stilSource;
		composersByFolderName = new HashMap<String, List<Composer>>();
		fileCountByFolderName = new HashMap<String, Integer>();
		detailsPanel = new GroupDetailsPanel();
		selectionPanel = new GroupSelectionPanel(preferences);
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
		composersByFolderName = database.getComposersByGroupFolderName();
		fileCountByFolderName = database.getFileCountByGroupFolderName();
		selectionPanel.setDatabase(database);
	}

	private void updateDetailsPanel() {
		Group group = selectionPanel.getSelectedGroup();
		if (group == null) {
			detailsPanel.setGroup(null, "", 0, "");
			return;
		}
		String composers = composersByFolderName.getOrDefault(group.folderName, List.of()).stream()
				.map(Composer::getDisplayName).collect(Collectors.joining(", "));
		detailsPanel.setGroup(group, composers, fileCountByFolderName.getOrDefault(group.folderName, 0),
				stilSource.getComment("/Groups/" + group.folderName + "/"));
	}

	/** The selected group, or <code>null</code>. */
	public Group getSelectedGroup() {
		return selectionPanel.getSelectedGroup();
	}

	public AttributeTable getTable() {
		return selectionPanel.getTable();
	}

	/** The field to focus first. */
	public JTextField getKeywordField() {
		return selectionPanel.getKeywordField();
	}
}
