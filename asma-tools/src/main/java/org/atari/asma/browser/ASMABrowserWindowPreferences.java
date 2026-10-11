package org.atari.asma.browser;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;

import com.wudsn.tools.base.common.ApplicationSettingsSection;
import com.wudsn.tools.base.gui.MainWindowPreferences;

/**
 * The main window's geometry and selected tab across runs, in one settings
 * section, like the RMT port's window preferences. A saved location is kept
 * on screen; without one, the window is centered. See plans/11.
 *
 * @author Peter Dell
 */
final class ASMABrowserWindowPreferences implements MainWindowPreferences {

	static final int DEFAULT_WIDTH = 1000;
	static final int DEFAULT_HEIGHT = 800;

	private static final String EXTENDED_STATE = "extendedState";
	private static final String X = "x";
	private static final String Y = "y";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	private static final String SELECTED_TAB = "selectedTab";

	// The part of the window that must stay on screen, about an icon.
	private static final int VISIBLE_SIZE = 32;

	private final ApplicationSettingsSection section;

	ASMABrowserWindowPreferences(ApplicationSettingsSection section) {
		if (section == null) {
			throw new IllegalArgumentException("Parameter 'section' must not be null.");
		}
		this.section = section;
	}

	@Override
	public void setMainWindowExtendedState(int mainWindowExtendedState) {
		section.writeUnsignedInt(EXTENDED_STATE, mainWindowExtendedState);
	}

	@Override
	public int getMainWindowExtendedState() {
		return section.getUnsignedInt(EXTENDED_STATE, 0);
	}

	// The coordinates are stored as strings, because they can be negative on
	// a screen left of or above the primary one.
	@Override
	public void setMainWindowLocation(Point mainWindowLocation) {
		if (mainWindowLocation != null) {
			section.writeString(X, Integer.toString(mainWindowLocation.x));
			section.writeString(Y, Integer.toString(mainWindowLocation.y));
		}
	}

	/** The saved location moved onto the screen, or <code>null</code> if none was saved. */
	@Override
	public Point getMainWindowLocation() {
		int x;
		int y;
		try {
			x = Integer.parseInt(section.getString(X, ""));
			y = Integer.parseInt(section.getString(Y, ""));
		} catch (NumberFormatException ex) {
			return null;
		}
		if (!GraphicsEnvironment.isHeadless()) {
			Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			return clamp(new Point(x, y), screen);
		}
		return new Point(x, y);
	}

	static Point clamp(Point location, Rectangle screen) {
		int x = Math.max(screen.x, Math.min(location.x, screen.x + screen.width - VISIBLE_SIZE));
		int y = Math.max(screen.y, Math.min(location.y, screen.y + screen.height - VISIBLE_SIZE));
		return new Point(x, y);
	}

	@Override
	public void setMainWindowSize(Dimension mainWindowSize) {
		if (mainWindowSize != null && mainWindowSize.width > 0 && mainWindowSize.height > 0) {
			section.writeUnsignedInt(WIDTH, mainWindowSize.width);
			section.writeUnsignedInt(HEIGHT, mainWindowSize.height);
		}
	}

	@Override
	public Dimension getMainWindowSize() {
		return new Dimension(section.getUnsignedInt(WIDTH, DEFAULT_WIDTH),
				section.getUnsignedInt(HEIGHT, DEFAULT_HEIGHT));
	}

	void setSelectedTab(int selectedTab) {
		section.writeUnsignedInt(SELECTED_TAB, selectedTab);
	}

	/**
	 * @param tabCount
	 *            The number of tabs.
	 * @return The saved tab index if it is valid for the tab count, 0
	 *         otherwise.
	 */
	int getSelectedTab(int tabCount) {
		int selectedTab = section.getUnsignedInt(SELECTED_TAB, 0);
		return (selectedTab >= 0 && selectedTab < tabCount) ? selectedTab : 0;
	}
}
