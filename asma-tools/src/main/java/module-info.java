module org.atari.asma.tools {
	exports org.atari.asma.asap;
	exports org.atari.asma.asap.player;
	exports org.atari.asma.editor;
	exports org.atari.asma.exporter;

	requires transitive org.atari.asma;
	requires com.google.gson;
	requires transitive asap;
	requires transitive java.desktop;
}
