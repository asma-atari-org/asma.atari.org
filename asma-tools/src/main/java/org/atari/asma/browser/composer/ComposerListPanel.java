package org.atari.asma.browser.composer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingConstants;

import org.atari.asma.Composer;
import org.atari.asma.browser.PlainNumberCellRenderer;
import org.atari.asma.demozoo.DemozooID;

import com.wudsn.tools.base.common.StringUtility;
import com.wudsn.tools.base.gui.AttributeTableModel;
import com.wudsn.tools.base.gui.AttributeTablePanel;
import com.wudsn.tools.base.gui.AttributeTablePreferences;

/**
 * Panel displaying a list of {@link Composer} instances in a
 * column-configurable, sortable attribute table, built like the
 * file info list of the ASMABrowser. The columns follow the Composers tab of
 * the website player's About dialog.
 *
 * @author Peter Dell
 */
public final class ComposerListPanel extends AttributeTablePanel {

	public static final class TableModel extends AttributeTableModel {

		public static final class Columns {
			public static final int FIRST_NAME = 0;
			public static final int LAST_NAME = 1;
			public static final int HANDLES = 2;
			public static final int FOLDER_NAME = 3;
			public static final int COUNTRY = 4;
			public static final int DEMOZOO_ID = 5;
			public static final int FILE_COUNT = 6;
			public static final int GROUPS = 7;

			private Columns() {
			}
		}

		private List<Composer> composers;
		private Map<String, Integer> fileCountByFolderName;

		public TableModel() {
			composers = new ArrayList<Composer>();
			fileCountByFolderName = new HashMap<String, Integer>();
			addColumn(Composer.Attributes.FIRST_NAME, Column.VISIBLE | Column.SORTABLE,
					StringUtility.CASE_INSENSITIVE_COMPARATOR, null, null);
			addColumn(Composer.Attributes.LAST_NAME, Column.FIXED | Column.SORTABLE,
					StringUtility.CASE_INSENSITIVE_COMPARATOR, null, null);
			addColumn(Composer.Attributes.HANDLES, Column.VISIBLE | Column.SORTABLE,
					StringUtility.CASE_INSENSITIVE_COMPARATOR, null, null);
			addColumn(Composer.Attributes.FOLDER_NAME, Column.VISIBLE | Column.SORTABLE,
					StringUtility.CASE_INSENSITIVE_COMPARATOR, null, null);
			addColumn(Composer.Attributes.COUNTRY, Column.VISIBLE | Column.SORTABLE,
					StringUtility.CASE_INSENSITIVE_COMPARATOR, null, null);
			addColumn(Composer.Attributes.DEMOZOO_ID, Column.VISIBLE | Column.SORTABLE, null,
					new PlainNumberCellRenderer(SwingConstants.RIGHT), null);
			addColumn(Composer.Attributes.FILE_COUNT, Column.VISIBLE | Column.SORTABLE);
			addColumn(Composer.Attributes.GROUPS, Column.HIDDEN | Column.SORTABLE,
					StringUtility.CASE_INSENSITIVE_COMPARATOR, null, null);
		}

		/**
		 * @param composers
		 *            The composers, not <code>null</code>.
		 * @param fileCountByFolderName
		 *            The number of files per composer folder name, or
		 *            <code>null</code> if unknown; the file count column is
		 *            empty then.
		 */
		public void setComposers(List<Composer> composers, Map<String, Integer> fileCountByFolderName) {
			if (composers == null) {
				throw new IllegalArgumentException("Parameter 'composers' must not be null.");
			}
			this.composers = composers;
			this.fileCountByFolderName = fileCountByFolderName;
			fireTableDataChanged();
		}

		public Composer getComposer(int row) {
			return composers.get(row);
		}

		@Override
		public int getRowCount() {
			return composers.size();
		}

		// Like the website: the Unicode spelling when there is one.
		private static String getUnicodeOrDefault(String unicodeValue, String value) {
			return (unicodeValue != null && !unicodeValue.isEmpty()) ? unicodeValue : value;
		}

		@Override
		public Object getValueAt(int row, int column) {
			Composer composer = composers.get(row);
			switch (column) {
			case Columns.LAST_NAME:
				return getUnicodeOrDefault(composer.lastNameUnicode, composer.lastName);
			case Columns.FIRST_NAME:
				return getUnicodeOrDefault(composer.firstNameUnicode, composer.firstName);
			case Columns.HANDLES:
				return composer.handles;
			case Columns.COUNTRY:
				return composer.country;
			case Columns.DEMOZOO_ID:
				return DemozooID.toInteger(composer.getDemozooID());
			case Columns.FILE_COUNT:
				return (fileCountByFolderName == null) ? null
						: fileCountByFolderName.getOrDefault(composer.folderName, Integer.valueOf(0));
			case Columns.FOLDER_NAME:
				return composer.folderName;
			case Columns.GROUPS:
				return composer.groups;
			default:
				throw new IllegalArgumentException("Invalid column " + column + ".");
			}
		}
	}

	private final TableModel tableModel;

	public ComposerListPanel(AttributeTablePreferences preferences) {
		this(new TableModel(), preferences);
	}

	private ComposerListPanel(TableModel tableModel, AttributeTablePreferences preferences) {
		super(tableModel, preferences, "composerList");
		this.tableModel = tableModel;
	}

	public TableModel getTableModel() {
		return tableModel;
	}
}
