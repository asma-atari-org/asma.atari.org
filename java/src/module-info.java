module asma.atari.org {
	exports org.atari.asma.demozoo.model;
	exports org.atari.asma.demozoo;
	exports org.atari.asma.util;
	exports org.atari.asma.sap;
	exports org.atari.asma.sap.player;
	exports org.atari.asma;

	requires com.google.gson;
	requires transitive java.desktop;
	requires transitive asap;
}