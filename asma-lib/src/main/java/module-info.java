module org.atari.asma {
	exports org.atari.asma;
	exports org.atari.asma.demozoo;
	exports org.atari.asma.demozoo.model;
	exports org.atari.asma.sap;
	exports org.atari.asma.stil;
	exports org.atari.asma.util;

	opens org.atari.asma to com.google.gson;
	opens org.atari.asma.demozoo to com.google.gson;
	opens org.atari.asma.demozoo.model to com.google.gson;

	requires transitive com.google.gson;
	// Automatic module of the WUDSN Base library, see plans/06.
	requires transitive com.wudsn.tools.base;
}
