package gov.uspto.patent.model;

public class NplCitation extends Citation {

	private String citeText;

	public NplCitation(String num, String citeText, boolean examinerCited) {
		super(num, CitationType.NPLCIT, examinerCited);
		this.citeText = citeText;
	}

	public String getCiteText() {
		return citeText;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (!(o instanceof NplCitation)) {
			return false;
		}

		NplCitation other = (NplCitation) o;
		if (this.citeText != null && this.citeText != null) {
			if (this.citeText.equals(other.getCiteText()) && this.citeText.equals(other.getCiteText())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}	
	
	@Override
	public String toString() {
		return "NplCitation [num=" + super.getNum() + ", citeText=" + citeText + ", examinerCited=" + super.isExaminerCited() + " ]";
	}
}
