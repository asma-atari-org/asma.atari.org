package org.atari.asma.browser;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.atari.asma.demozoo.DemozooID;

import com.wudsn.tools.base.common.MessageQueueEntry;

/**
 * Display field for a {@link DemozooID}: the ID as a link that opens the
 * Demozoo page in the browser, like the website's links. Without an ID, it
 * offers "Find ..." and "Create ..." links instead, like the website's
 * author details. See plans/09.
 *
 * @author Peter Dell
 */
public final class DemozooIDField extends JPanel {

	private final DemozooID.Kind kind;

	/**
	 * @param kind
	 *            The kind of the displayed IDs, not <code>null</code>.
	 */
	public DemozooIDField(DemozooID.Kind kind) {
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		if (kind == null) {
			throw new IllegalArgumentException("Parameter 'kind' must not be null.");
		}
		this.kind = kind;
		setOpaque(false);
		clear();
	}

	/** Displays nothing, e.g. without a selected entry. */
	public void clear() {
		removeAll();
		// A blank label keeps the row height constant.
		add(new JLabel(" "));
		revalidate();
		repaint();
	}

	/**
	 * Displays the ID as a link, or the find and create links if there is no
	 * ID.
	 *
	 * @param demozooID
	 *            The ID or <code>null</code>; its kind must be the field's kind.
	 * @param searchText
	 *            The text to search for without an ID, e.g. the composer's name,
	 *            not <code>null</code>.
	 */
	public void setDemozooID(DemozooID demozooID, String searchText) {
		if (searchText == null) {
			throw new IllegalArgumentException("Parameter 'searchText' must not be null.");
		}
		if (demozooID != null && demozooID.getKind() != kind) {
			throw new IllegalArgumentException("Demozoo ID " + demozooID + " is of kind " + demozooID.getKind()
					+ " instead of " + kind + ".");
		}
		removeAll();
		if (demozooID != null) {
			add(createLink(demozooID.toString(), demozooID.getURL()));
		} else {
			if (!searchText.isBlank()) {
				add(createLink(getFindText(), kind.getFindURL(searchText.trim())));
				add(new JLabel(", "));
			}
			add(createLink(getCreateText(), kind.getCreateURL()));
		}
		revalidate();
		repaint();
	}

	private String getFindText() {
		switch (kind) {
		case SCENER:
			return Texts.DemozooIDField_FindScener;
		case GROUP:
			return Texts.DemozooIDField_FindGroup;
		default:
			return Texts.DemozooIDField_FindMusic;
		}
	}

	private String getCreateText() {
		switch (kind) {
		case SCENER:
			return Texts.DemozooIDField_CreateScener;
		case GROUP:
			return Texts.DemozooIDField_CreateGroup;
		default:
			return Texts.DemozooIDField_CreateMusic;
		}
	}

	// A button that looks like a link, so it is also reachable with the keyboard.
	private JButton createLink(String text, String url) {
		JButton button = new JButton("<html><a href=\"\">" + text.replace("&", "&amp;").replace("<", "&lt;")
				+ "</a></html>");
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.setToolTipText(url);
		button.addActionListener(event -> openURL(url));
		return button;
	}

	private void openURL(String url) {
		try {
			if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				throw new UnsupportedOperationException("Opening a web browser is not supported on this system.");
			}
			Desktop.getDesktop().browse(new URI(url));
		} catch (Exception ex) {
			String reason = ex.getMessage() != null ? ex.getMessage() : ex.toString();
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
					new MessageQueueEntry(this, null, Messages.E006, url, reason).getMessageText(), "Demozoo",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
