package org.atari.asma.browser.group;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.atari.asma.DataTypes;
import org.atari.asma.Group;
import org.atari.asma.browser.DetailsPanel;
import org.atari.asma.browser.GroupDemozooIDField;

/**
 * Details panel displaying the properties of the selected {@link Group}, plus
 * the STIL comment of the group's folder. See plans/09, section 5.
 *
 * @author Peter Dell
 */
public final class GroupDetailsPanel extends DetailsPanel {

	private final JTextField nameField;
	private final JTextField folderNameField;
	private final GroupDemozooIDField demozooIDField;
	private final JTextField fileCountField;
	private final JTextField composersField;
	private final JTextArea stilArea;

	public GroupDetailsPanel() {
		nameField = addField(DataTypes.Group_Name);
		folderNameField = addField(DataTypes.Group_FolderName);
		demozooIDField = addDemozooIDField(DataTypes.Group_DemozooID, new GroupDemozooIDField());
		fileCountField = addField(DataTypes.Group_FileCount);
		composersField = addFullWidthField(DataTypes.Group_Composers);
		stilArea = addSTILArea();
		setGroup(null, "", 0, "");
	}

	/**
	 * @param group
	 *            The group or <code>null</code>.
	 * @param composers
	 *            The names of the group's composers, not <code>null</code>.
	 * @param fileCount
	 *            The number of files in the group's folder.
	 * @param stilComment
	 *            The STIL comment of the group's folder, not
	 *            <code>null</code>.
	 */
	public void setGroup(Group group, String composers, int fileCount, String stilComment) {
		if (composers == null) {
			throw new IllegalArgumentException("Parameter 'composers' must not be null.");
		}
		if (stilComment == null) {
			throw new IllegalArgumentException("Parameter 'stilComment' must not be null.");
		}
		if (group == null) {
			nameField.setText("");
			folderNameField.setText("");
			demozooIDField.clear();
			fileCountField.setText("");
			composersField.setText("");
			stilArea.setText("");
			return;
		}
		nameField.setText(group.name);
		folderNameField.setText(group.folderName);
		demozooIDField.setDemozooID(group.getDemozooID(), group.name);
		fileCountField.setText(String.valueOf(fileCount));
		composersField.setText(composers);
		composersField.setCaretPosition(0);
		stilArea.setText(stilComment);
		stilArea.setCaretPosition(0);
	}
}
