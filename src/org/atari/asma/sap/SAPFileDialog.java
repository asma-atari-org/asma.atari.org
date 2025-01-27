package org.atari.asma.sap;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.atari.asma.util.FileDrop;
import org.atari.asma.util.MessageQueue;

class SAPFileDialog {

//	private SAPEditor editor;

	private static String TITLE = "SAP File Editor";

	private JFrame frame;
	private JTextField filePathTextField;
	private JTextArea headerTextArea;
	private JTextArea bodyTextArea;
	private JTextArea messageTextArea;

	private JFileChooser fileChooser;

	private File file;
	private SAPFile sapFile;

	public SAPFileDialog(SAPFileEditor editor) {

//		this.editor = editor;
		frame = new JFrame();
		frame.setTitle(TITLE);

		var openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				fileChooser.setCurrentDirectory(file.getParentFile());
				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setMultiSelectionEnabled(true);
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					editor.runFiles(fileChooser.getSelectedFiles());
				}

			}
		});
		var saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File outputFile = new File(file.getParentFile(), "_" + file.getName());
				sapFile.header = headerTextArea.getText();
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(outputFile);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
					return;
				}
				try {
					for (int i = 0; i < sapFile.header.length(); i++) {
						char c = sapFile.header.charAt(i);
						if (c == 0x0a) {
							fos.write(0x0d);
							fos.write(0x0a);
						} else {
							fos.write(c);
						}

					}
					fos.write(sapFile.content, sapFile.segmentsStartIndex,
							sapFile.content.length - sapFile.segmentsStartIndex);
					System.out.println("SAP file '" + outputFile.getAbsolutePath() + "' saved.");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
					try {
						fos.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			}
		});
		var toolbar = new JToolBar();
		toolbar.add(openButton);
		toolbar.add(saveButton);

		frame.setLayout(new BorderLayout());

		var filePanel = new JPanel();
		filePanel.setLayout(new BorderLayout());

		filePathTextField = new JTextField();
		filePathTextField.setEditable(false);

		var contentPanel = new JPanel();
		var layout = new GridLayout();
		layout.setColumns(2);
		contentPanel.setLayout(layout);
		headerTextArea = new JTextArea();
		var border = new EmptyBorder(5, 5, 5, 5);
		headerTextArea.setBorder(border);
		contentPanel.add(headerTextArea);

		bodyTextArea = new JTextArea();
		bodyTextArea.setBorder(border);
		bodyTextArea.setEditable(false);
		bodyTextArea.setBackground(frame.getBackground());
		contentPanel.add(bodyTextArea);

		messageTextArea = new JTextArea();
		messageTextArea.setBorder(border);
		messageTextArea.setEditable(false);
		messageTextArea.setBackground(frame.getBackground());
		contentPanel.add(messageTextArea);

		filePanel.add(filePathTextField, BorderLayout.NORTH);
		filePanel.add(contentPanel, BorderLayout.CENTER);

		frame.add(toolbar, BorderLayout.NORTH);

		frame.add(filePanel, BorderLayout.CENTER);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				System.exit(0);
			}
		});

		new FileDrop(frame, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				// handle file drop
				editor.runFiles(files);
			} // end filesDropped
		}); // end FileDrop.Listener

		fileChooser = new JFileChooser();
	}

	public JFrame getFrame() {
		return frame;
	}

	void show(File file, SAPFile sapFile) {
		this.file = file;
		this.sapFile = sapFile;

		StringBuilder title = new StringBuilder(TITLE);
		if (sapFile.asapInfo != null) {
			title.append(" - ").append(sapFile.asapInfo.getTitle()).append(" by ").append(sapFile.asapInfo.getAuthor());
		} else {
			title.append(" - ").append(file.getName());
		}
		frame.setTitle(title.toString());
		filePathTextField.setText(file.getAbsolutePath());
		headerTextArea.setText(sapFile.header);
		bodyTextArea.setText(sapFile.getSegmentsString());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		headerTextArea.requestFocus();
	}

	void displayMessageQueue(MessageQueue messageQueue) {
//		messageTextArea=messageQueue.toString();
	}

}