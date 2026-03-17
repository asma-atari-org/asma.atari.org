package org.atari.asma.sap;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SAPFileList {

	private Map<String, SAPFileDialog> sapFileDialogMap;

	private DefaultListModel<String> listModel;
	private JFrame frame;

	public SAPFileList() {

		sapFileDialogMap = new TreeMap<String, SAPFileDialog>();
		listModel = new DefaultListModel<>();

		frame = new JFrame();
		frame.setTitle("SAPFileEditor");
		// Create a JList and set its model to the DefaultListModel
		JList<String> jList = new JList<>(listModel);

		// Add the JList to a JScrollPane to allow for scrolling
		JScrollPane scrollPane = new JScrollPane(jList);

		// Add the JScrollPane to a container such as a JFrame or JPanel
		JPanel panel = new JPanel();
		panel.add(scrollPane);
		frame.add(panel);

		
		jList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				var dialog=getDialog( listModel.get(e.getFirstIndex()));
				var frame=dialog.getFrame();
				frame.requestFocus();
				
			}
		});
		// Set the JFrame properties
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("SAPFileEditor - Files Opened");
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	

	public JFrame getFrame() {
		return frame;
	}

	public SAPFileDialog getDialog(String path) {
		return sapFileDialogMap.get(path);
	}

	public void putDialog(String path, SAPFileDialog dialog) {
		sapFileDialogMap.put(path, dialog);
		listModel.addElement(path);
		frame.pack();

	}

	public void removeDialog(String path) {
		sapFileDialogMap.remove(path);
		listModel.removeElement(path);

	}


}
