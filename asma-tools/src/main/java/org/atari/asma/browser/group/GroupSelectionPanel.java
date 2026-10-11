package org.atari.asma.browser.group;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.atari.asma.ASMAClient;
import org.atari.asma.Composer;
import org.atari.asma.Group;

import com.wudsn.tools.base.gui.AttributeTablePreferences;

/**
 * Reusable group list: the {@link GroupFilterPanel} above the
 * {@link GroupListPanel}, used by the Groups tab of the ASMABrowser and by
 * the {@link GroupListDialog}. See plans/09.
 *
 * @author Peter Dell
 */
public final class GroupSelectionPanel extends JPanel {

	private final GroupFilterPanel filterPanel;
	private final GroupListPanel listPanel;
	private List<Group> allGroups;
	private Map<String, List<Composer>> composersByFolderName;
	private Map<String, Integer> fileCountByFolderName;

	public GroupSelectionPanel(AttributeTablePreferences preferences) {
		super(new BorderLayout());
		allGroups = new ArrayList<Group>();
		composersByFolderName = new HashMap<String, List<Composer>>();
		fileCountByFolderName = new HashMap<String, Integer>();
		filterPanel = new GroupFilterPanel(selection -> applySelection(selection));
		listPanel = new GroupListPanel(preferences);
		listPanel.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(filterPanel, BorderLayout.NORTH);
		add(listPanel, BorderLayout.CENTER);
		applySelection(filterPanel.getSelection());
	}

	/**
	 * Shows the groups of the database, with their composers and file counts.
	 *
	 * @param database
	 *            The database, not <code>null</code>.
	 */
	public void setDatabase(ASMAClient.Database database) {
		if (database == null) {
			throw new IllegalArgumentException("Parameter 'database' must not be null.");
		}
		this.allGroups = (database.groupInfos != null) ? database.groupInfos : new ArrayList<Group>();
		this.composersByFolderName = database.getComposersByGroupFolderName();
		this.fileCountByFolderName = database.getFileCountByGroupFolderName();
		applySelection(filterPanel.getSelection());
	}

	private void applySelection(Group.Selection selection) {
		List<Group> selectedGroups = new ArrayList<Group>(allGroups.size());
		for (Group group : allGroups) {
			if (selection.matches(group)) {
				selectedGroups.add(group);
			}
		}
		listPanel.getTableModel().setGroups(selectedGroups, composersByFolderName, fileCountByFolderName);
		filterPanel.setCountText(selectedGroups.size(), allGroups.size());
	}

	/** The selected group, or <code>null</code>. */
	public Group getSelectedGroup() {
		JTable table = listPanel.getTable();
		int viewRow = table.getSelectedRow();
		if (viewRow < 0) {
			return null;
		}
		return listPanel.getTableModel().getGroup(table.convertRowIndexToModel(viewRow));
	}

	public JTable getTable() {
		return listPanel.getTable();
	}

	/** The field to focus first. */
	public JTextField getKeywordField() {
		return filterPanel.getKeywordField();
	}
}
