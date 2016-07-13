package gov.uspto.patent.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NplCitation extends Citation {

	private static Pattern QUOTED_TEXT = Pattern.compile("(?:[\"“]|<i>)([^\"”]+)(?:[\"”]|<\\/i>)");

	private String citeText;

	public NplCitation(String num, String citeText, boolean examinerCited) {
		super(num, CitationType.NPLCIT, examinerCited);
		this.citeText = citeText;
	}

	public String getQuotedText(){
		Matcher matcher = QUOTED_TEXT.matcher(citeText);
		if (matcher.find()){
			return matcher.group(1);
		}
		return "";
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
		return "NplCitation [num=" + super.getNum() + ", citeText=" + citeText + ", quotedText()=" + getQuotedText() +", examinerCited=" + super.isExaminerCited() + " ]";
	}
}
