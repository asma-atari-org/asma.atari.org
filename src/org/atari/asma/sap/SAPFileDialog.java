package org.atari.asma.sap;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.atari.asma.util.FileDrop;
import org.atari.asma.util.FileUtility;
import org.atari.asma.util.MessageQueue;

class SAPFileDialog {

//	private SAPEditor editor;

	private static String TITLE = "SAP File Editor";

	private SAPFileEditor editor;
	private SAPFileLogic sapFileLogic;
	private MessageQueue messageQueue;

	private JFrame frame;
	private JButton openButton;
	private JButton saveButton;
	private JTextField inputFilePathTextField;
	private JTextField outputFilePathTextField;
	private JTextArea headerTextArea;
	private JTextArea bodyTextArea;
	private JTextArea messageTextArea;
	private JFileChooser fileChooser;

	// State
	private File inputFile;
	private File outputFile;

	private StringBuilder header;
	private SAPFile sapFile;

	public SAPFileDialog(SAPFileEditor editor, SAPFileLogic sapFileLogic, MessageQueue messageQueue) {

		this.editor = editor;
		this.sapFileLogic = sapFileLogic;
		this.messageQueue = messageQueue;

		// State
		inputFile = null;
		outputFile = null;
		header = new StringBuilder();
		sapFile = null;

		frame = new JFrame();
		frame.setTitle(TITLE);

		openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				performOpen();

			}

		});
		saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				performSave();

			}
		});
		var toolbar = new JToolBar();
		toolbar.add(openButton);
		toolbar.add(saveButton);

		frame.setLayout(new BorderLayout());

		var filePanel = new JPanel();
		filePanel.setLayout(new BorderLayout());

		inputFilePathTextField = new JTextField();
		inputFilePathTextField.setEditable(false);

		outputFilePathTextField = new JTextField();
		outputFilePathTextField.setEditable(false);

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

		var filePathsPanel = new JPanel();
		filePathsPanel.setLayout(new BorderLayout());
		filePathsPanel.add(inputFilePathTextField, BorderLayout.NORTH);
		filePathsPanel.add(outputFilePathTextField, BorderLayout.SOUTH);
		filePanel.add(filePathsPanel, BorderLayout.NORTH);

		filePanel.add(contentPanel, BorderLayout.CENTER);

		frame.add(toolbar, BorderLayout.NORTH);

		frame.add(filePanel, BorderLayout.CENTER);

		messageTextArea = new JTextArea();
		messageTextArea.setBorder(border);
		messageTextArea.setEditable(false);
		messageTextArea.setBackground(frame.getBackground());
		frame.add(messageTextArea, BorderLayout.SOUTH);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				System.exit(0);
			}
		});

		new FileDrop(frame, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				// handle file drop
				if (files.length == 1) {
					editor.processFile(files[0]);
				}
			} // end filesDropped
		}); // end FileDrop.Listener

		fileChooser = new JFileChooser();

	}

	public JFrame getFrame() {
		return frame;
	}

	private void dataToUI() {
		StringBuilder title = new StringBuilder(TITLE);
		if (sapFile.asapInfo != null) {
			title.append(" - ").append(sapFile.asapInfo.getTitle()).append(" by ").append(sapFile.asapInfo.getAuthor());
		} else {
			title.append(" - ").append(inputFile.getName());
		}

		saveButton.setEnabled(outputFile != null);
		frame.setTitle(title.toString());
		inputFilePathTextField.setText(inputFile.getAbsolutePath());
		if (outputFile != null) {
			outputFilePathTextField.setText(outputFile.getAbsolutePath());
		} else {
			outputFilePathTextField.setText("");
		}

		headerTextArea.setText(header.toString());
		bodyTextArea.setText(sapFile.segmentList.toString());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		headerTextArea.requestFocus();
		displayMessageQueue();
	}

	private void dataFromUI() {
	}

	public void show(File inputFile) {

		this.inputFile = inputFile;
		this.outputFile = null;
		header.setLength(0);
		messageQueue.sendInfo("Reading '" + inputFile.getAbsolutePath() + "'.");
		final var fileExtension = (FileUtility.getFileExtension(inputFile.getName()).toLowerCase());
		if (fileExtension.equals(".sap")) {
			sapFile = sapFileLogic.loadSAPFile(inputFile, messageQueue);
			outputFile = inputFile;
		} else if (SAPFile.isOriginalModuleFileExtension(fileExtension)) {
			sapFile = sapFileLogic.loadOriginalModuleFile(inputFile, messageQueue);
			if (sapFile != null) {
				outputFile = FileUtility.changeFileExtension(inputFile, ".sap");
				messageQueue.sendInfo("Converted ASAP comptible file '" + inputFile.getName() + "' to '"
						+ outputFile.getName() + "'.");

				if (outputFile.exists()) {
					messageQueue.sendWarning("Be careful when saving, the target file already exists.");
				}
			}
		} else if (fileExtension.equals(".xex")) {
			var writer = new StringWriter();
			sapFile = sapFileLogic.loadXEXFile(inputFile, new PrintWriter(writer), messageQueue);
			header.append(writer.toString());
			if (sapFile != null) {
				outputFile = FileUtility.changeFileExtension(inputFile, ".sap");
			} else {
				outputFile = null;

			}
		}

		if (sapFile == null) {
			messageQueue.sendInfo("Error reading '" + inputFile.getAbsolutePath() + "'. See above.");
		}

		dataToUI();
	}

	private void displayMessageQueue() {
		var builder = new StringBuilder();
		var entries = messageQueue.getEntries();
		for (var entry : entries) {

			builder.append(entry.getType().toString()).append(" : ").append(entry.getMessage());
			builder.append("\n");

		}
		messageTextArea.setText(builder.toString());
		messageQueue.clear();
	}

	private void performOpen() {
		dataFromUI();

		fileChooser.setCurrentDirectory(inputFile.getParentFile());
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			editor.processFile(fileChooser.getSelectedFile());
		}

		dataToUI();
	}

	private void performSave() {
		dataFromUI();
		if (sapFileLogic.saveSAPFile(outputFile, sapFile, messageQueue)) {
			messageQueue.sendInfo("SAP file '" + outputFile.getAbsolutePath() + "' saved.");
		}
		dataToUI();
	}

}