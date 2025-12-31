package org.atari.asma.demozoo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import com.google.gson.GsonBuilder;

public class RestClient {

	public static class HttpRequest {

		private HttpRequest() {
		};

		public static class ParameterValue {
			private final String key;
			private final String value;

			public ParameterValue(String key, String value) {
				this.key = key;
				this.value = value;
			}

			public String getKey() {
				return key;
			}

			public String getValue() {
				return value;
			}
		}

		public static String getParmetersString(List<ParameterValue> parameterValues) {
			var result = new StringBuilder();

			try {
				for (int i = 0; i < parameterValues.size(); i++) {
					var entry = parameterValues.get(i);
					result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
					result.append("=");
					result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
					if (i < parameterValues.size() - 1) {
						result.append("&");
					}
				}
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex);
			}

			return result.toString();
		}
	}

	public static final class HttpResponse {
		public int status;
		public String content;

		public boolean isSuccess() {
			return (status >= 200) && (status <= 299);
		}
	}

	public static HttpResponse sendGetRequest(String urlString) throws IOException {
		HttpURLConnection con = null;
		var response = new HttpResponse();
		try {

			URL url = new URL(urlString);

			// logInfo("Sending GET to '" + url.toString() + "'.");
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);

			var status = con.getResponseCode();
			var streamReader = new InputStreamReader(con.getInputStream());

			var in = new BufferedReader(streamReader);
			String inputLine;
			var content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();

			con.disconnect();

			response.status = status;
			response.content = content.toString();

		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		return response;
	}
}
