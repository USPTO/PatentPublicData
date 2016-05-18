package gov.uspto.patent.model;

public class PatentApplication extends Patent {
	
	public PatentApplication(DocumentId documentId){
		super(PatentType.USPAT, documentId);
	}

}
