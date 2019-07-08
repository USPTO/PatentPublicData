package gov.uspto.patent.model;

import gov.uspto.patent.InvalidDataException;

/**
 * Type of Patent
 *
 * <pre>
 * UTILITY : 1
 * REISSUE : 2
 * DESIGN  : 4
 * DEF : 5
 * SIR : 7
 * UNDEFINED : 0
 * </pre>
 */
public enum PatentType {
	UTILITY(1), // over 90% of patents issued by USPTO.
	DESIGN(4), 
	PLANT(6),
	REISSUE(2), // reissue to correct error in issued utility, design or plant patent.
	DEF(5), // (DEF = DEFENSIVE_PUBLICATION) used until 1985-86.
	SIR(7), // (SIR = STATUTORY_INVENTION_REGISTRATION) from 1985-86 to 2011. Occurs in
			// Patent XML as app-type "SIR".
	UNDEFINED(0);

	private final int number;

	PatentType(int number) {
		this.number = number;
	}

	/**
	 * Number Representation
	 * 
	 * @return
	 */
	public int getNumber() {
		return number;
	}

	public static PatentType fromNumber(int num) throws InvalidDataException {
		for (PatentType type : PatentType.values()) {
			if (type.getNumber() == num) {
				return type;
			}
		}
		return PatentType.UNDEFINED;
	}

	public static PatentType fromString(String strValue) throws InvalidDataException {
		try {
			if (strValue != null) {
				return PatentType.valueOf(strValue.trim().toUpperCase());
			}
			return PatentType.UNDEFINED;
		} catch (IllegalArgumentException e) {
			throw new InvalidDataException("Invalid PatentType: '" + strValue + "'");
			// return CountryCode.UNKNOWN;
		}
	}

	public static PatentType parse(Integer num) throws InvalidDataException {
		return fromNumber(num);
	}

	public static PatentType parse(String str) throws InvalidDataException {
		return fromString(str);
	}
}
