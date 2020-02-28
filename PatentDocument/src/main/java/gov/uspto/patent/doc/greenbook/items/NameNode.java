package gov.uspto.patent.doc.greenbook.items;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.directory.InvalidAttributesException;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

public class NameNode extends ItemReader<Name> {
	private static final Logger LOGGER = LoggerFactory.getLogger(NameNode.class);

	private static final XPath NAMEXP = DocumentHelper.createXPath("NAM");

	public static final Set<String> PERSON_SUFFIXES = new HashSet<String>(
			Arrays.asList("PHD", "ESQ", "J.D", "MR", "MRS", "M.D", "DR", "P.L", "P.E", "JR", "SR", "I", "II", "III",
					"IV", "V", "1ST", "2ND", "3RD", "4TH", "5TH", "1", "2", "3", "4", "5"));

	public static final Set<String> PERSON_LONG_SUFFIXES = new HashSet<String>(Arrays.asList("ADMINISTRATOR",
			"ADMINSTRATOR", "ADMINISTRATOR AND EXECUTOR", "ADMINISTRATOR BY", "ADMINISTRATORS",
			"ADMINISTRATRIX/EXECUTRIX", "ADMINISTRATRIX", "AMINISTRATRIX", "AGENT", "PATENT AGENT", "PAT. AGENT",
			"ASSOC", "ASSICIATE", "ATTY", "ATTORNEY", "PATENT ATTORNEY", "PAT. ATTY", "CO-EXECUTRIX", "COEXECUTRIX",
			"EXECTRIX", "EXECUTOR", "EXECUTER", "EXECUTORS", "EXECUTOR OF ESTATE", "EXECUTRIX", "ESQUIRE",
			"LEGAL GUARDIAN", "GUARDIAN", "JR. DECEASED", "JR. II", "HEIR", "HEIR AND LEGAL SUCCESSOR", "HEIRS",
			"HEIRS-AT-LAW", "HEIR-AT-LAW", "HEIR AT LAW", "HEIRESS", "COEXECUTOR", "CO-EXECUTOR", "INHERITOR",
			"LEGAL AUTHORIZED HEIR", "LEGAL HEIR", "LEGAL REPRESENTATIVE", "LEGAL REPRESENTIVE",
			"A LEGAL REPRESENTATIVE", "LEGAL REPRESENTATIVE AND HEIR", "SUCCESSOR", "SOLE BENEFICIARY", "SOLE HEIR",
			"REPRESENTATIVE", "PERSONAL REPRESENTATIVE", "JOINT PERSONAL REPRESENTATIVE", "SURVIVING SPOUSE",
			"SPECIAL ADMINISTRATOR", "TRUST", "TRUSTEE", "TRUSTEE OR SUCCESSOR TRUSTEE", "DECEASED", "DECESASED",
			"LEGAL", "LEGALESS", "JR. ESQ", "IV ESQ", "PH.D", "JR. ATTY", "JR. EXECUTOR"));

	public static final Set<String> COMPANY_SUFFIXES = new HashSet<String>(
			Arrays.asList("INC", "LLC", "L.L.C", "LTD", "LTD PLC", "PLC", "P.L.C", "L.C", "LC", "LLP", "L.L.P",
					"P.L.L.C", "PLLC", "S.C", "P.A", "PA", "P.C", "PC", "P.L", "P.S", "S.P.A", "S.P.C", "CHTD",
					"IP GROUP", "INTELLECTUAL PROPERTY PRACTICE GROUP", "GROUP", "COMPANY"));

	public NameNode(Node itemNode) {
		super(itemNode);
	}

	@Override
	public Name read() {
		Node nameN = NAMEXP.selectSingleNode(itemNode);
		String fullName = nameN != null ? nameN.getText().trim() : null;
		if (fullName == null) {
			return null;
		}

		try {
			return createName(fullName);
		} catch (InvalidDataException e) {
			return null;
		}
	}

