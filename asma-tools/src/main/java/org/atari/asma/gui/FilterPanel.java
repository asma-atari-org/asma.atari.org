package org.atari.asma.gui;

import java.awt.FlowLayout;
import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.wudsn.tools.base.gui.ElementFactory;
import com.wudsn.tools.base.repository.DataType;

/**
 * Base class of the filter panels above the attribute tables: a row of
 * labelled text fields and combo boxes, followed by a count label. Every
 * change is applied immediately and reported as a selection of type
 * <code>S</code> to the owner. A subclass adds its fields in its
 * constructor, then calls {@link #addCountLabel()}.
 *
 * @param <S>
 *            The selection type, e.g. a record with a <code>matches</code>
 *            method.
 *
 * @author Peter Dell
 */
public abstract class FilterPanel<S> extends JPanel {

	private final Consumer<S> selectionConsumer;
	private JLabel countLabel;

	protected FilterPanel(Consumer<S> selectionConsumer) {
		if (selectionConsumer == null) {
			throw new IllegalArgumentException("Parameter 'selectionConsumer' must not be null.");
		}
		this.selectionConsumer = selectionConsumer;
		setLayout(new FlowLayout(FlowLayout.LEFT, 8, 2));
	}

	protected final JTextField addTextField(DataType dataType, int columns) {
		JTextField field = new JTextField(columns);
		add(ElementFactory.createLabel(dataType, field));
		add(field);
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent event) {
				fireSelectionChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				fireSelectionChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				fireSelectionChanged();
			}
		});
		return field;
	}

	protected final JComboBox<String> addComboBox(DataType dataType, String[] texts) {
		JComboBox<String> comboBox = new JComboBox<String>(texts);
		add(ElementFactory.createLabel(dataType, comboBox));
		add(comboBox);
		comboBox.addActionListener(event -> fireSelectionChanged());
		return comboBox;
	}

	protected final void addCountLabel() {
		countLabel = new JLabel();
		add(countLabel);
	}

	public final void fireSelectionChanged() {
		selectionConsumer.accept(getSelection());
	}

	public abstract S getSelection();

	/**
	 * @param selectedCount
	 *            The number of selected entries.
	 * @param totalCount
	 *            The total number of entries.
	 * @param unit
	 *            The plural of the entry kind, e.g. "files", not
	 *            <code>null</code>.
	 */
	protected final void setCountText(int selectedCount, int totalCount, String unit) {
		if (countLabel == null) {
			throw new IllegalStateException("addCountLabel() was not called.");
		}
		countLabel.setText(selectedCount + " of " + totalCount + " " + unit);
	}
}
