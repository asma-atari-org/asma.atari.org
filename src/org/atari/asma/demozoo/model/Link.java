package org.atari.asma.demozoo.model;

public final class Link {
	
	public String link_class; // "BaseUrl"
	public String url; // "https://asma.atari.org/asma/Composers/Hughes_Trevin/1bit_Body_Puncher.sap"
	
	public String toString() {
		return "{ link_class="+link_class+", url="+url+" }";
	}
}