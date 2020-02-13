package gov.uspto.patent.model;

import org.apache.commons.lang3.StringUtils;

import gov.uspto.patent.InvalidDataException;

/**
 * <p>Country Codes, from WIPO standard ST.3. two-letter codes
 * which mirrors the country codes listed as ISO Alpha-2 Codes in International Standard ISO 3166-1,
 * with addition to codes given to organizations.</p>
 *
 * <p>WIPO updated Country Codes April 2016.</p>
 *
 * <p>Note 1: different code existed before January 1, 1978, the pre 1978 are also listed in 
 * the WIPO ST.3.</p>
 * 
 * <p>Note 2: Sometimes states or province codes are entered erroneously as Country Codes by the applicant.</p>
 *
 * @see http://www.wipo.int/export/sites/www/standards/en/pdf/03-03-01.pdf
 * @see http://patft.uspto.gov/netahtml/PTO/help/helpctry.htm
 */
public enum CountryCode {
	AD("Andorra"),
	AE("Arab Emirates"),
	AF("Afghanistan"),
	AG("Antigua and Barbuda"),
	AI("Anguilla"),
	AL("Albania"),
	AM("Armenia"),
	AO("Angola"),
	AP("African Regional IP Organization (ARIPO)"),
	AR("Argentina"),
	AT("Austria"),
	AU("Australia"),
	AW("Aruba"),
	AZ("Azerbaijan"),
	BA("Bosnia and Herzegovina"),
	BB("Barbados"),
	BD("Bangladesh"),
	BE("Belgium"),
	BF("Burkina Faso"),
	BG("Bulgaria"),
	BH("Bahrain"),
	BI("Burundi"),
	BJ("Benin"),
	BM("Bermuda"),
	BN("Brunei Darussalam"),
	BO("Bolivia"),
	BQ("Bonaire, Sint Eustatius, and Saba"),
	BR("Brazil"),
	BS("Bahamas"),
	BT("Bhutan"),
	BV("Bouvet Island"),
	BW("Botswana"),
	BX("Benelux Office for IP (BOIP)"),
	BY("Belarus"),
	BZ("Belize"),
	CA("Canada"),
	CC("Cocos Islands"),
	CD("Congo, Democratic Republic"),
	CF("Central African Republic"),
	CG("Congo"),
	CH("Switzerland"),
	CI("Ivory Coast"),
	CK("Cook Islands"),
	CL("Chile"),
	CM("Cameroon"),
	CN("China"),
	CO("Colombia"),
	CR("Costa Rica"),
	CU("Cuba"),
	CV("Cape Verde"),
	CW("Curacao"),
	CY("Cyprus"),
	CZ("Czech Republic"), // Czechia
	DE("Germany"),
	DJ("Djibouti"),
	DK("Denmark"),
	DM("Dominica"),
	DO("Dominican Republic"),
	DZ("Algeria"),
	EA("Eurasian Patent Organization (EAPO)"),
	EC("Ecuador"),
	EE("Estonia"),
	EG("Egypt"),
	EH("Western Sahara"),
	EM("European Union IP Office (EUIPO)"),
	EP("European Patent Office (EPO)"),
	ER("Eritrea"),
	ES("Spain"),
	ET("Ethiopia"),
	FI("Finland"),
	FJ("Fiji"),
	FK("Falkland Islands (Malvinas)"),
	FO("Faroe Islands"),
	FR("France"),
	GA("Gabon"),
	GB("United Kingdom"),
	GC("Gulf Cooperation Council Patent Office (GCCPO)"),
	GD("Grenada"),
	GE("Georgia"),
	GF("French Guiana"),
	GG("Guernsey"),
	GH("Ghana"),
	GI("Gibraltar"),
	GL("Greenland"),
	GM("Gambia"),
	GN("Guinea"),
	GP("Guadeloupe"),
	GQ("Equatorial Guinea"),
	GR("Greece"),
	GS("South Georgia and the South Sandwich Islands"),
	GT("Guatemala"),
	GW("Guinea-Bissau"),
	GY("Guyana"),
	HK("China, Hong Kong SAR"),
	HN("Honduras"),
	HR("Croatia"),
	HT("Haiti"),
	HU("Hungary"),
	IB("International Bureau of the World IP Organization (WIPO"),
	ID("Indonesia"),
	IE("Ireland"),
	IL("Israel"),
	IM("Isle of Man"),
	IN("India"),
	IQ("Iraq"),
	IR("Iran"),
	IS("Iceland"),
	IT("Italy"),
	JE("Jersey"),
	JM("Jamaica"),
	JO("Jordan"),
	JP("Japan"),
	KE("Kenya"),
	KG("Kyrgyzstan"),
	KH("Cambodia"),
	KI("Kiribati"),
	KM("Comoros"),
	KN("Saint Kitts and Nevis"),
	KP("North Korea"),
	KR("South Korea"),
	KW("Kuwait"),
	KY("Cayman Islands"),
	KZ("Kazakhstan"),
	LA("Laos"),
	LB("Lebanon"),
	LC("St. Lucia"),
	LI("Liechtenstein"),
	LK("Sri Lanka"),
	LR("Liberia"),
	LS("Lesotho"),
	LT("Lithuania"),
	LU("Luxembourg"),
	LV("Latvia"),
	LY("Libya"),
	MA("Morocco"),
	MC("Monaco"),
	MD("Moldova"),
	ME("Montenegro"),
	MG("Madagascar"),
	MH("Marshall Islands"),
	MK("Macedonia (Former Yugoslav Republic)"),
	ML("Mali"),
	MM("Myanmar"),
	MN("Mongolia"),
	MO("Macau"),
	MP("Northern Mariana Islands"),
	MQ("Martinique"),
	MR("Mauritania"),
	MS("Montserrat"),
	MT("Malta"),
	MU("Mauritius"),
	MV("Maldives"),
	MW("Malawi"),
	MX("Mexico"),
	MY("Malaysia"),
	MZ("Mozambique"),
	NA("Namibia"),
	NE("Niger"),
	NG("Nigeria"),
	NI("Nicaragua"),
	NL("Netherlands"),
	NO("Norway"),
	NP("Nepal"),
	NR("Nauru"),
	NZ("New Zealand"),
	OA("African IP Organization (OAPI)"),
	OM("Oman"),
	PA("Panama"),
	PE("Peru"),
	PF("French Polynesia"),
	PG("Papua New Guinea"),
	PH("Philippines"),
	PK("Pakistan"),
	PL("Poland"),
	PT("Portugal"),
	PW("Palau"),
	PY("Paraguay"),
	QA("Qatar"),
	QZ("Community Plant Variety Office (European Union) (CPVO)"),
	RO("Romania"),
	RS("Serbia"),
	RU("Russia"),
	RW("Rwanda"),
	SA("Saudi Arabia"),
	SB("Solomon Islands"),
	SC("Seychelles"),
	SD("Sudan"),
	SE("Sweden"),
	SG("Singapore"),
	SH("Saint Helena, Ascension, and Tristan da Cunha"),
	SI("Slovenia"),
	SK("Slovakia"),
	SL("Sierra Leone"),
	SM("San Marino"),
	SN("Senegal"),
	SO("Somalia"),
	SR("Suriname"),
	SS("South Sudan"), // independence 2011, new April 2016.
	ST("Sao Tome and Principe"),
	SV("El Salvador"),
	SX("Sint Maarten (Dutch part)"),
	SY("Syria"),
	SZ("Swaziland"), // Eswatini
	TC("Turks and Caicos Islands"),
	TD("Chad"),
	TG("Togo"),
	TH("Thailand"),
	TJ("Tajikistan"),
	TL("Timor-Leste"),
	TM("Turkmenistan"),
	TN("Tunisia"),
	TO("Tonga"),
	TR("Turkey"),
	TT("Trinidad and Tobago"),
	TV("Tuvalu"),
	TW("Taiwan"),
	TZ("Tanzania"),
	UA("Ukraine"),
	UG("Uganda"),
	US("United States"),
	UY("Uruguay"),
	UZ("Uzbekistan"),
	VA("Vatican City State"),
	VC("St. Vincent and the Grenadines"),
	VE("Venezuela"),
	VG("Virgin Islands (British)"),
	VN("Viet Nam"),
	VU("Vanuatu"),
	WF("Wallis and Futuna"),
	WO("World IP Organization (WIPO)"),
	WS("Samoa"),
	XN("Nordic Patent Institute (NPI)"),
	XU("International Union for the Protection of New Varieties of Plants (UPOV)"),
	XV("Visegrad Patent Institute (VPI)"),
	YE("Yemen"),
	ZA("South Africa"),
	ZM("Zambia"),
	ZW("Zimbabwe"),
    XP("NOT PROVIDED or Non-patent literature NPL"),
	UNKNOWN("unknown"),   // when lookup fails on non-null.
	UNDEFINED("undefined"), // trying lookup with null.
	OMITTED("unknown"),

