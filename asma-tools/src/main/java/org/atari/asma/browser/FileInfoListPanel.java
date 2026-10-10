package org.atari.asma.browser;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.atari.asma.FileInfo;

import com.wudsn.tools.base.common.StringUtility;
import com.wudsn.tools.base.gui.AttributeTableModel;
import com.wudsn.tools.base.gui.AttributeTablePanel;
import com.wudsn.tools.base.gui.AttributeTablePreferences;

/**
 * Panel displaying a list of {@link FileInfo} instances in a
 * column-configurable, sortable attribute table, following the pattern of
 * The!Cart Studio's WorkbookEntriesPanel.
 *
 * @author Peter Dell
 */
public final class FileInfoListPanel extends AttributeTablePanel {

	public static final class TableModel extends AttributeTableModel {

		public static final class Columns {
			public static final int TITLE = 0;
			public static final int AUTHOR = 1;
			public static final int DATE = 2;
			public static final int FORMAT = 3;
			public static final int CHANNELS = 4;
			public static final int SONGS = 5;
			public static final int FILE_PATH = 6;
			public static final int FILE_SIZE = 7;
			public static final int HARDWARE = 8;
			public static final int DEFAULT_SONG = 9;
			public static final int DEMOZOO_ID = 10;

			private Columns() {
			}
		}

		private List<FileInfo> fileInfos;

		public TableModel() {
			fileInfos = new ArrayList<FileInfo>();
			addColumn(FileInfo.Attributes.TITLE, Column.FIXED | Column.SORTABLE,
					StringUtility.CASE_INSENSITIVE_COMPARATOR, null, null);
			addColumn(FileInfo.Attributes.AUTHOR, Column.VISIBLE | Column.SORTABLE,
					StringUtility.CASE_INSENSITIVE_COMPARATOR, null, null);
			addColumn(FileInfo.Attributes.DATE, Column.VISIBLE | Column.SORTABLE);
			addColumn(FileInfo.Attributes.ORIGINAL_MODULE_EXT, Column.VISIBLE | Column.SORTABLE);
			// The value stays numeric for sorting; the renderer shows the website's text.
			addColumn(FileInfo.Attributes.CHANNELS, Column.VISIBLE | Column.SORTABLE, null,
					new DefaultTableCellRenderer() {
						@Override
						public Component getTableCellRendererComponent(JTable table, Object value,
								boolean isSelected, boolean hasFocus, int row, int column) {
							super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
							setText(getFileInfo(table.convertRowIndexToModel(row)).getChannelsText());
							return this;
						}
					}, null);
			addColumn(FileInfo.Attributes.SONGS, Column.VISIBLE | Column.SORTABLE);
			addColumn(FileInfo.Attributes.FILE_PATH, Column.HIDDEN | Column.SORTABLE,
					StringUtility.CASE_INSENSITIVE_COMPARATOR, null, null);
			addColumn(FileInfo.Attributes.FILE_SIZE, Column.HIDDEN | Column.SORTABLE);
			addColumn(FileInfo.Attributes.HARDWARE, Column.HIDDEN | Column.SORTABLE);
			addColumn(FileInfo.Attributes.DEFAULT_SONG_INDEX, Column.HIDDEN | Column.SORTABLE);
			addColumn(FileInfo.Attributes.DEMOZOO_ID, Column.HIDDEN | Column.SORTABLE);
		}

		public void setFileInfos(List<FileInfo> fileInfos) {
			if (fileInfos == null) {
				throw new IllegalArgumentException("Parameter 'fileInfos' must not be null.");
			}
			this.fileInfos = fileInfos;
			fireTableDataChanged();
		}

		public FileInfo getFileInfo(int row) {
			return fileInfos.get(row);
		}

		@Override
		public int getRowCount() {
			return fileInfos.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			FileInfo fileInfo = fileInfos.get(row);
			switch (column) {
			case Columns.TITLE:
				return fileInfo.title;
			case Columns.AUTHOR:
				return fileInfo.author;
			case Columns.DATE:
				return fileInfo.date;
			case Columns.FORMAT:
				return fileInfo.originalModuleExt;
			case Columns.CHANNELS:
				return Integer.valueOf(fileInfo.channels);
			case Columns.SONGS:
				return Integer.valueOf(fileInfo.songs);
			case Columns.FILE_PATH:
				return fileInfo.filePath;
			case Columns.FILE_SIZE:
				return Long.valueOf(fileInfo.fileSize);
			case Columns.HARDWARE:
				return fileInfo.hardware;
			case Columns.DEFAULT_SONG:
				return Integer.valueOf(fileInfo.defaultSongIndex + 1);
			case Columns.DEMOZOO_ID:
				return Integer.valueOf(fileInfo.getDemozooID());
			default:
				throw new IllegalArgumentException("Invalid column " + column + ".");
			}
		}
	}

	private final TableModel tableModel;

	public FileInfoListPanel(AttributeTablePreferences preferences) {
		this(new TableModel(), preferences);
	}

	private FileInfoListPanel(TableModel tableModel, AttributeTablePreferences preferences) {
		super(tableModel, preferences, "fileInfoList");
		this.tableModel = tableModel;
	}

	public TableModel getTableModel() {
		return tableModel;
	}
}
