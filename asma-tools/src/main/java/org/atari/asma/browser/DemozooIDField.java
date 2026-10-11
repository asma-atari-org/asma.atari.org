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
 * author details. There is one subclass per kind of ID, which provides these
 * links. See plans/10.
 *
 * @param <T>
 *            The kind of the displayed IDs.
 *
 * @author Peter Dell
 */
public abstract class DemozooIDField<T extends DemozooID> extends JPanel {

	protected DemozooIDField() {
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setOpaque(false);
		clear();
	}

	/** The text of the link to search for the entity without an ID. */
	protected abstract String getFindText();

	/** The URL to search for the given text, not <code>null</code>. */
	protected abstract String getFindURL(String searchText);

	/** The text of the link to create the entity without an ID. */
	protected abstract String getCreateText();

	/** The URL to create the entity. */
	protected abstract String getCreateURL();

	/** Displays nothing, e.g. without a selected entry. */
	public final void clear() {
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
	 *            The ID or <code>null</code>.
	 * @param searchText
	 *            The text to search for without an ID, e.g. the composer's name,
	 *            not <code>null</code>.
	 */
	public final void setDemozooID(T demozooID, String searchText) {
		if (searchText == null) {
			throw new IllegalArgumentException("Parameter 'searchText' must not be null.");
		}
		removeAll();
		if (demozooID != null) {
			add(createLink(demozooID.toString(), demozooID.getURL()));
		} else {
			if (!searchText.isBlank()) {
				add(createLink(getFindText(), getFindURL(searchText.trim())));
				add(new JLabel(", "));
			}
			add(createLink(getCreateText(), getCreateURL()));
		}
		revalidate();
		repaint();
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
					new MessageQueueEntry(this, null, Messages.E506, url, reason).getMessageText(), "Demozoo",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
