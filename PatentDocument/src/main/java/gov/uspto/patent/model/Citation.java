package gov.uspto.patent.model;

import gov.uspto.patent.model.classification.Classification;

/**
 * A reference to a document considered relevant to the examination of a patent application. 
 * Citations may be made by the inventor, applicant, but most tend to be made by the examiner.
 */
public class Citation {
	private final String num;
	private final CitationType citType;
	private final DocumentId documentId;
	private boolean examinerCited;
	private Classification mainClassification;

	public Citation(String num, CitationType citType, DocumentId documentId, boolean examinerCited) {
		this.num = num;
		this.citType = citType;
		this.documentId = documentId;
		this.examinerCited = examinerCited;
	}

	public void setClassification(Classification mainClassification) {
		this.mainClassification = mainClassification;
	}

	public Classification getClassification() {
		return mainClassification;
	}

	public String getNum() {
		return num;
	}

	public DocumentId getDocumentId() {
		return documentId;
	}

	public CitationType getCitType() {
		return citType;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (!(o instanceof DocumentId)) {
			return false;
		}

		DocumentId other = (DocumentId) o;
		if (this.documentId != null && this.documentId != null) {
			if (this.documentId.equals(other.getDocNumber()) && this.documentId.equals(other.getDocNumber())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean wasExaminerCited() {
		return examinerCited;
	}

	@Override
	public String toString() {
		return "Citation [num=" + num + ", citType=" + citType + ", documentId=" + documentId + ", examinerCited="
				+ examinerCited + ", mainClassification=" + mainClassification + "]";
	}
}
