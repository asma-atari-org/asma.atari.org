package org.atari.asma.exporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.wudsn.tools.base.repository.Message;

/**
 * All message repository classes of ASMA load, and their identifiers are
 * unique across the classes: WUDSN Base checks the numbers within one class
 * only, while the area ASA is shared by several classes with their own number
 * ranges. See plans/06, phase 2.
 */
public class MessagesTest {

	private static final List<Class<?>> CLASSES = List.of(org.atari.asma.Messages.class,
			org.atari.asma.sap.Messages.class, org.atari.asma.demozoo.Messages.class,
			org.atari.asma.exporter.Messages.class, org.atari.asma.editor.Messages.class,
			org.atari.asma.browser.Messages.class);

	@Test
	public void identifiersAreUniqueAcrossClasses() throws IllegalAccessException {
		Map<String, String> classByIdentifier = new HashMap<String, String>();
		for (Class<?> clazz : CLASSES) {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.getType() != Message.class || !Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				Message message = (Message) field.get(null);
				String identifier = message.getIdentifier();
				String previous = classByIdentifier.put(identifier, clazz.getName());
				if (previous != null) {
					fail("Identifier " + identifier + " is used by " + previous + " and " + clazz.getName() + ".");
				}
			}
		}
		assertTrue(classByIdentifier.size() > 100, "Only " + classByIdentifier.size() + " messages found.");
	}

	@Test
	public void areasAndRanges() {
		assertEquals("SAP-024", org.atari.asma.sap.Messages.E024.getIdentifier());
		assertEquals(Message.WARNING, org.atari.asma.sap.Messages.W009.getSeverity());
		assertEquals("ASA-012", org.atari.asma.Messages.E012.getIdentifier());
		assertEquals("ASA-108", org.atari.asma.Messages.E108.getIdentifier());
		assertEquals("ASA-206", org.atari.asma.Messages.E206.getIdentifier());
		assertEquals("ASA-301", org.atari.asma.exporter.Messages.E301.getIdentifier());
		assertEquals("ASA-402", org.atari.asma.editor.Messages.W402.getIdentifier());
		assertEquals("ASA-505", org.atari.asma.browser.Messages.S505.getIdentifier());
		assertEquals("DMZ-010", org.atari.asma.demozoo.Messages.E010.getIdentifier());
		assertEquals("File x.sap has no TIME tag for the default song.",
				org.atari.asma.Messages.W011.format("x.sap"));
	}
}
