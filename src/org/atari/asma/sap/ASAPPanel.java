package org.atari.asma.sap;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ASAPPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int row;

	public JLabel authorLabel;
	public JTextField authorField;

	public JLabel nameLabel;
	public JTextField nameField;

	public JLabel dateLabel;
	public JTextField dateField;

	private void addLabel(JLabel label) {
		var c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets.top = 5;
		c.insets.right = 5;
		c.gridx = 0;
		c.gridy = row;

		add(label, c);
	}

	private void addTextField(JTextField textField) {
		textField.setBorder(null);
		var c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets.top = 5;
		c.insets.right = 5;
		c.gridx = 1;
		c.gridy = row;
		c.weightx = 1.0;
		add(textField, c);
		row++;
	}

	public ASAPPanel() {

		var layout = new GridBagLayout();
		setLayout(layout);

		authorLabel = new JLabel();
		authorLabel.setText("Author");
		addLabel(authorLabel);
		authorField = new JTextField();
		addTextField(authorField);

		nameLabel = new JLabel();
		nameLabel.setText("Title");
		addLabel(nameLabel);

		nameField = new JTextField();
		addTextField(nameField);

		dateLabel = new JLabel();
		dateLabel.setText("Date");
		addLabel(dateLabel);
		dateField = new JTextField();
		addTextField(dateField);

		var filler = new JLabel();
		var c = new GridBagConstraints();
		c.gridy = row;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 1.0;
		add(filler, c);
		setEditable(false);
	}

	public void setEditable(boolean editable) {
		authorField.setEditable(editable);
		nameField.setEditable(editable);
		dateField.setEditable(editable);

	}

	public void dataToUI(ASAPFile asapFile, File outputFile) {

		authorField.setText(asapFile.getAuthor());
		nameField.setText(asapFile.getTitle());
		dateField.setText(asapFile.getDate());
		setEditable(outputFile != null);
	}

	public void dataFromUI(ASAPFile asapFile) {
		asapFile.setAuthor(authorField.getText());
		asapFile.setTitle(nameField.getText());
		asapFile.setDate(dateField.getText());

	}

}
