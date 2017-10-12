package gov.uspto.patent.model;

public class PatentGranted extends Patent {

	public PatentGranted(DocumentId documentId, PatentType patentType){
		super(PatentCorpus.USPAT, documentId, patentType);
	}

}
