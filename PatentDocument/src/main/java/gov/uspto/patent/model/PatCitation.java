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
		if (o == null || !(o instanceof DocumentId) || this.documentId == null) {
			return false;
		} else {
			DocumentId other = (DocumentId) o;
			return this.documentId.equals(other);
		}
	}

	@Override
	public String toString() {
		return "PatCitation [num=" + super.getNum() + ", documentId=" + documentId + ", citedBy=" + super.getCitedBy()
				+ ", classifications=" + classifications + "]";
	}
}
