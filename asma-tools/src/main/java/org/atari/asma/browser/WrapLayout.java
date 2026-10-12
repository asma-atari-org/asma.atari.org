package org.atari.asma.browser;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * A {@link FlowLayout} whose preferred size accounts for wrapping: when the
 * components do not fit into the container's width, the preferred height
 * covers all rows. A plain FlowLayout wraps as well, but reports the height of
 * a single row, so the wrapped rows are cut off, e.g. at the top of a
 * BorderLayout. Candidate for WUDSN Base. See plans/07.
 *
 * @author Peter Dell
 */
public final class WrapLayout extends FlowLayout {

	private static final long serialVersionUID = 1L;

	public WrapLayout(int align, int hgap, int vgap) {
		super(align, hgap, vgap);
	}

	@Override
	public Dimension preferredLayoutSize(Container target) {
		return getLayoutSize(target, true);
	}

	@Override
	public Dimension minimumLayoutSize(Container target) {
		Dimension result = getLayoutSize(target, false);
		// Allow the container to shrink below its preferred width.
		result.width -= getHgap() + 1;
		return result;
	}

	private Dimension getLayoutSize(Container target, boolean preferred) {
		synchronized (target.getTreeLock()) {
			// Before the first layout, the container has no width yet; then the
			// nearest ancestor with a width is the best estimate.
			Container container = target;
			while (container.getSize().width == 0 && container.getParent() != null) {
				container = container.getParent();
			}
			int targetWidth = container.getSize().width;
			if (targetWidth == 0) {
				targetWidth = Integer.MAX_VALUE;
			}

			int hgap = getHgap();
			int vgap = getVgap();
			Insets insets = target.getInsets();
			int maxRowWidth = targetWidth - (insets.left + insets.right + hgap * 2);

			int width = 0;
			int height = 0;
			int rowWidth = 0;
			int rowHeight = 0;
			for (int i = 0; i < target.getComponentCount(); i++) {
				Component component = target.getComponent(i);
				if (!component.isVisible()) {
					continue;
				}
				Dimension size = preferred ? component.getPreferredSize() : component.getMinimumSize();
				if (rowWidth > 0 && rowWidth + hgap + size.width > maxRowWidth) {
					width = Math.max(width, rowWidth);
					height += rowHeight + vgap;
					rowWidth = 0;
					rowHeight = 0;
				}
				if (rowWidth > 0) {
					rowWidth += hgap;
				}
				rowWidth += size.width;
				rowHeight = Math.max(rowHeight, size.height);
			}
			width = Math.max(width, rowWidth);
			height += rowHeight;

			return new Dimension(width + insets.left + insets.right + hgap * 2,
					height + insets.top + insets.bottom + vgap * 2);
		}
	}
}
