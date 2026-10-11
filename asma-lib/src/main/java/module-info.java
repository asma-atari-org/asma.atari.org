module org.atari.asma {
	exports org.atari.asma;
	exports org.atari.asma.demozoo;
	exports org.atari.asma.demozoo.model;
	exports org.atari.asma.sap;
	exports org.atari.asma.stil;
	exports org.atari.asma.util;

	// Opened unconditionally: gson deserializes the model, WUDSN Base's NLS
	// reflects on the repository classes and loads their .properties resources.
	opens org.atari.asma;
	opens org.atari.asma.demozoo;
	opens org.atari.asma.demozoo.model to com.google.gson;
	opens org.atari.asma.sap;

	requires transitive com.google.gson;
	// Automatic module of the WUDSN Base library, see plans/06.
	requires transitive com.wudsn.tools.base;
}
