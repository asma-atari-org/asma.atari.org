package org.atari.asma.browser.group;

import java.util.function.Consumer;

import javax.swing.JTextField;

import org.atari.asma.DataTypes;
import org.atari.asma.Group;
import org.atari.asma.browser.FilterPanel;

/**
 * Filter panel above the group table. Every change is applied immediately
 * and reported as a {@link Group.Selection} to the owner.
 *
 * @author Peter Dell
 */
public final class GroupFilterPanel extends FilterPanel<Group.Selection> {

	private final JTextField keywordField;
	private final JTextField nameField;

	public GroupFilterPanel(Consumer<Group.Selection> selectionConsumer) {
		super(selectionConsumer);

		keywordField = addTextField(DataTypes.GroupSelection_Keyword, 12);
		nameField = addTextField(DataTypes.Group_Name, 10);
		addCountLabel();
	}

	/** The field to focus first. */
	public JTextField getKeywordField() {
		return keywordField;
	}

	@Override
	public Group.Selection getSelection() {
		return new Group.Selection(keywordField.getText(), nameField.getText());
	}

	public void setCountText(int selectedCount, int totalCount) {
		setCountText(selectedCount, totalCount, "groups");
	}
}
