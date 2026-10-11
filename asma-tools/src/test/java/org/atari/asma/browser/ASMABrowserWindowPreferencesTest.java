package org.atari.asma.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.wudsn.tools.base.common.ApplicationSettings;

/**
 * The window preferences round-trip through a throw-away settings node, never
 * the real settings of the ASMABrowser. See plans/11.
 */
public class ASMABrowserWindowPreferencesTest {

	private Preferences root;

	@BeforeEach
	public void setUp() {
		root = Preferences.userRoot().node("org/atari/asma/test/" + UUID.randomUUID());
	}

	@AfterEach
	public void tearDown() throws BackingStoreException {
		Preferences parent = root.parent();
		root.removeNode();
		// Leave no empty "test" node behind.
		if (parent.childrenNames().length == 0 && parent.keys().length == 0) {
			parent.removeNode();
		}
		Preferences.userRoot().flush();
	}

	private ASMABrowserWindowPreferences create() {
		return new ASMABrowserWindowPreferences(new ApplicationSettings(root).getSection("MainWindow"));
	}

	@Test
	public void defaultsWithoutSavedValues() {
		ASMABrowserWindowPreferences preferences = create();
		assertEquals(0, preferences.getMainWindowExtendedState());
		assertNull(preferences.getMainWindowLocation());
		assertEquals(new Dimension(ASMABrowserWindowPreferences.DEFAULT_WIDTH,
				ASMABrowserWindowPreferences.DEFAULT_HEIGHT), preferences.getMainWindowSize());
		assertEquals(0, preferences.getSelectedTab(3));
	}

	@Test
	public void valuesRoundTrip() {
		ASMABrowserWindowPreferences preferences = create();
		preferences.setMainWindowExtendedState(Frame.MAXIMIZED_BOTH);
		preferences.setMainWindowSize(new Dimension(1200, 900));
		preferences.setMainWindowSize(new Dimension(0, 0)); // Ignored, e.g. never shown.
		preferences.setSelectedTab(2);
		preferences.setMainWindowLocation(new Point(100, 50));

		ASMABrowserWindowPreferences other = create();
		assertEquals(new Point(100, 50), other.getMainWindowLocation());
		assertEquals(Frame.MAXIMIZED_BOTH, other.getMainWindowExtendedState());
		assertEquals(new Dimension(1200, 900), other.getMainWindowSize());
		assertEquals(2, other.getSelectedTab(3));
		// A tab that no longer exists.
		assertEquals(0, other.getSelectedTab(2));
	}

	@Test
	public void locationIsKeptOnScreen() {
		Rectangle screen = new Rectangle(0, 0, 1920, 1040);
		assertEquals(new Point(100, 50), ASMABrowserWindowPreferences.clamp(new Point(100, 50), screen));
		assertEquals(new Point(0, 0), ASMABrowserWindowPreferences.clamp(new Point(-500, -20), screen));
		assertEquals(new Point(1888, 1008), ASMABrowserWindowPreferences.clamp(new Point(5000, 3000), screen));
	}
}
