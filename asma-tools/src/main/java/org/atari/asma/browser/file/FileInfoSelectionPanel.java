package org.atari.asma.browser.file;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.atari.asma.FileInfo;

import com.wudsn.tools.base.gui.AttributeTable;
import com.wudsn.tools.base.gui.AttributeTablePreferences;

/**
 * Reusable file list: the {@link FileInfoFilterPanel} above the
 * {@link FileInfoListPanel}, the counterpart of the composer selection panel.
 * See plans/09.
 *
 * @author Peter Dell
 */
public final class FileInfoSelectionPanel extends JPanel {

	private final FileInfoFilterPanel filterPanel;
	private final FileInfoListPanel listPanel;
	private final List<Runnable> selectionListeners;
	private final Random random;
	private List<FileInfo> allFileInfos;

	public FileInfoSelectionPanel(AttributeTablePreferences preferences) {
		super(new BorderLayout());
		allFileInfos = new ArrayList<FileInfo>();
		selectionListeners = new ArrayList<Runnable>();
		random = new Random();
		filterPanel = new FileInfoFilterPanel(selection -> applySelection(selection));
		listPanel = new FileInfoListPanel(preferences);
		listPanel.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listPanel.getTable().getSelectionModel().addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				fireSelectionChanged();
			}
		});
		add(filterPanel, BorderLayout.NORTH);
		add(listPanel, BorderLayout.CENTER);
		applySelection(filterPanel.getSelection());
	}

	/**
	 * @param fileInfos
	 *            The file infos, not <code>null</code>.
	 */
	public void setFileInfos(List<FileInfo> fileInfos) {
		if (fileInfos == null) {
			throw new IllegalArgumentException("Parameter 'fileInfos' must not be null.");
		}
		this.allFileInfos = fileInfos;
		applySelection(filterPanel.getSelection());
	}

	private void applySelection(FileInfo.Selection selection) {
		List<FileInfo> selectedFileInfos = new ArrayList<FileInfo>(allFileInfos.size());
		for (FileInfo fileInfo : allFileInfos) {
			if (selection.matches(fileInfo)) {
				selectedFileInfos.add(fileInfo);
			}
		}
		listPanel.getTableModel().setFileInfos(selectedFileInfos);
		filterPanel.setCountText(selectedFileInfos.size(), allFileInfos.size());
		fireSelectionChanged();
	}

	/** Called whenever the selected row may have changed, also by filtering. */
	public void addSelectionListener(Runnable listener) {
		if (listener == null) {
			throw new IllegalArgumentException("Parameter 'listener' must not be null.");
		}
		selectionListeners.add(listener);
	}

	private void fireSelectionChanged() {
		for (Runnable listener : selectionListeners) {
			listener.run();
		}
	}

	/** The selected file info, or <code>null</code>. */
	public FileInfo getSelectedFileInfo() {
		JTable table = listPanel.getTable();
		int viewRow = table.getSelectedRow();
		if (viewRow < 0) {
			return null;
		}
		return listPanel.getTableModel().getFileInfo(table.convertRowIndexToModel(viewRow));
	}

	/** Selects a random row of the filtered list and returns its file info, or <code>null</code>. */
	public FileInfo selectRandomFileInfo() {
		var tableModel = listPanel.getTableModel();
		int rowCount = tableModel.getRowCount();
		if (rowCount == 0) {
			return null;
		}
		int modelRow = random.nextInt(rowCount);
		JTable table = listPanel.getTable();
		int viewRow = table.convertRowIndexToView(modelRow);
		table.setRowSelectionInterval(viewRow, viewRow);
		table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
		return tableModel.getFileInfo(modelRow);
	}

	public AttributeTable getTable() {
		return listPanel.getTable();
	}

	/** The field to focus first. */
	public JTextField getKeywordField() {
		return filterPanel.getKeywordField();
	}
}
