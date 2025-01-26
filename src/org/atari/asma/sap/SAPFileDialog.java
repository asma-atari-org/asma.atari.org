package org.atari.asma.sap;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

class SAPFileDialog {

	private JFrame frame;
	private JLabel filePathLabel;
	private JTextArea headerTextArea;
	private JTextArea bodyTextArea;

	public SAPFileDialog() {

		frame = new JFrame();
		frame.setTitle("SAP Editor");

		frame.setLayout(new BorderLayout());

		filePathLabel = new JLabel();

		var mainPanel = new JPanel();
		var layout = new GridLayout();
		layout.setColumns(2);
		mainPanel.setLayout(layout);
		headerTextArea = new JTextArea();
		var border = new EmptyBorder(5, 5, 5, 5);
		headerTextArea.setBorder(border);
		mainPanel.add(headerTextArea);
		bodyTextArea = new JTextArea();
		bodyTextArea.setBorder(border);

		bodyTextArea.setEditable(false);
		bodyTextArea.setBackground(frame.getBackground());
		mainPanel.add(bodyTextArea);

		frame.add(filePathLabel, BorderLayout.NORTH);
		frame.add(mainPanel, BorderLayout.CENTER);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				System.exit(0);
			}
		});
	}

	void show(File file, SAPFile sapFile) {
		filePathLabel.setText(file.getAbsolutePath());
		headerTextArea.setText(sapFile.header);
		bodyTextArea.setText(sapFile.getSegmentsString());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		headerTextArea.requestFocus();
	}

}