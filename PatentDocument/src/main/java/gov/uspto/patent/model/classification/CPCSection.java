package gov.uspto.patent.model.classification;

/**
 * 
 *  CPC follows IPC 
 *
 */
public enum CPCSection{
	A("HUMAN NECESSITIES"),
	B("PERFORMING OPERATIONS; TRANSPORTING"),
	C("CHEMISTRY; METALLURGY"),
	D("TEXTILES; PAPER"),
	E("FIXED CONSTRUCTIONS"),
	F("MECHANICAL ENGINEERING; LIGHTING; HEATING; WEAPONS; BLASTING"),
	G("PHYSICS"),
	H("ELECTRICITY"),
	Y("EMERGING CROSS-SECTIONAL TECHNOLOGIES");
	
	private String title;
	
	private CPCSection(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}
}
