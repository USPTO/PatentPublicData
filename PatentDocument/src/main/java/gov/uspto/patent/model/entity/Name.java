package gov.uspto.patent.model.entity;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import gov.uspto.patent.InvalidDataException;

public abstract class Name {

	private Set<String> synonym = new HashSet<String>(); // nickname, aliases, variants.
	private String suffix; // LLP, LLC, Ltd;
	private String prefix; // Dr.

	public Set<String> getSynonymSet() {
		return synonym;
	}

	/**
	 * Get Synonyms / Possible Variations
	 * 
	 * @return length sorted list
	 */
	public List<String> getSynonyms() {
		Comparator<String> byLength = (e1, e2) -> e1.length() > e2.length() ? 0 : 1;
		return synonym.stream().filter(e -> !e.startsWith("fuzz:")).sorted(byLength).collect(Collectors.toList());
	}

	public List<String> getFuzzySynonyms() {
		return synonym.stream().filter(e -> e.startsWith("fuzz:")).collect(Collectors.toList());
	}

	public String getFuzzySynonyms(String name, boolean removeName) {
		String key = "fuzz:" + name + "-";
		Optional<String> first = synonym.stream().filter(e -> e.startsWith(key)).findFirst();
		if (removeName) {
			return first.isPresent() ? first.get().substring(key.length()) : "";
		} else {
			return first.isPresent() ? first.get() : "";
		}
	}

	public String getShortestSynonym() {
		Comparator<String> byLength = (e1, e2) -> e1.length() > e2.length() ? -1 : 1;
		Optional<String> shortest = synonym.stream().filter(e -> !e.startsWith("fuzz:")).sorted(byLength.reversed())
				.findFirst();
		return shortest.isPresent() ? shortest.get() : "";
	}

	public String getLongestSynonym() {
		Comparator<String> byLength = (e1, e2) -> e1.length() > e2.length() ? -1 : 1;
		Optional<String> shortest = synonym.stream().filter(e -> !e.startsWith("fuzz:")).sorted(byLength).findFirst();
		return shortest.isPresent() ? shortest.get() : "";
	}

	public void setSynonyms(Set<String> synonym) {
		this.synonym = synonym;
	}

	public void addSynonym(String synonym) {
		this.synonym.add(synonym);
	}

	public void addFuzzSynonym(String name, String synonym) {
		this.synonym.add("fuzz:" + name + "-" + synonym);
	}

	public void addSynonymNorm(String synonym) {
		String norm1 = synonym.replaceAll("\\s+", " ").replaceFirst(",\\s*$", "").trim().toUpperCase();
		this.synonym.add(norm1);

		String norm2 = synonym.replaceAll("[\\.'](?!(?:com?)\\b)", "") // period replace with no space. except if ".COM"
				// .replaceAll("[\\p{Punct}\\p{IsPunctuation}]", " ")
				.replaceAll("[,;:\\-()\\[\\]?!'/\\\\]", " ").replaceAll("\\s+", " ").trim().toUpperCase();
		this.synonym.add(norm2);

		String norm3 = synonym.replaceAll("[\\.'](?!(?:com?)\\b)", " ") // period replace with space. except if ".COM"
				// .replaceAll("[\\p{Punct}\\p{IsPunctuation}]", " ")
				.replaceAll("[,;:\\-()\\[\\]?!'/\\\\]", " ").replaceAll("\\s+", " ").trim().toUpperCase();
		this.synonym.add(norm3);
	}

	public void addSynonym(Collection<String> synonym) {
		this.synonym.addAll(synonym);
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String toString() {
		return " synonym=" + synonym + ", prefix=" + prefix + ", suffix=" + suffix + ", fullName=" + getName()
				+ ", initials=" + getInitials();
	}

	public abstract String getName();

	public abstract String getNameNormalizeCase();

	public abstract String getInitials();
	
	public abstract boolean validate() throws InvalidDataException;
}