package gov.uspto.patent.model.classification;

/**
 * 
 *  DWPI Sections 
 *  
 */
public enum DWPISection{
	/*
	 * Chemical; CPI Manual Codes
	 */
	A("Polymers and Plastics"),
	B("Pharmaceuticals"),
	C("Agricultural Chemicals"),
	D("Food, Detergents, Water Treatment and Biotechnology"),
	E("General Chemicals"),
	F("Textiles and Paper-Making"),
	G("Printing, Coating, Photographic"),
	H("Petroleum"),
	J("Chemical Engineering"),
	K("Nucleonics, Explosives and Protection"),
	L("Refractories, Ceramics, Cement and Electro(in)organics"),
	M("Metallurgy"),
	N("Catalysts"),

	/*
	 * Engineering; EPI Manual Codes
	 */
	P("General"),
	P1("Agriculture, Food, Tobacco"),
	P2("Personal, Domestic"),
	P3("Health, Amusement"),
	P4("Separating, Mixing"),
	P5("Shaping Metal"),
	P6("Shaping Non-metal"),
	P7("Pressing, Printing"),
	P8("Optics, Photography; General"),
	Q("Mechanical"),
	Q1("Vehicles in General"),
	Q2("Special Vehicles"),
	Q3("Conveying, Packaging, Storing"),
	Q4("Buildings, Construction"),
	Q5("Engines, Pumps"),
	Q6("Engineering Elements"),
	Q7("Lighting, Heating"),
	
	/*
	 * Electronic and Electrical
	 */
	S("Instrumentation, Measuring and Testing"),
	T("Computing and Control"),
	U("Semiconductors and Electronic Circuitry"),
	V("Electronic Components"),
	W("Communications"),
	X("Electric Power Engineering");
	
	private String title;
	
	private DWPISection(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}
}
