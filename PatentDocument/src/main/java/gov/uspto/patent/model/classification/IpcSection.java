package gov.uspto.patent.model.classification;

public enum IpcSection{
	A("HUMAN NECESSITIES"),
	B("PERFORMING OPERATIONS; TRANSPORTING"),
	C("CHEMISTRY; METALLURGY"),
	D("TEXTILES; PAPER"),
	E("FIXED CONSTRUCTIONS"),
	F("MECHANICAL ENGINEERING; LIGHTING; HEATING; WEAPONS; BLASTING"),
	G("PHYSICS"),
	H("ELECTRICITY");
	
	private String title;
	
	private IpcSection(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}
}
