package org.atari.asma;

import com.wudsn.tools.base.common.MemorySize;
import com.wudsn.tools.base.repository.DataType;
import com.wudsn.tools.base.repository.NLS;

/**
 * Text repository for the {@link FileInfo} data types, used by the
 * ASMABrowser table and details panel. See plans/07.
 *
 * @author Peter Dell
 */
public final class DataTypes extends NLS {

	public static DataType FileInfo_FilePath = new DataType(String.class);
	public static DataType FileInfo_Title = new DataType(String.class);
	public static DataType FileInfo_Author = new DataType(String.class);
	public static DataType FileInfo_Date = new DataType(String.class);
	public static DataType FileInfo_Hardware = new DataType(String.class);
	public static DataType FileInfo_OriginalModuleExt = new DataType(String.class);
	public static DataType FileInfo_Channels = new DataType(Integer.class);
	public static DataType FileInfo_Songs = new DataType(Integer.class);
	public static DataType FileInfo_SongsAndDefaultSong = new DataType(Integer.class);
	public static DataType FileInfo_DefaultSongIndex = new DataType(Integer.class);
	public static DataType FileInfo_FileSize = new DataType(MemorySize.class);
	public static DataType FileInfo_DemozooID = new DataType(Integer.class);

	public static DataType FileInfoSelection_Keyword = new DataType(String.class);

	public static DataType Composer_FolderName = new DataType(String.class);
	public static DataType Composer_LastName = new DataType(String.class);
	public static DataType Composer_FirstName = new DataType(String.class);
	public static DataType Composer_Handles = new DataType(String.class);
	public static DataType Composer_Country = new DataType(String.class);
	public static DataType Composer_DemozooID = new DataType(Integer.class);
	public static DataType Composer_Groups = new DataType(String.class);
	public static DataType Composer_FileCount = new DataType(Integer.class);

	public static DataType ComposerSelection_Keyword = new DataType(String.class);
	public static DataType ComposerSelection_Name = new DataType(String.class);

	public static DataType Group_FolderName = new DataType(String.class);
	public static DataType Group_Name = new DataType(String.class);
	public static DataType Group_DemozooID = new DataType(Integer.class);
	public static DataType Group_Composers = new DataType(String.class);
	public static DataType Group_FileCount = new DataType(Integer.class);

	public static DataType GroupSelection_Keyword = new DataType(String.class);

	static {
		initializeClass(DataTypes.class, null);
	}
}
