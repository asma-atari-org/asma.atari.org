module asma.atari.org {
	exports org.atari.asma.demozoo.model;
	exports org.atari.asma.demozoo;
	exports org.atari.asma.util;
	exports org.atari.asma.sap;
	exports org.atari.asma;

	requires transitive asap;
	requires com.google.gson;
	requires transitive java.desktop;
}