package gov.uspto.patent.model;

/**
 * 
 * 
 * WIPO 3.14
 * 
 * 
 * 
 * A reference to a document considered relevant to the examination of a patent application. 
 * Citations may be made by the inventor, applicant, but most tend to be made by the examiner.
 * Examiner citations are generally more limiting in scope.
 */
public abstract class Citation {

	private final CitationType citeType;
	private final String num;
	private boolean examinerCited;

	public Citation(String num, CitationType citeType, boolean examinerCited){
		this.num = num;
		this.citeType = citeType;
		this.examinerCited = examinerCited;
	}

	public String getNum(){
		return num;
	}

	public CitationType getCitType(){
		return citeType;
	}

	public boolean isExaminerCited() {
		return examinerCited;
	}
}
