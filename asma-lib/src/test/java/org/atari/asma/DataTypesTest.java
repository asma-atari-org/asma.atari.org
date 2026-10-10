package org.atari.asma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import com.wudsn.tools.base.repository.DataType;

public class DataTypesTest {

	@Test
	public void allDataTypesAreLoadedWithLabels() throws Exception {
		int count = 0;
		for (Field field : DataTypes.class.getFields()) {
			if (Modifier.isStatic(field.getModifiers()) && field.getType() == DataType.class) {
				DataType dataType = (DataType) field.get(null);
				assertNotNull(dataType, field.getName());
				assertNotNull(dataType.getLabel(), field.getName() + " label");
				count++;
			}
		}
		assertEquals(12, count);
	}

	@Test
	public void attributesBindTheGsonFieldNames() {
		assertEquals("filePath", FileInfo.Attributes.FILE_PATH.getName());
		assertEquals("title", FileInfo.Attributes.TITLE.getName());
		assertEquals("demozooID", FileInfo.Attributes.DEMOZOO_ID.getName());
		assertEquals(DataTypes.FileInfo_Title, FileInfo.Attributes.TITLE.getDataType());
	}
}
