package gov.uspto.patent.model;

import javax.naming.directory.InvalidAttributesException;

/**
 * Maps historic 2 digit Country Codes to current Country Code.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public enum CountryCodeHistoric {
	AN("Albania",CountryCode.AL),
	AG("Algeria",CountryCode.DZ),
	OE("Austria",CountryCode.AT),
	BB("Bahrain",CountryCode.BH),
	BA("Bangladesh",CountryCode.BD),
	BD("Barbados",CountryCode.BB),
	DA("Benin",CountryCode.BJ),
	BH("Bhutan",CountryCode.BT),
	BT("Botswana",CountryCode.BW),
	UV("Burkina Faso",CountryCode.BF),
	HV("Burkina Faso", CountryCode.BF),
	CD("Cambodia",CountryCode.KH),
	KA("Cameroon",CountryCode.CM),
	ZR("Central African Republic",CountryCode.CF),
	TS("Chad",CountryCode.TD),
	CE("Chile",CountryCode.CL),
	RC("China",CountryCode.CN),
	CF("Congo",CountryCode.CG),
	KN("Democratic People’s Republic of Korea",CountryCode.KP),
	// Democratic Republic of the Congo CB ZR/CD
	CB("Democratic Republic of the Congo",CountryCode.CD),
	//ZR("Democratic Republic of the Congo",CountryCode.CD),
	DR("Dominican Republic",CountryCode.DO),
	ET("Egypt",CountryCode.EG),
	SL("El Salvador",CountryCode.SV),
	EA("Ethiopia",CountryCode.ET),
	SF("Finland",CountryCode.FI),
	GE("Gambia",CountryCode.GM),
	DT("Germany",CountryCode.DE),
	GU("Guatemala",CountryCode.GT),
	GI("Guinea",CountryCode.GN),
	HI("Haiti",CountryCode.HT),
	CV("Holy See",CountryCode.VA),
	HO("Honduras",CountryCode.HN),
	EI("Ireland",CountryCode.IE),
	JA("Japan",CountryCode.JP),
	KU("Kuwait",CountryCode.KW),
	FL("Liechtenstein",CountryCode.LI),
	MD("Madagascar",CountryCode.MG),
	MJ("Mali",CountryCode.ML),
	ML("Malta",CountryCode.MT),
	MT("Mauritania",CountryCode.MR),
	MS("Mauritius",CountryCode.MU),
	MO("Mongolia",CountryCode.MN),
	BU("Myanmar",CountryCode.MM),
	NA("Nicaragua",CountryCode.NI),
	NI("Niger",CountryCode.NE),
	WN("Nigeria",CountryCode.NG),
	MU("Oman",CountryCode.OM),
	PM("Panama",CountryCode.PA),
	PP("Papua New Guinea",CountryCode.PG),
	PG("Paraguay",CountryCode.PY),
	PO("Poland",CountryCode.PL),
	KS("Republic of Korea",CountryCode.KR),
	RU("Romania",CountryCode.RO),
	WL("Sierra Leone",CountryCode.SL),
	CL("Sri Lanka",CountryCode.LK),
	SW("Sweden",CountryCode.SE),
	SR("Syrian Arab Republic",CountryCode.SY),
	TP("Timor–Leste",CountryCode.TL),
	TO("Togo",CountryCode.TG),
	TI("Tonga",CountryCode.TO),
	TD("Trinidad and Tobago",CountryCode.TT),
	TA("United Republic of Tanzania",CountryCode.TZ),
	ZB("Zambia",CountryCode.ZM),
	/*
	 * Following no longer exist.
	 */
	IB("International Patent Institute", CountryCode.WO),
	CS("Czechoslovakia", CountryCode.CS),
	SY("Democratic Yemen", CountryCode.DD),
	DL("Democratic Yemen", CountryCode.DD),
	DD("Democratic Yemen", CountryCode.DD),
	SU("Soviet Union", CountryCode.SU),
	YU("Yugoslavia/Serbia and Montenegro", CountryCode.YU);

	private String name;
	private CountryCode countryCode;

	private CountryCodeHistoric(String name, CountryCode countryCode){
		this.name = name;
		this.countryCode = countryCode;
	}

	public String getName(){
		return name;
	}
	
	public CountryCode getCountryCode(){
		return countryCode;
	}

	public static CountryCode fromString(String strValue) throws InvalidAttributesException{
		try {
			if (strValue != null){
				return CountryCodeHistoric.valueOf(strValue.trim().toUpperCase()).getCountryCode();
			}
			return CountryCode.UNDEFINED;
		} catch(IllegalArgumentException e) {
			throw new InvalidAttributesException("Invalid Code: " + strValue);
			//return CountryCode.UNKNOWN;
		}
	}
}
