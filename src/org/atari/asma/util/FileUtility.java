package org.atari.asma.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtility {

	public static String readAsString(File file) {
		byte[] bytes = readAsByteArray(file);
		String result;
		try {
			result = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	public static byte[] readAsByteArray(File file) {
		try {
			return readAsByteArray(new FileInputStream(file));

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static byte[] readAsByteArray(InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("Parameter 'inputStream' must not be null.");
		}
		// Read binary from the input stream.
		byte[] result;
		int resultLength;
		try {
			var buffer = new byte[1024 * 1024*16];
			resultLength = readAndClose(inputStream, buffer);
			result = new byte[resultLength];
			System.arraycopy(buffer, 0, result, 0, resultLength);

		} catch (IOException ex) {

			throw new RuntimeException(ex);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ex) {
				throw new RuntimeException(ex);

			}
		}
		return result;
	}

	/**
	 * Fill a byte array from an input stream.
	 * 
	 * @param inputStream The input stream, not <code>null</code>.
	 * @param buffer      The byte array in which the module shall be loaded. Only
	 *                    the bytes which fit into the array are loaded.
	 * @return The actual number of bytes read.
	 * @throws IOException If the reading fails.
	 */
	private static final int readAndClose(InputStream inputStream, byte[] buffer) throws IOException {
		if (inputStream == null) {
			throw new IllegalArgumentException("Parameter 'inputStream' must not be null.");
		}
		if (buffer == null) {
			throw new IllegalArgumentException("Parameter 'buffer' must not be null.");
		}
		int got = 0;
		int need = buffer.length;
		try {
			while (need > 0) {
				int i = inputStream.read(buffer, got, need);
				if (i <= 0)
					break;
				got += i;
				need -= i;
			}
		} finally {
			inputStream.close();
		}
		return got;

	}

	public static List<File> getRecursiveFileList(File dir, FileFilter filter) {
		List<File> fileList = new ArrayList<File>();
		getRecursiveFileListInternal(dir, filter, fileList);
		return fileList;
	}

	private static void getRecursiveFileListInternal(File dir, FileFilter filter, List<File> fileList) {
		if (dir == null) {
			throw new IllegalArgumentException("Parameter dir must not be null.");
		}
		if (filter == null) {
			throw new IllegalArgumentException("Parameter filter must not be null.");
		}
		if (fileList == null) {
			throw new IllegalArgumentException("Parameter fileList must not be null.");
		}
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				getRecursiveFileListInternal(f, filter, fileList);
			} else {
				if (filter.accept(f)) {
					fileList.add(f);
				}
			}
		}
	}
}
