package gov.uspto.patent.model;

/**
 * Citation
 *
 * <p>
 * WIPO 3.14
 * </p>
 *
 * <p>
 * A reference to a document considered relevant to the examination of a patent
 * application. Citations may be made by the applicant, examiner or by a
 * third-party. The large majority are cited by the applicant, some by the
 * examiner (around 15-17%), and a relatively small number by a third-party.
 * </p>
 * 
 * <p>
 * Examiner and third-party citations have a tendency to be more limiting in
 * scope.
 * </p>
 */
public abstract class Citation {

	public enum CitedBy {
		EXAMINER, // used in Patent XML and SGML.
		APPLICANT, // only used in Patent XML.
		THIRD_PARTY, // only used in Patent XML.
		UNDEFINED, // when unknown or in Patent Greenbook since not available or defined.
		OTHER // only used in Patent SGML.
	};

	private final CitationType citeType;
	private final String num;
	private CitedBy citedBy;

	public Citation(String num, CitationType citeType, CitedBy citedBy) {
		this.num = num;
		this.citeType = citeType;
		this.citedBy = citedBy;
	}

	public String getNum() {
		return num;
	}

	public void setCitedBy(CitedBy citedBy) {
		this.citedBy = citedBy;
	}

	public CitationType getCitType() {
		return citeType;
	}

	public CitedBy getCitedBy() {
		return citedBy;
	}

	public boolean isExaminerCited() {
		return citedBy == CitedBy.EXAMINER;
	}

	public boolean isApplicantCited() {
		return citedBy == CitedBy.APPLICANT;
	}

	public boolean isThirdPartyCited() {
		return citedBy == CitedBy.THIRD_PARTY;
	}
}
