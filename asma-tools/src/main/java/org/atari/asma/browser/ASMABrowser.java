package org.atari.asma.browser;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.atari.asma.ASMAClient;
import org.atari.asma.FileInfo;
import org.atari.asma.composer.ComposerListDialog;

import com.wudsn.tools.base.common.Application;
import com.wudsn.tools.base.common.MessageQueue;
import com.wudsn.tools.base.common.MessageQueueEntry;
import com.wudsn.tools.base.gui.AttributeTablePreferences;
import com.wudsn.tools.base.gui.ElementFactory;
import com.wudsn.tools.base.gui.StatusBar;

/**
 * ASMABrowser - browses the database of the Atari SAP Music Archive
 * (https://asma.atari.org) in a details panel and an attribute table,
 * loaded via the {@link ASMAClient} from the website or from a local
 * database file, with replay controls like the website player. See
 * plans/07.
 *
 * @author Peter Dell
 */
public final class ASMABrowser implements ActionListener, ReplayControls.Host {

	private static final String TITLE = "ASMABrowser - Atari SAP Music Archive";

	private static final class Commands {
		public static final String EXIT = "exit";
		public static final String LOAD_FROM_WEBSITE = "loadFromWebsite";
		public static final String LOAD_FROM_LOCAL_FILE = "loadFromLocalFile";
		public static final String COMPOSERS = "composers";
		public static final String ABOUT = "about";

		private Commands() {
		}
	}

	// TODO plans/07: persist via ApplicationSettings instead of in-memory.
	private static final class InMemoryAttributeTablePreferences implements AttributeTablePreferences {
		private final Map<String, String> layoutProperties = new HashMap<String, String>();

		@Override
		public Map<String, String> getLayoutProperties() {
			return layoutProperties;
		}
	}

