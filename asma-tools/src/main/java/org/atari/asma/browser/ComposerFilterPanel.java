package org.atari.asma.browser;

import java.util.function.Consumer;

import javax.swing.JTextField;

import org.atari.asma.Composer;
import org.atari.asma.DataTypes;

/**
 * Filter panel above the composer table. Every change is applied
 * immediately and reported as a {@link Composer.Selection} to the owner.
 *
 * @author Peter Dell
 */
public final class ComposerFilterPanel extends FilterPanel<Composer.Selection> {

	private final JTextField keywordField;
	private final JTextField nameField;
	private final JTextField handlesField;
	private final JTextField countryField;
	private final JTextField groupsField;

	public ComposerFilterPanel(Consumer<Composer.Selection> selectionConsumer) {
		super(selectionConsumer);

		keywordField = addTextField(DataTypes.ComposerSelection_Keyword, 12);
		nameField = addTextField(DataTypes.ComposerSelection_Name, 10);
		handlesField = addTextField(DataTypes.Composer_Handles, 10);
		countryField = addTextField(DataTypes.Composer_Country, 8);
		groupsField = addTextField(DataTypes.Composer_Groups, 8);
		addCountLabel();
	}

	/** The field to focus first. */
	public JTextField getKeywordField() {
		return keywordField;
	}

	@Override
	public Composer.Selection getSelection() {
		return new Composer.Selection(keywordField.getText(), nameField.getText(), handlesField.getText(),
				countryField.getText(), groupsField.getText());
	}

	public void setCountText(int selectedCount, int totalCount) {
		setCountText(selectedCount, totalCount, "composers");
	}
}
