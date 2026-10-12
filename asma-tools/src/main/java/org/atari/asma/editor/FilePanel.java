package org.atari.asma.editor;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.wudsn.tools.base.gui.Desktop;
import com.wudsn.tools.base.gui.ElementFactory;

/**
 * A file or folder path with its status, size and date, and a button to open
 * it with the desktop.
 */
public final class FilePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final boolean fileMode;
	private final JTextField filePathField;
	private final JLabel fileStatusLabel;
	private final JLabel fileSizeLabel;
	private final JLabel fileDateLabel;
	private final JButton button;

	/**
	 * @param label
	 *            The label, not <code>null</code>.
	 * @param fileMode
	 *            <code>true</code> for a file, <code>false</code> for a folder.
	 */
	public FilePanel(String label, boolean fileMode) {
		this.fileMode = fileMode;

		var border = BorderFactory.createEmptyBorder(1, 5, 1, 5);
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		var filePathLabel = new JLabel(label);
		filePathLabel.setBorder(border);
		add(filePathLabel);

		filePathField = new JTextField();
		filePathField.setBorder(border);
		filePathField.setEditable(false);
		add(filePathField);

		fileStatusLabel = new JLabel();
		fileStatusLabel.setBorder(border);
		add(fileStatusLabel);

		fileSizeLabel = new JLabel();
		fileSizeLabel.setBorder(border);
		add(fileSizeLabel);

		fileDateLabel = new JLabel();
		fileDateLabel.setBorder(border);
		add(fileDateLabel);

		// The same buttons appear in several panels, so they get no mnemonic.
		button = ElementFactory.createButton(fileMode ? Actions.FilePanel_Play : Actions.FilePanel_OpenFolder,
				false);
		button.addActionListener(event -> {
			try {
				Desktop.openFile(new File(filePathField.getText()));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
		add(button);
	}

	public void setFile(File file) {
		var format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		if (file != null) {
			filePathField.setText(file.getAbsolutePath());
			if (fileMode) {
				if (file.exists()) {
					if (file.isFile()) {
						fileStatusLabel.setText(Texts.FilePanel_Exists);
						fileSizeLabel.setText(String.valueOf(file.length()) + " b");
						fileDateLabel.setText(format.format(new Date(file.lastModified())));
						button.setEnabled(true);
					} else {
						fileStatusLabel.setText(Texts.FilePanel_Directory);
						fileSizeLabel.setText("");
						fileDateLabel.setText("");
						button.setEnabled(false);
					}
				} else {
					fileStatusLabel.setText(Texts.FilePanel_New);
					button.setEnabled(false);
				}
			} else {
				button.setEnabled(true);
			}
		} else {
			filePathField.setText("");
			fileStatusLabel.setText("");
			fileSizeLabel.setText("");
			fileDateLabel.setText("");
			button.setEnabled(false);
		}
	}
}