	private final MessageQueue messageQueue;
	private final ASMAClient client;
	private JFrame frame;
	private FileInfoDetailsPanel detailsPanel;
	private ReplayControls replayControls;
	private FileInfoFilterPanel filterPanel;
	private FileInfoListPanel fileInfoListPanel;
	private StatusBar statusBar;
	private java.util.List<FileInfo> allFileInfos;
	private java.util.List<org.atari.asma.Composer> allComposers;
	// Shared by all attribute tables, each under its own preferences prefix.
	private final AttributeTablePreferences attributeTablePreferences;
	// The archive folder next to a locally loaded database, null when loaded from the website.
	private File localArchiveFolder;
	private final Random random;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception ex) {
					// Ignore: keep the look and feel Swing already selected.
				}
				Application.createInstance("https://asma.atari.org/bin/ASMABrowser.zip", "ASMABrowser.jar",
						ASMABrowser.class);
				new ASMABrowser().run();
			}
		});
	}

	private ASMABrowser() {
		messageQueue = new MessageQueue();
		client = new ASMAClient();
		allFileInfos = new java.util.ArrayList<FileInfo>();
		allComposers = new java.util.ArrayList<org.atari.asma.Composer>();
		attributeTablePreferences = new InMemoryAttributeTablePreferences();
		random = new Random();
	}

	private void run() {
		frame = new JFrame(TITLE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Converted from public_html/img/favicon.ico (16x16), plus a pixel-exact 32x32 for the taskbar.
		frame.setIconImages(java.util.List.of(
				new ImageIcon(ASMABrowser.class.getResource("ASMABrowser-16.png")).getImage(),
				new ImageIcon(ASMABrowser.class.getResource("ASMABrowser-32.png")).getImage()));
		frame.setJMenuBar(createMenuBar());

		replayControls = new ReplayControls(this);
		detailsPanel = new FileInfoDetailsPanel(replayControls.getSongButtonsPanel(),
				replayControls.getPlayingTimePanel());
		detailsPanel.addFullWidthRow(replayControls.getButtonPanel());
		// Escape anywhere in the window stops playing, like the Stop button.
		var rootPane = frame.getRootPane();
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				"stop");
		rootPane.getActionMap().put("stop", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				replayControls.stop();
			}
		});
		filterPanel = new FileInfoFilterPanel(selection -> applySelection(selection));
		fileInfoListPanel = new FileInfoListPanel(attributeTablePreferences);
		statusBar = new StatusBar();

		fileInfoListPanel.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileInfoListPanel.getTable().getSelectionModel().addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				updateDetailsPanel();
			}
		});
		// A double click plays the file, like a click in the website player's result list.
		fileInfoListPanel.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					replayControls.playSelected();
				}
			}
		});

		var northPanel = new javax.swing.JPanel(new BorderLayout());
		northPanel.add(detailsPanel, BorderLayout.CENTER);
		northPanel.add(filterPanel, BorderLayout.SOUTH);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(northPanel, BorderLayout.NORTH);
		frame.getContentPane().add(fileInfoListPanel, BorderLayout.CENTER);
		frame.getContentPane().add(statusBar.getComponent(), BorderLayout.SOUTH);

		frame.setSize(1000, 800);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);

		// The website is the one source that is always there.
		loadFromWebsite();
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = ElementFactory.createMenu(com.wudsn.tools.base.Actions.MainMenu_File);
		fileMenu.add(createMenuItem(Actions.MainMenu_File_Exit, Commands.EXIT));
		menuBar.add(fileMenu);

		JMenu databaseMenu = ElementFactory.createMenu(Actions.MainMenu_Database);
		databaseMenu.add(createMenuItem(Actions.MainMenu_Database_LoadFromWebsite, Commands.LOAD_FROM_WEBSITE));
		databaseMenu.add(createMenuItem(Actions.MainMenu_Database_LoadFromLocalFile, Commands.LOAD_FROM_LOCAL_FILE));
		databaseMenu.addSeparator();
		databaseMenu.add(createMenuItem(Actions.MainMenu_Database_Composers, Commands.COMPOSERS));
		menuBar.add(databaseMenu);

		JMenu helpMenu = ElementFactory.createMenu(com.wudsn.tools.base.Actions.MainMenu_Help);
		helpMenu.add(createMenuItem(Actions.MainMenu_Help_About, Commands.ABOUT));
		menuBar.add(helpMenu);

		return menuBar;
	}

	private JMenuItem createMenuItem(com.wudsn.tools.base.repository.Action action, String command) {
		JMenuItem menuItem = ElementFactory.createMenuItem(action, command);
		menuItem.addActionListener(this);
		return menuItem;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		switch (event.getActionCommand()) {
		case Commands.EXIT:
			// The replay timer would keep the event dispatch thread alive after a dispose.
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			break;
		case Commands.LOAD_FROM_WEBSITE:
			loadFromWebsite();
			break;
		case Commands.LOAD_FROM_LOCAL_FILE:
			loadFromLocalFile();
			break;
		case Commands.COMPOSERS:
			ComposerListDialog.browse(frame, attributeTablePreferences, allComposers, getFileCountByComposerFolderName());
			break;
		case Commands.ABOUT:
			JOptionPane.showMessageDialog(frame,
					TITLE + "\nLocal version " + Application.getInstance().getLocalVersion(), "About",
					JOptionPane.INFORMATION_MESSAGE);
			break;
		default:
			throw new IllegalArgumentException("Invalid command " + event.getActionCommand() + ".");
		}
	}

	private void updateDetailsPanel() {
		var table = fileInfoListPanel.getTable();
		int viewRow = table.getSelectedRow();
		FileInfo fileInfo = null;
		if (viewRow >= 0) {
			fileInfo = fileInfoListPanel.getTableModel().getFileInfo(table.convertRowIndexToModel(viewRow));
		}
		detailsPanel.setFileInfo(fileInfo);
		replayControls.setSelectedFileInfo(fileInfo);
	}

	// Counts the files per folder in "Composers/<folderName>/...".
	private Map<String, Integer> getFileCountByComposerFolderName() {
		final String prefix = "Composers/";
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (FileInfo fileInfo : allFileInfos) {
			String filePath = fileInfo.filePath;
			if (filePath.startsWith(prefix)) {
				int index = filePath.indexOf('/', prefix.length());
				if (index > 0) {
					result.merge(filePath.substring(prefix.length(), index), Integer.valueOf(1), Integer::sum);
				}
			}
		}
		return result;
	}

	private void applySelection(FileInfo.Selection selection) {
		var selectedFileInfos = new java.util.ArrayList<FileInfo>(allFileInfos.size());
		for (FileInfo fileInfo : allFileInfos) {
			if (selection.matches(fileInfo)) {
				selectedFileInfos.add(fileInfo);
			}
		}
		fileInfoListPanel.getTableModel().setFileInfos(selectedFileInfos);
		filterPanel.setCountText(selectedFileInfos.size(), allFileInfos.size());
		updateDetailsPanel();
	}

	private interface DatabaseLoader {
		ASMAClient.Database load() throws IOException;
	}

	private void loadFromWebsite() {
		loadDatabase(client.getBaseURL() + ASMAClient.DATABASE_JSON_PATH, client::loadDatabase, null);
	}

	// The expected location of the generated database when started from the
	// repository root or from one of its module folders.
	private static File getExpectedLocalDatabaseFile() {
		File[] candidates = { new File("public_html/asmadb/asmadb.json"),
				new File("../public_html/asmadb/asmadb.json") };
		for (File file : candidates) {
			if (file.isFile()) {
				return file.getAbsoluteFile();
			}
		}
		return candidates[0].getAbsoluteFile();
	}

	private void loadFromLocalFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(Texts.LoadFromLocalFileDialog_Title);
		fileChooser.setFileFilter(new FileNameExtensionFilter(Texts.LoadFromLocalFileDialog_Filter, "json", "js"));
		File expectedFile = getExpectedLocalDatabaseFile();
		if (expectedFile.isFile()) {
			fileChooser.setSelectedFile(expectedFile);
		} else if (expectedFile.getParentFile() != null && expectedFile.getParentFile().isDirectory()) {
			fileChooser.setCurrentDirectory(expectedFile.getParentFile());
		}
		if (fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = fileChooser.getSelectedFile().getAbsoluteFile();
		loadDatabase(file.getPath(), () -> client.loadDatabase(file), getLocalArchiveFolder(file));
	}

	// public_html/asmadb/asmadb.json -> public_html/asma, if it exists; without
	// it, the module files are downloaded from the website.
	private static File getLocalArchiveFolder(File databaseFile) {
		File databaseFolder = databaseFile.getParentFile();
		if (databaseFolder == null || databaseFolder.getParentFile() == null) {
			return null;
		}
		File archiveFolder = new File(databaseFolder.getParentFile(), "asma");
		return archiveFolder.isDirectory() ? archiveFolder : null;
	}

	// Loads in the background, so a download does not freeze the window.
	private void loadDatabase(String source, DatabaseLoader loader, File archiveFolder) {
		statusBar.displayStatusMessage(new MessageQueueEntry(this, null, Messages.S005, source));
		new SwingWorker<ASMAClient.Database, Void>() {
			@Override
			protected ASMAClient.Database doInBackground() throws IOException {
				return loader.load();
			}

			@Override
			protected void done() {
				messageQueue.clear();
				try {
					ASMAClient.Database database = get();
					allFileInfos = database.fileInfos;
					allComposers = database.composerInfos;
					localArchiveFolder = archiveFolder;
					applySelection(filterPanel.getSelection());
					messageQueue.sendMessage(ASMABrowser.this, null, Messages.I001,
							String.valueOf(database.fileInfos.size()), source);
				} catch (ExecutionException ex) {
					Throwable cause = ex.getCause();
					messageQueue.sendMessage(ASMABrowser.this, null, Messages.E002, source,
							cause.getMessage() != null ? cause.getMessage() : cause.toString());
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				statusBar.displayMessageQueue(messageQueue);
				updateDetailsPanel();
			}
		}.execute();
	}

	// ReplayControls.Host: local archive files first, the website otherwise.
	@Override
	public byte[] loadContent(FileInfo fileInfo) throws IOException {
		if (localArchiveFolder != null) {
			File file = new File(localArchiveFolder, fileInfo.filePath);
			if (file.isFile()) {
				return Files.readAllBytes(file.toPath());
			}
		}
		return client.downloadFile(fileInfo);
	}

	@Override
	public FileInfo selectRandomFileInfo() {
		var tableModel = fileInfoListPanel.getTableModel();
		int rowCount = tableModel.getRowCount();
		if (rowCount == 0) {
			return null;
		}
		int modelRow = random.nextInt(rowCount);
		var table = fileInfoListPanel.getTable();
		int viewRow = table.convertRowIndexToView(modelRow);
		table.setRowSelectionInterval(viewRow, viewRow);
		table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
		return tableModel.getFileInfo(modelRow);
	}

	@Override
	public void showPlaybackError(FileInfo fileInfo, String reason) {
		messageQueue.clear();
		messageQueue.sendMessage(this, null, Messages.E004, fileInfo == null ? "" : fileInfo.filePath, reason);
		statusBar.displayMessageQueue(messageQueue);
	}
}
