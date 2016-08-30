package gov.uspto.patent.model;

public class PatentGranted extends Patent {

	public PatentGranted(DocumentId documentId){
		super(PatentCorpus.PGPUB, documentId);
	}

}
