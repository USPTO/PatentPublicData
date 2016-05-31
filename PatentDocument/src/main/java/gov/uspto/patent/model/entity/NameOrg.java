package gov.uspto.patent.model.entity;

import gov.uspto.patent.InvalidDataException;

/**
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 * TODO: parse name and suffix from full name.
 */
public class NameOrg extends Name {

	//private final String fullName;
	//private Set<String> synonym = new HashSet<String>(); // nickname, aliases, varients.
	//private String suffix; // LLP, LLC, Ltd  https://en.wikipedia.org/wiki/Types_of_business_entity
							// Corp., Inc. Corporation, Incorporated
	
		// normalize: L.L.C. to LLC.
	
		/*
		 * Todo parse names such as:
		 * 
		 * 	Cisco Technology Inc.
		 * 	Cisco Technology, Inc.
		 * 	Husch Blackwell LLP
		 *  Premark Feg L.L.C.
		 */

	public NameOrg(String fullName) throws InvalidDataException{
		super(fullName);
		
		if (!validate()){
			throw new InvalidDataException("Invalid OrgName, name is blank");
		}
	}
	
	public boolean validate(){
		String fullName = super.getName();

		if (fullName == null || fullName.length() < 2){
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "OrgName["+ super.toString() +"]";
	}
}
