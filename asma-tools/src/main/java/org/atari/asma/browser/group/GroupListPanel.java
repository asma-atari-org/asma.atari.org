package org.atari.asma.browser.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.SwingConstants;

import org.atari.asma.Composer;
import org.atari.asma.Group;
import org.atari.asma.browser.PlainNumberCellRenderer;

import com.wudsn.tools.base.common.StringUtility;
import com.wudsn.tools.base.gui.AttributeTableModel;
import com.wudsn.tools.base.gui.AttributeTablePanel;
import com.wudsn.tools.base.gui.AttributeTablePreferences;

/**
 * Panel displaying a list of {@link Group} instances in a
 * column-configurable, sortable attribute table, built like the composer
 * list. Groups are the folders in "Groups/" for music made by several
 * composers together. See plans/09.
 *
 * @author Peter Dell
 */
public final class GroupListPanel extends AttributeTablePanel {

	public static final class TableModel extends AttributeTableModel {

		public static final class Columns {
			public static final int NAME = 0;
			public static final int FOLDER_NAME = 1;
			public static final int COMPOSERS = 2;
			public static final int FILE_COUNT = 3;
			public static final int DEMOZOO_ID = 4;

			private Columns() {
			}
		}

		private List<Group> groups;
		private Map<String, List<Composer>> composersByFolderName;
		private Map<String, Integer> fileCountByFolderName;

		public TableModel() {
			groups = new ArrayList<Group>();
			composersByFolderName = new HashMap<String, List<Composer>>();
			fileCountByFolderName = new HashMap<String, Integer>();
			addColumn(Group.Attributes.NAME, Column.VISIBLE | Column.SORTABLE,
					StringUtility.CASE_INSENSITIVE_COMPARATOR, null, null);
			// Fixed, because only some groups have a name.
			addColumn(Group.Attributes.FOLDER_NAME, Column.FIXED | Column.SORTABLE,
					StringUtility.CASE_INSENSITIVE_COMPARATOR, null, null);
			addColumn(Group.Attributes.COMPOSERS, Column.VISIBLE | Column.SORTABLE,
					StringUtility.CASE_INSENSITIVE_COMPARATOR, null, null);
			addColumn(Group.Attributes.FILE_COUNT, Column.VISIBLE | Column.SORTABLE);
			addColumn(Group.Attributes.DEMOZOO_ID, Column.VISIBLE | Column.SORTABLE, null,
					new PlainNumberCellRenderer(SwingConstants.RIGHT), null);
		}

		/**
		 * @param groups
		 *            The groups, not <code>null</code>.
		 * @param composersByFolderName
		 *            The composers per group folder name, not
		 *            <code>null</code>.
		 * @param fileCountByFolderName
		 *            The number of files per group folder name, not
		 *            <code>null</code>.
		 */
		public void setGroups(List<Group> groups, Map<String, List<Composer>> composersByFolderName,
				Map<String, Integer> fileCountByFolderName) {
			if (groups == null) {
				throw new IllegalArgumentException("Parameter 'groups' must not be null.");
			}
			if (composersByFolderName == null) {
				throw new IllegalArgumentException("Parameter 'composersByFolderName' must not be null.");
			}
			if (fileCountByFolderName == null) {
				throw new IllegalArgumentException("Parameter 'fileCountByFolderName' must not be null.");
			}
			this.groups = groups;
			this.composersByFolderName = composersByFolderName;
			this.fileCountByFolderName = fileCountByFolderName;
			fireTableDataChanged();
		}

		public Group getGroup(int row) {
			return groups.get(row);
		}

		@Override
		public int getRowCount() {
			return groups.size();
		}

		private static Integer parseDemozooID(String demozooID) {
			try {
				return Integer.valueOf(demozooID.trim());
			} catch (NumberFormatException | NullPointerException ex) {
				return null;
			}
		}

		// "First Last" in the Unicode spelling when there is one, the handles for unknown names.
		private static String getComposerName(Composer composer) {
			String lastName = composer.lastNameUnicode.isEmpty() ? composer.lastName : composer.lastNameUnicode;
			String firstName = composer.firstNameUnicode.isEmpty() ? composer.firstName : composer.firstNameUnicode;
			if (lastName.isEmpty() || lastName.equals("<?>")) {
				return composer.handles;
			}
			return (firstName.isEmpty() || firstName.equals("<?>")) ? lastName : firstName + " " + lastName;
		}

		@Override
		public Object getValueAt(int row, int column) {
			Group group = groups.get(row);
			switch (column) {
			case Columns.FOLDER_NAME:
				return group.folderName;
			case Columns.NAME:
				return group.name;
			case Columns.COMPOSERS:
				return composersByFolderName.getOrDefault(group.folderName, List.of()).stream()
						.map(TableModel::getComposerName).collect(Collectors.joining(", "));
			case Columns.FILE_COUNT:
				return fileCountByFolderName.getOrDefault(group.folderName, Integer.valueOf(0));
			case Columns.DEMOZOO_ID:
				return parseDemozooID(group.demozooID);
			default:
				throw new IllegalArgumentException("Invalid column " + column + ".");
			}
		}
	}

	private final TableModel tableModel;

	public GroupListPanel(AttributeTablePreferences preferences) {
		this(new TableModel(), preferences);
	}

	private GroupListPanel(TableModel tableModel, AttributeTablePreferences preferences) {
		super(tableModel, preferences, "groupList");
		this.tableModel = tableModel;
	}

	public TableModel getTableModel() {
		return tableModel;
	}
}
