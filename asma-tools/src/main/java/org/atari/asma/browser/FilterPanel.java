package org.atari.asma.browser;

import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.wudsn.tools.base.gui.ElementFactory;
import com.wudsn.tools.base.repository.DataType;
import com.wudsn.tools.base.repository.ValueSet;

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
		// Wraps the fields into further rows if the window is too narrow.
		setLayout(new WrapLayout(FlowLayout.LEFT, 8, 2));
		// The preferred height depends on the width, so a new width needs a new layout of the parent.
		addComponentListener(new ComponentAdapter() {
			private int width;

			@Override
			public void componentResized(ComponentEvent event) {
				if (getWidth() != width) {
					width = getWidth();
					revalidate();
				}
			}
		});
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

	/**
	 * Adds a combo box with an "All" entry, standing for no restriction,
	 * followed by the values of a value set. Read it with
	 * {@link #getSelectedValue(JComboBox, Class)}. To be replaced by WUDSN
	 * Base's planned ValueSetSelectionField, see plans/13.
	 */
	protected final <T extends ValueSet> JComboBox<Object> addValueSetComboBox(DataType dataType,
			Class<T> valueSetClass) {
		List<Object> items = new ArrayList<Object>();
		items.add(Texts.FilterPanel_All);
		items.addAll(ValueSet.getValues(valueSetClass));
		// A value set shows its text.
		JComboBox<Object> comboBox = new JComboBox<Object>(items.toArray());
		add(ElementFactory.createLabel(dataType, comboBox));
		add(comboBox);
		comboBox.addActionListener(event -> fireSelectionChanged());
		return comboBox;
	}

	/**
	 * @return The selected value, or <code>null</code> for "All".
	 */
	protected static <T extends ValueSet> T getSelectedValue(JComboBox<Object> comboBox, Class<T> valueSetClass) {
		Object item = comboBox.getSelectedItem();
		return valueSetClass.isInstance(item) ? valueSetClass.cast(item) : null;
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
