package gov.uspto.patent.model.entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import gov.uspto.common.text.StringCaseUtil;

public abstract class Name {

	private final String fullName;
	private Set<String> synonym = new HashSet<String>(); // nickname, aliases, variants.
	private String suffix; // LLP, LLC, Ltd;
	private String prefix; // Dr.

	public Name(final String fullName){
		this.fullName = fullName;
	}
	
	public String getName(){
		return fullName;
	}

	public String getNameTitleCase(){
	    return StringCaseUtil.toTitleCase(fullName);
	}

	public Set<String> getSynonymSet() {
		return synonym;
	}

	/**
	 * Get Synonyms / Possible Variations
	 * 
	 * @return length sorted list
	 */
	public Collection<String> getSynonyms() {
		List<String> synArray = Arrays.asList(synonym.toArray(new String[0]));
		Collections.sort(synArray, new OrderingByLength());
		return synArray;
	}

	public void setSynonyms(Set<String> synonym) {
		this.synonym = synonym;
	}

	public void addSynonym(String synonym){
		this.synonym.add(synonym);
	}

	public void addSynonymNorm(String synonym){
		String norm1 = synonym.replaceAll("\\s+", " ")
				.replaceAll(",$", "")
				.trim()
				.toUpperCase();
		this.synonym.add(norm1);

		String norm2 = synonym.replaceAll("[\\.'](?!com\\b)", "") // period replace with no space. except if ".COM"
				//.replaceAll("[\\p{Punct}\\p{IsPunctuation}]", " ")
				.replaceAll("[,\\-;:()\\[\\]?!'/\\\\]", " ")
				.replaceAll("\\s+", " ")
				.trim()
				.toUpperCase();
		this.synonym.add(norm2);
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
	
	public String getPrefix(){
		return prefix;
	}

	public void setPrefix(String prefix){
		this.prefix = prefix;
	}

	private class OrderingByLength extends Ordering<String> {
	    @Override
	    public int compare(String s1, String s2) {
	        return Ints.compare(s1.length(), s2.length());
	    }
	}

	@Override
	public String toString() {
		return " synonym=" + synonym + ", prefix=" + prefix + ", suffix=" + suffix + ", fullName=" + fullName;
	}
}