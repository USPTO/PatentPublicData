package gov.uspto.patent.model;

import java.util.ArrayList;
import java.util.List;

import gov.uspto.patent.model.classification.PatentClassification;

public class PatCitation extends Citation {

	private final DocumentId documentId;
	// A citation can have both the main CPC and the main USPC classifications.
	private List<PatentClassification> classifications = new ArrayList<PatentClassification>();

	public PatCitation(String num, DocumentId documentId, CitedBy citedBy) {
		super(num, CitationType.PATCIT, citedBy);
		this.documentId = documentId;
	}

	public void addClassification(PatentClassification classification) {
		if (classification != null) {
			classifications.add(classification);
		}
	}

	public List<PatentClassification> getClassification() {
		return classifications;
	}

	public void setClassification(Iterable<PatentClassification> classifications) {
		for (PatentClassification classification : classifications) {
			addClassification(classification);
		}
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
			if (this.documentId.equals(other.getIdNoKind()) && this.documentId.equals(other.getIdNoKind())) {
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
		return "PatCitation [num=" + super.getNum() + ", documentId=" + documentId + ", citedBy=" + super.getCitedBy()
				+ ", classifications=" + classifications + "]";
	}
}
