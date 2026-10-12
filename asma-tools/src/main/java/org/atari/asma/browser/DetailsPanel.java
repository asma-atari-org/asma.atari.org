package org.atari.asma.browser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.wudsn.tools.base.gui.ElementFactory;
import com.wudsn.tools.base.repository.DataType;

/**
 * Base class of the details panels above the entity lists and of the
 * SAPFileEditor's header fields: read-only fields in two label/field column
 * pairs, filled row by row, and full-width rows in between, in the order they
 * are added. The value columns keep their width when the content changes.
 *
 * @author Peter Dell
 */
public abstract class DetailsPanel extends JPanel {

	// Fields are laid out in two label/field column pairs, filled row by row;
	// a full-width row starts a new row.
	private int row;
	private int column;

	protected DetailsPanel() {
		setLayout(new GridBagLayout());
	}

	protected final JTextField addField(DataType dataType) {
		JTextField field = new JTextField();
		field.setEditable(false);
		addComponent(ElementFactory.createLabel(dataType, field), field);
		return field;
	}

	protected final <F extends DemozooIDField<?>> F addDemozooIDField(DataType dataType, F field) {
		if (field == null) {
			throw new IllegalArgumentException("Parameter 'field' must not be null.");
		}
		addComponent(new JLabel(dataType.getLabelWithoutMnemonics()), field);
		return field;
	}

	protected final void addComponent(JLabel label, JComponent field) {
		int gridx = column * 2;
		int gridy = row;

		add(label, createLabelConstraints(gridx, gridy));

		GridBagConstraints fieldConstraints = new GridBagConstraints();
		fieldConstraints.gridx = gridx + 1;
		fieldConstraints.gridy = gridy;
		fieldConstraints.weightx = 1.0;
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldConstraints.insets = new Insets(2, 0, 2, gridx == 0 ? 16 : 4);
		add(createFixedWidthCell(field), fieldConstraints);

		if (++column == 2) {
			column = 0;
			row++;
		}
	}

	private static GridBagConstraints createLabelConstraints(int gridx, int gridy) {
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridx = gridx;
		labelConstraints.gridy = gridy;
		labelConstraints.anchor = GridBagConstraints.NORTHWEST;
		labelConstraints.insets = new Insets(4, 4, 2, 8);
		return labelConstraints;
	}

	/**
	 * Wraps a value component into a cell with a constant preferred width, so
	 * both value columns share the panel width equally and keep their width
	 * when the content changes (a longer title, more song buttons, the playing
	 * time text). The height stays the component's natural height.
	 */
	private static JComponent createFixedWidthCell(JComponent component) {
		JPanel cell = new JPanel(new BorderLayout()) {
			@Override
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				size.width = 1;
				return size;
			}

			@Override
			public Dimension getMinimumSize() {
				Dimension size = super.getMinimumSize();
				size.width = 1;
				return size;
			}
		};
		cell.setOpaque(false);
		cell.add(component, BorderLayout.CENTER);
		return cell;
	}

	// A full-width row starts below a half-filled row.
	private int getNextFullWidthRow() {
		if (column != 0) {
			column = 0;
			row++;
		}
		return row++;
	}

	/** Adds an unlabeled row spanning all columns. */
	public final void addFullWidthRow(JComponent component) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = getNextFullWidthRow();
		constraints.gridwidth = 4;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(2, 0, 2, 4);
		add(component, constraints);
	}

	/** Adds a labeled row whose value spans the remaining columns. */
	protected final void addFullWidthField(JLabel label, JComponent field) {
		int gridy = getNextFullWidthRow();
		add(label, createLabelConstraints(0, gridy));
		GridBagConstraints fieldConstraints = new GridBagConstraints();
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = gridy;
		fieldConstraints.gridwidth = 3;
		fieldConstraints.weightx = 1.0;
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fieldConstraints.insets = new Insets(2, 0, 2, 4);
		add(createFixedWidthCell(field), fieldConstraints);
	}

	protected final JTextField addFullWidthField(DataType dataType) {
		JTextField field = new JTextField();
		field.setEditable(false);
		addFullWidthField(ElementFactory.createLabel(dataType, field), field);
		return field;
	}

	/**
	 * Adds the read-only area for the STIL comment, see {@link STILSource}.
	 *
	 * @return The text area, not <code>null</code>.
	 */
	protected final JTextArea addSTILArea() {
		JTextArea area = new JTextArea(3, 1);
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(area);
		addFullWidthField(new JLabel(Texts.DetailsPanel_STIL), scrollPane);
		return area;
	}
}
