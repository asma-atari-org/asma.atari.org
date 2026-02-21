package org.atari.asma.sap;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class FilePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JLabel filePathLabel;

	public JTextField filePathField;

	public JLabel fileStatusLabel;
	public JLabel fileSizeLabel;
	public JLabel fileDateLabel;
	
	public JButton playButton;

	public FilePanel() {

		var border = BorderFactory.createEmptyBorder(1, 5, 1, 5);

		var layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
		setLayout(layout);
		filePathLabel = new JLabel();
		filePathLabel.setBorder(border);

		add(filePathLabel);

		filePathField = new JTextField();
		filePathField.setBorder(border);

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

		playButton=new JButton("Play");
		add(playButton);
		
		playButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				var  desktop = Desktop.getDesktop();
				try {
					desktop.open(new File(filePathField.getText()));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});

		setEditable(false);
	}

	public void setEditable(boolean editable) {
		filePathField.setEditable(editable);
	}

	public void setFile(File file) {
		var format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		if (file != null) {
			filePathField.setText(file.getAbsolutePath());
			if (file.exists()) {
				if (file.isFile()) {
					fileStatusLabel.setText("Exists");
					fileSizeLabel.setText(String.valueOf(file.length()) + " b");
					fileDateLabel.setText(format.format(new Date(file.lastModified())));
					playButton.setEnabled(true);

				} else {
					fileStatusLabel.setText("Directory");
					fileSizeLabel.setText("");
					fileDateLabel.setText("");
					playButton.setEnabled(false);

				}
			} else {
				fileStatusLabel.setText("New");
				playButton.setEnabled(false);

			}
		} else {
			filePathField.setText("");
			fileStatusLabel.setText("");
			fileSizeLabel.setText("");
			fileDateLabel.setText("");
			
			playButton.setEnabled(false);

		}

	}

}
