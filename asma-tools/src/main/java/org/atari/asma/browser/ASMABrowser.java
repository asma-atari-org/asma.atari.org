package org.atari.asma.browser;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import org.atari.asma.ASMAClient;
import org.atari.asma.FileInfo;

import com.wudsn.tools.base.common.Application;
import com.wudsn.tools.base.common.MessageQueue;
import com.wudsn.tools.base.gui.AttributeTablePreferences;
import com.wudsn.tools.base.gui.ElementFactory;
import com.wudsn.tools.base.gui.StatusBar;

/**
 * ASMABrowser - browses the database of the Atari SAP Music Archive
 * (https://asma.atari.org) in a details panel and an attribute table,
 * loaded via the {@link ASMAClient} from the local generated database or
 * from the website. See plans/07.
 *
 * @author Peter Dell
 */
public final class ASMABrowser implements ActionListener {

	private static final String TITLE = "ASMABrowser - Atari SAP Music Archive";

	private static final class Commands {
		public static final String EXIT = "exit";
		public static final String RELOAD_LOCAL = "reloadLocal";
		public static final String LOAD_FROM_WEBSITE = "loadFromWebsite";
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
	private FileInfoListPanel fileInfoListPanel;
	private StatusBar statusBar;

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
	}

	private void run() {
		frame = new JFrame(TITLE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setJMenuBar(createMenuBar());

		detailsPanel = new FileInfoDetailsPanel();
		fileInfoListPanel = new FileInfoListPanel(new InMemoryAttributeTablePreferences());
		statusBar = new StatusBar();

		fileInfoListPanel.getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileInfoListPanel.getTable().getSelectionModel().addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				updateDetailsPanel();
			}
		});

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(detailsPanel, BorderLayout.NORTH);
		frame.getContentPane().add(fileInfoListPanel, BorderLayout.CENTER);
		frame.getContentPane().add(statusBar.getComponent(), BorderLayout.SOUTH);

		frame.setSize(1000, 800);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);

		loadLocalDatabase();
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = ElementFactory.createMenu(com.wudsn.tools.base.Actions.MainMenu_File);
		fileMenu.add(createMenuItem(Actions.MainMenu_File_Exit, Commands.EXIT));
		menuBar.add(fileMenu);

		JMenu databaseMenu = ElementFactory.createMenu(Actions.MainMenu_Database);
		databaseMenu.add(createMenuItem(Actions.MainMenu_Database_ReloadLocal, Commands.RELOAD_LOCAL));
		databaseMenu.add(createMenuItem(Actions.MainMenu_Database_LoadFromWebsite, Commands.LOAD_FROM_WEBSITE));
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
			frame.dispose();
			break;
		case Commands.RELOAD_LOCAL:
			loadLocalDatabase();
			break;
		case Commands.LOAD_FROM_WEBSITE:
			loadDatabaseFromWebsite();
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
	}

	// The local candidates cover starting in the repository root and in the
	// asma-lib/asma-tools module folders.
	private void loadLocalDatabase() {
		File[] candidates = { new File("public_html/asmadb/asmadb.js"),
				new File("../public_html/asmadb/asmadb.js") };
		for (File file : candidates) {
			if (file.isFile()) {
				loadDatabase(file.getPath(), null);
				return;
			}
		}
		messageQueue.clear();
		messageQueue.sendMessage(this, null, Messages.E003,
				candidates[0].getAbsolutePath() + ", " + candidates[1].getAbsolutePath());
		statusBar.displayMessageQueue(messageQueue);
	}

	private void loadDatabaseFromWebsite() {
		loadDatabase(client.getBaseURL() + ASMAClient.DATABASE_JSON_PATH, Boolean.TRUE);
	}

	private void loadDatabase(String source, Boolean fromWebsite) {
		messageQueue.clear();
		try {
			ASMAClient.Database database;
			if (fromWebsite != null) {
				database = client.loadDatabase();
			} else {
				database = client.loadDatabase(new File(source));
			}
			fileInfoListPanel.getTableModel().setFileInfos(database.fileInfos);
			messageQueue.sendMessage(this, null, Messages.I001, String.valueOf(database.fileInfos.size()), source);
		} catch (IOException ex) {
			messageQueue.sendMessage(this, null, Messages.E002, source, ex.getMessage());
		}
		statusBar.displayMessageQueue(messageQueue);
		updateDetailsPanel();
	}
}
