package gov.uspto.patent.doc.greenbook.items;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.directory.InvalidAttributesException;

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
			"LEGAL", "LEGALESS", "JR. ESQ", "IV ESQ", "PH.D", "JR. ATTY"));

	public static final Set<String> COMPANY_SUFFIXES = new HashSet<String>(
			Arrays.asList("INC", "LLC", "LLP", "LTD PLC", "P.L.C", "S.C", "P.A", "PA", "P.C", "PC", "L.L.P", "PLLC"));

	// AKA also known as: FORMERLY -or- NEE -or- WIDOW

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
		String[] parts = lastName.split(",");
		if (parts.length == 2) {
			String suffixCheck = parts[1].trim().replaceFirst("\\.$", "").toUpperCase();
			if (COMPANY_SUFFIXES.contains(suffixCheck)) {
				return new String[] { "org", parts[0], suffixCheck };
			} else if ((suffixCheck.length() < 4 && PERSON_SUFFIXES.contains(suffixCheck))
					|| PERSON_LONG_SUFFIXES.contains(suffixCheck)) {
				LOGGER.debug("Suffix Fixed, parsed common suffix '{}' from lastname: '{}'", suffixCheck, lastName);
				return new String[] { "per", parts[0], suffixCheck };
			} else {
				LOGGER.info("Unmatched Suffix: {} :: {} -> {}", lastName, suffixCheck);
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
		if (fullName == null) {
			throw new InvalidDataException("Full Name is Null");
		}

		List<String> nameParts = Splitter.onPattern(";").limit(2).trimResults().splitToList(fullName);

		Name entityName;
		if (nameParts.size() == 2) {
			String lastName = nameParts.get(0);
			String firstName = nameParts.get(1);
			String suffix = null;
			boolean isPerson = true;

			if (lastName.contains(",")) {
				String[] parts = suffixFix(lastName);
				if (parts != null && "per".equals(parts[0])) {
					isPerson = true;
					lastName = parts[0];
					suffix = parts[1];
				} else if (parts != null && "org".equals(parts[0])) {
					isPerson = false;
					lastName = parts[0];
					suffix = parts[1];
				}
			}

			if (isPerson) {
				entityName = new NamePerson(firstName, lastName);
				entityName.setSuffix(suffix);
			} else {
				entityName = new NameOrg(fullName);
				entityName.setSuffix(suffix);
			}
		} else {
			entityName = new NameOrg(fullName);
		}

		return entityName;
	}

	public boolean isOrgName(String name) {
		return false;
		// special characters in orgName "&"
		//
	}
}
