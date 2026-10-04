package org.atari.asma;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.atari.asma.sap.ASAPFileUtility;

import net.sf.asap.PokeyStatesList;

public class SAPRFile {

	public static String CRLF = "\r\n";
	public static String HEADER = "SAP" + CRLF;

	public String author;
	public String name;
	public boolean stereo;
	public boolean ntsc;
	public int millis;
	public boolean loop;
	public PokeyStatesList statesList;

	public SAPRFile() {
		author = "<?>";
		name = "<?>";
		statesList = new PokeyStatesList();
	}

	private String getHeader() {
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
		return builder.toString();
	}

	public void setStatesList(PokeyStatesList statesList) {
		stereo = statesList.getPokeys() > 1;
		this.statesList = statesList;

	}

	public void write(OutputStream os) throws IOException {
		var wrapper = new net.sf.asap.OutputStream() {

			@Override
			public void write(int b) throws net.sf.asap.IOException {
				try {
					os.write(b);
				} catch (IOException ex) {
					throw new net.sf.asap.IOException(ex.getMessage());
				}

			}
		};

		var builder = getHeader() + CRLF;
		os.write(builder.toString().getBytes(Charset.forName("ASCII")));
		try {
			statesList.write(wrapper);
		} catch (net.sf.asap.IOException ex) {
			throw new IOException(ex.getMessage());
		}

	}

	public String toString() {
		return getHeader();
	}

}
