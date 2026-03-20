package org.atari.asma;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.atari.asma.sap.ASAPFileUtility;

public class SAPRFile {

	public static String CRLF = "\r\n";

	public String author = "<?>";
	public String name = "<?>";
	public boolean stereo;
	public boolean ntsc;
	public int millis;
	public boolean loop;
	public byte[] content;

	public void write(OutputStream os) throws IOException {
		if (content == null) {
			throw new IOException("No content set.");
		}
		String timeString = null;
		if (millis > 0) {
			timeString = ASAPFileUtility.getTimeString(millis);
			if (loop) {
				timeString += " LOOP";
			}
		}
		var builder = new StringBuilder();
		builder.append("SAP").append(CRLF);
		builder.append("AUTHOR \"<?>\"" + CRLF);
		builder.append("NAME \"<?>\"" + CRLF);
		if (stereo) {
			builder.append("STEREO" + CRLF);
		}
		if (ntsc) {
			builder.append("NTSC" + CRLF);
		}

		builder.append("TYPE R" + CRLF);
		if (timeString != null) {
			builder.append("TIME " + timeString + CRLF);
		}
		builder.append(CRLF);
		os.write(builder.toString().getBytes(Charset.forName("ASCII")));

		os.write(content);
	}

	public void setContent(byte[] content) {
		if (content == null) {
			throw new IllegalArgumentException("Parameter must not be null");
		}
		int contentSize = content.length;
		int frameSize = ASAPFileUtility.getFrameSize(stereo);
		if (contentSize % frameSize != 0) {
			throw new IllegalArgumentException(
					"Content size " + contentSize + " is no multiple of the frame size " + frameSize + ".");
		}
		this.content = content;

	}

}
