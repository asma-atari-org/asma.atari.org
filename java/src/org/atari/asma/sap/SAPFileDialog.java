package org.atari.asma.sap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.atari.asma.util.FileDrop;
import org.atari.asma.util.FileUtility;
import org.atari.asma.util.MessageQueue;

class SAPFileDialog {

//	private SAPEditor editor;

	private static String TITLE = "SAP File Editor";

	private SAPFileProcessor fileProcessor;
	private ASAPFileLogic asapFileLogic;
	private MessageQueue messageQueue;

	private JFrame frame;
	private JButton openButton;
	private JButton saveButton;
	private FilePanel inputFilePanel;
	private FilePanel outputFilePanel;
	private JTextArea inputFileStructureTextArea;
	private JTextArea analysisTextArea;
	private ASAPPanel asapPanel;
	private JTextArea messageTextArea;
	private JFileChooser fileChooser;

	// State
	private File inputFile;
	private File outputFile;

	private StringBuilder header;
	private ASAPFile asapFile;

	@SuppressWarnings("serial")
	private final class SaveAction extends AbstractAction {

		public SaveAction(String text, ImageIcon icon, String description, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, description);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			performSave();
		}

	};

	public SAPFileDialog(SAPFileProcessor editor) {

		this.fileProcessor = editor;
		this.asapFileLogic = new ASAPFileLogic();
		this.messageQueue = new MessageQueue();

		// State
		inputFile = new File("");
		outputFile = null;
		header = new StringBuilder();
		asapFile = new ASAPFile();

		frame = new JFrame();
		frame.setTitle(TITLE);

		openButton = new JButton("Open");
		openButton.setToolTipText("Open a new input file.");

		openButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				performOpen();

			}

		});

		var saveAction = new SaveAction("Save", null, "Save the current output file", null);

		saveButton = new JButton(saveAction);

		var toolbar = new JToolBar();
		toolbar.add(openButton);
		toolbar.add(saveButton);

		frame.setLayout(new BorderLayout());

		var filePanel = new JPanel();
		filePanel.setLayout(new BorderLayout());

		inputFilePanel = new FilePanel();
		inputFilePanel.filePathLabel.setText("Input File");

		outputFilePanel = new FilePanel();
		outputFilePanel.filePathLabel.setText("Output File");

		var contentPanel = new JPanel();
		var layout = new GridLayout();
		layout.setColumns(2);
		contentPanel.setLayout(layout);
		var border = new EmptyBorder(5, 5, 5, 5);

		inputFileStructureTextArea = new JTextArea();
		inputFileStructureTextArea.setBorder(border);
		inputFileStructureTextArea.setEditable(false);
		inputFileStructureTextArea.setBackground(new Color(204, 204, 204));
		contentPanel.add(inputFileStructureTextArea);

		analysisTextArea = new JTextArea();
		analysisTextArea.setBorder(border);
		analysisTextArea.setEditable(false);
		analysisTextArea.setBackground(new Color(224, 224, 224));
		contentPanel.add(analysisTextArea);

		asapPanel = new ASAPPanel();
		asapPanel.setBackground(inputFileStructureTextArea.getBackground());

		contentPanel.add(asapPanel);

		var filePathsPanel = new JPanel();
		filePathsPanel.setLayout(new BorderLayout());
		filePathsPanel.add(inputFilePanel, BorderLayout.NORTH);
		filePathsPanel.add(outputFilePanel, BorderLayout.SOUTH);
		filePanel.add(filePathsPanel, BorderLayout.NORTH);

		filePanel.add(contentPanel, BorderLayout.CENTER);

		frame.add(toolbar, BorderLayout.NORTH);

		frame.add(filePanel, BorderLayout.CENTER);

		messageTextArea = new JTextArea();
		messageTextArea.setBorder(border);
		messageTextArea.setEditable(false);
		messageTextArea.setBackground(frame.getBackground());
		frame.add(messageTextArea, BorderLayout.SOUTH);

		/*
		 * KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(
		 * new KeyEventDispatcher() {
		 * 
		 * @Override public boolean dispatchKeyEvent(KeyEvent e) { if (e.isControlDown()
		 * && e.getKeyCode() == KeyEvent.VK_S) { e.consume(); performSave(); return
		 * true; } return false; } });
		 */

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {

				editor.closeFile(inputFile);
			}
		});

		new FileDrop(frame, new FileDrop.Listener() {
			public void filesDropped(java.io.File[] files) {
				// handle file drop
				for (var file : files) {
					editor.processFile(file);
				}
			} // end filesDropped
		}); // end FileDrop.Listener

		fileChooser = new JFileChooser();

		dataToUI();
		frame.pack();
		frame.setVisible(false);

	}

	public JFrame getFrame() {
		return frame;
	}

	private void dataToUI() {
		var title = new StringBuilder(TITLE).append(" - ").append(inputFile.getName()).append(" - ");

		title.append(asapFile.getTitle()).append(" by ").append(asapFile.getAuthor());

		saveButton.setEnabled(outputFile != null && asapFile.getASAPInfo()!=null);
		frame.setTitle(title.toString());
		inputFilePanel.setFile(inputFile);
		outputFilePanel.setFile(outputFile);

		analysisTextArea.setText(header.toString());
		inputFileStructureTextArea.setText(asapFile.segmentList.toString());

		asapPanel.dataToUI(asapFile, outputFile);

		asapPanel.requestFocus();
		displayMessageQueue();
	}

	private void dataFromUI() {
		messageQueue.clear();
		asapPanel.dataFromUI(asapFile);
	}

	public void show(File inputFile) {
		ASAPFile newASAPFile = null;
		this.inputFile = inputFile;
		this.outputFile = null;
		header.setLength(0);
		messageQueue.sendInfo("Reading '" + inputFile.getAbsolutePath() + "'.");
		final var fileExtension = (FileUtility.getFileExtension(inputFile.getName()).toLowerCase());
		if (fileExtension.equals(".sap")) {
			newASAPFile = asapFileLogic.loadSAPFile(inputFile, messageQueue);
			outputFile = inputFile;
		} else if (ASAPFile.isOriginalModuleFileExtension(fileExtension)) {
			newASAPFile = asapFileLogic.loadOriginalModuleFile(inputFile, messageQueue);
			if (newASAPFile != null) {
				outputFile = FileUtility.changeFileExtension(inputFile, ".sap");
				messageQueue.sendInfo("Converted ASAP compatible file '" + inputFile.getName() + "' to '"
						+ outputFile.getName() + "'.");

				if (outputFile.exists()) {
					messageQueue.sendWarning("Be careful when saving, the target file already exists.");
				}
			}
		} else if (fileExtension.equals(".xex")) {
			var writer = new StringWriter();
			newASAPFile = asapFileLogic.loadXEXFile(fileProcessor, inputFile, new PrintWriter(writer), messageQueue);
			header.append(writer.toString());
			if (asapFile != null) {
				outputFile = FileUtility.changeFileExtension(inputFile, ".sap");
			} else {
				outputFile = null;

			}
		} else {
			messageQueue.sendError("Unsupported file extension or file type.");
		}

		if (newASAPFile != null) {
			this.asapFile = newASAPFile;
		} else {
			messageQueue.sendInfo("Error reading '" + inputFile.getAbsolutePath() + "'. See above.");
		}

		dataToUI();

		if (!frame.isVisible()) {
			frame.pack();
			frame.setVisible(true);

		}
	}

	private void displayMessageQueue() {
		var builder = new StringBuilder();
		var entries = messageQueue.getEntries();
		for (var entry : entries) {

			builder.append(entry.getType().toString()).append(" : ").append(entry.getMessage());
			builder.append("\n");

		}
		messageTextArea.setText(builder.toString());
	}

	private void performOpen() {
		dataFromUI();

		fileChooser.setCurrentDirectory(inputFile.getParentFile());
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			fileProcessor.processFile(fileChooser.getSelectedFile());
		}

		dataToUI();
	}

	private void performSave() {
		dataFromUI();
		if (asapFileLogic.saveSAPFile(outputFile, asapFile, messageQueue)) {
			messageQueue.sendInfo("SAP file '" + outputFile.getAbsolutePath() + "' saved.");
			outputFilePanel.setFile(outputFile);
		}

		dataToUI();

	}

}