package org.atari.asma.util;

import com.google.gson.GsonBuilder;

public class Serializer {

	public static String serialize(Object object) {
		var gson = new GsonBuilder().create();
		try {
			return gson.toJson(object);
			} catch (RuntimeException ex) {
			throw new RuntimeException("Cannot serialiize '" + object + "'.", ex);
		}
	}

	public static <T> T deserialize(String jsonString, Class<T> typeToken) {
		var gson = new GsonBuilder().create();
		try {
			T object = gson.fromJson(jsonString, typeToken);
			return object;
		} catch (RuntimeException ex) {
			throw new RuntimeException("Cannot deserialiize '" + jsonString + "'.", ex);
		}
	}
}
