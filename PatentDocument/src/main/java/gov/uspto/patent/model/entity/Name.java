package gov.uspto.patent.model.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class Name {

	private final String fullName;
	private Set<String> synonym = new HashSet<String>(); // nickname, aliases, variants.
	private String suffix; // LLP, LLC, Ltd; Dr.  

	public Name(final String fullName){
		this.fullName = fullName;
	}
	
	public String getName(){
		return fullName;
	}

	public Set<String> getSynonyms() {
		return synonym;
	}

	public void setSynonyms(Set<String> synonym) {
		this.synonym = synonym;
	}

	public void addSynonym(String synonym){
		this.synonym.add(synonym);
	}

	public void addSynonym(Collection<String> synonym){
		this.synonym.addAll(synonym);
	}

	public String getSuffix(){
		return suffix;
	}

	public void setSuffix(String suffix){
		this.suffix = suffix;
	}

	@Override
	public String toString() {
		return " synonym=" + synonym + ", suffix=" + suffix + ", fullName=" + fullName;
	}
}