	protected String[] suffixFix(String lastName) {
		List<String> parts = Splitter.onPattern(",").limit(2).trimResults().splitToList(lastName);
		List<String> words = Splitter.onPattern("\\s").trimResults().splitToList(lastName);
		List<String> commas = Splitter.onPattern("\\s").trimResults().splitToList(lastName);

		if (parts.size() == 2) {
			String suffix = parts.get(1);
			String suffixCheck = suffix.replaceFirst("\\.$", "").replace(",", "").toUpperCase();
			String finalWord = words.get(words.size() - 1).replaceFirst("\\.$", "").replace(",", "").toUpperCase();
			String finalComma = words.get(commas.size() - 1).replaceFirst("\\.$", "").replace(",", "").toUpperCase();

			if (COMPANY_SUFFIXES.contains(suffixCheck) || COMPANY_SUFFIXES.contains(finalWord)
					|| COMPANY_SUFFIXES.contains(finalComma)) {
				return new String[] { "org", parts.get(0), suffix };
			} else if ((suffixCheck.length() < 4 && PERSON_SUFFIXES.contains(suffixCheck.toUpperCase()))
					|| PERSON_LONG_SUFFIXES.contains(suffixCheck)) {
				LOGGER.debug("Suffix Fixed, common suffix '{}' from lastname: '{}' -> '{}'", suffixCheck, lastName,
						parts.get(0));
				return new String[] { "per", parts.get(0), suffix };
			} else if (suffixCheck.startsWith("NEE ") || suffixCheck.startsWith("FORMERLY ")
					|| suffixCheck.startsWith("WIDOW ")) {
				String synonym = suffix.substring(suffix.indexOf(" ") + 1);
				String[] synCheck = suffixFix(synonym);
				String synname = synCheck != null ? synCheck[1] : synonym;
				LOGGER.debug("Suffix Fixed '{}' from lastname: '{}' -> '{}'", suffixCheck, synname, parts.get(0));
				return new String[] { "per-syn", parts.get(0), suffix, synonym };
			} else if (suffixCheck.startsWith("BY CHANGE OF NAME ")
					|| suffixCheck.startsWith("NOW BY CHANGE OF NAME ")) {
				String synonym = suffix.substring(suffixCheck.indexOf("NAME") + 5);
				LOGGER.debug("Suffix Fixed '{}' from lastname: '{}' -> '{}'", suffixCheck, lastName, parts.get(0));
				return new String[] { "per-syn-full", parts.get(0), suffix, synonym };
			} else if (suffixCheck.startsWith("BY SAID ")) {
				LOGGER.debug("Suffix Fixed '{}' from lastname: '{}' -> '{}'", suffixCheck, lastName, parts.get(0));
				return new String[] { "per", parts.get(0), suffix };
			} else if (suffixCheck.startsWith("A/K/A ")) {
				String synonym = suffix.substring(6);
				LOGGER.debug("Suffix Fixed '{}' from lastname: '{}' -> '{}'", suffixCheck, lastName, parts.get(0));
				String[] synCheck = suffixFix(synonym);
				String synname = synCheck != null ? synCheck[1] : synonym;
				LOGGER.debug("Suffix Synonym '{}' from lastname '{}'", synname, lastName);
				return new String[] { "per-syn", parts.get(0), suffix, synname };
			} else {
				LOGGER.info("Unmatched Suffix: '{}' from lastname: '{}'", suffix, lastName);
			}
		}

		return null;
	}

	/**
	 * Parse string containing Full Name, break into name parts and build Name
	 * object.
	 * 
	 * @param fullName
	 * @return
	 * @throws InvalidAttributesException
	 */
	public Name createName(String fullName) throws InvalidDataException {
		if (fullName == null || fullName.trim().isEmpty()) {
			throw new InvalidDataException("Name is missing");
		}

		List<String> nameParts = Splitter.onPattern(";").limit(2).trimResults().splitToList(fullName);

		Name entityName = null;
		if (nameParts.size() == 2) {
			String lastName = nameParts.get(0);
			String firstName = nameParts.get(1);

			if (firstName.length() > 50 && isOrgName(firstName)) {
				return new NameOrg(fullName);
			}

			if (firstName.length() > 50) {
				LOGGER.warn("Long FirstName: '{}' : {}", firstName, fullName);
			}

			lastName = lastName.replaceFirst(",? deceased\\b", "");
			lastName = lastName.replaceFirst("([a-z]) nee ", "$1, nee ");

			if (lastName.contains(",")) {
				String[] parts = suffixFix(lastName);
				if (parts != null && "per".equals(parts[0])) {
					lastName = parts[1];
					entityName = new NamePerson(firstName, lastName);
					entityName.setSuffix(parts[2]);
				} else if (parts != null && "org".equals(parts[0])) {
					lastName = parts[1];
					entityName = new NameOrg(fullName);
					entityName.setSuffix(parts[2]);
				} else if (parts != null && "per-syn".endsWith(parts[0])) {
					lastName = parts[1];
					String suffix = parts[2];
					String synonym = parts[3];
					if (synonym.contains(firstName)) {
						synonym = synonym.replace(firstName, "");
					}
					entityName = new NamePerson(firstName, lastName);
					entityName.setSuffix(suffix);
					entityName.addSynonym(synonym + ", " + firstName);
					entityName.addSynonym(synonym + ", " + firstName.subSequence(0, 1) + ".");
				} else if (parts != null && "per-syn-full".endsWith(parts[0])) {
					lastName = parts[1];
					String suffix = parts[2];
					String synonym = parts[3];
					entityName = new NamePerson(firstName, lastName);
					entityName.setSuffix(suffix);
					entityName.addSynonym(synonym);
				}
			} else {
				entityName = new NamePerson(firstName, lastName);
			}

		} else {
			entityName = new NameOrg(fullName);
		}

		return entityName;
	}

	public boolean isOrgName(String name) {
		String[] ret = suffixFix(name);
		if (ret != null && "org".equals(ret[0])) {
			return true;
		}
		return false;
	}
}
