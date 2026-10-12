package org.atari.asma.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

/**
 * The preferred height covers all rows the components wrap into.
 */
public class WrapLayoutTest {

	private static JPanel createPanel(int width) {
		// Gaps of 10, five components of 100x20.
		JPanel panel = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));
		for (int i = 0; i < 5; i++) {
			JLabel label = new JLabel();
			label.setPreferredSize(new Dimension(100, 20));
			panel.add(label);
		}
		panel.setSize(width, 100);
		return panel;
	}

	@Test
	public void oneRowWhenWideEnough() {
		// 5*100 + 4*10 between + 2*10 margins = 560.
		Dimension size = createPanel(560).getPreferredSize();
		assertEquals(560, size.width);
		assertEquals(20 + 2 * 10, size.height);
	}

	@Test
	public void wrapsIntoRowsWhenTooNarrow() {
		// Room for 2 per row (2*100 + 10 = 210 <= 250 - 20): 3 rows.
		Dimension size = createPanel(250).getPreferredSize();
		assertEquals(3 * 20 + 2 * 10 + 2 * 10, size.height);
		assertEquals(210 + 2 * 10, size.width);
	}
}
