module org.atari.asma.tools {
	exports org.atari.asma.asap;
	exports org.atari.asma.asap.player;
	exports org.atari.asma.browser;
	exports org.atari.asma.browser.composer;
	exports org.atari.asma.browser.file;
	exports org.atari.asma.browser.group;
	exports org.atari.asma.editor;
	exports org.atari.asma.exporter;

	// Opened unconditionally: WUDSN Base's NLS reflects on the repository
	// classes and loads their .properties resources.
	opens org.atari.asma.browser;

	requires transitive org.atari.asma;
	requires com.google.gson;
	requires com.wudsn.tools.base;
	requires transitive asap;
	requires transitive java.desktop;
}
