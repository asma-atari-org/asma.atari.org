package org.atari.asma.browser;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renders numeric identifiers like Demozoo IDs as plain digits. The default
 * number renderer of JTable applies the locale's digit grouping (e.g. 8.931),
 * which is wrong for identifiers.
 *
 * @author Peter Dell
 */
final class IdentifierCellRenderer extends DefaultTableCellRenderer {

	IdentifierCellRenderer() {
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	@Override
	protected void setValue(Object value) {
		setText(value == null ? "" : value.toString());
	}
}
