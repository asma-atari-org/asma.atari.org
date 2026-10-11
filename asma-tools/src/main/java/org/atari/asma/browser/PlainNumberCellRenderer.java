package org.atari.asma.browser;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renders numbers as plain digits with a given alignment. The default number
 * renderer of JTable applies the locale's digit grouping (e.g. 8.931), which
 * is wrong for identifiers like Demozoo IDs, and always aligns right.
 *
 * @author Peter Dell
 */
public final class PlainNumberCellRenderer extends DefaultTableCellRenderer {

	/**
	 * @param horizontalAlignment
	 *            A {@link javax.swing.SwingConstants} alignment, e.g.
	 *            LEADING or RIGHT.
	 */
	public PlainNumberCellRenderer(int horizontalAlignment) {
		setHorizontalAlignment(horizontalAlignment);
	}

	@Override
	protected void setValue(Object value) {
		setText(value == null ? "" : value.toString());
	}
}
