package gov.uspto.patent.model;

import gov.uspto.patent.model.classification.PatentClassification;

public class PatCitation extends Citation {

	private final DocumentId documentId;
	private PatentClassification mainClassification;

	public PatCitation(String num, DocumentId documentId, boolean examinerCited) {
		super(num, CitationType.PATCIT, examinerCited);
		this.documentId = documentId;
	}

	public void setClassification(PatentClassification mainClassification) {
		this.mainClassification = mainClassification;
	}

	public PatentClassification getClassification() {
		return mainClassification;
	}

	public DocumentId getDocumentId() {
		return documentId;
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

	@Override
	public String toString() {
		return "PatCitation [num=" + super.getNum() + ", documentId=" + documentId + ", isExaminerCited=" + super.isExaminerCited()
				+ ", mainClassification=" + mainClassification + "]";
	}
}