	/*
	 * Mail Address Codes
	 */
	UK("United Kingdom"), // Reserved for United Kingdom, official code is GB, mostly used in mailing address.
	AN("Netherlands Antilles"), // mailing address only.
	XH("Niue"), // mailing address only.

	/*
	 * Available for individual Use and provisional codes.
	 */
	AA("individual/provisional"),
    QM("individual/provisional"),
    QY("individual/provisional"),
    XA("individual/provisional"),
    XM("individual/provisional"),
    XO("individual/provisional"),    
    XT("individual/provisional"),
    XW("individual/provisional"),
    XY("individual/provisional"),
    XZ("individual/provisional"),
    ZZ("individual/provisional"),
    XX("UNKNOWN COUNTRY/ENTTIY"),

	/*
	 * Following no longer exist.
	 */
	YU("Yugoslavia"),
	SU("Soviet Union (USSR)");

	private String name;

	private CountryCode(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public static CountryCode fromString(String strValue) throws InvalidDataException {
		try {
			if (StringUtils.isEmpty(strValue) || "UNKNOWN".equalsIgnoreCase(strValue)) {
				throw new IllegalArgumentException();
			}
			return CountryCode.valueOf(strValue.trim().toUpperCase());
			//return CountryCode.UNDEFINED;
		} catch(IllegalArgumentException e) {
			throw new InvalidDataException("Invalid CountryCode: '" + strValue + "'");
			//return CountryCode.UNKNOWN;
		}
	}
}
