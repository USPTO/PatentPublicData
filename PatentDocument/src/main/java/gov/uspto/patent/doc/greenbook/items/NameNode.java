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

	public static final Set<String> PERSON_SUFFIXES = new HashSet<String>(Arrays.asList("JR", "SR", "I", "II", "III",
			"IV", "V", "1ST", "2ND", "3RD", "4TH", "5TH", "1", "2", "3", "4", "5"));
	public static final Set<String> PERSON_LONG_SUFFIXES = new HashSet<String>(Arrays.asList("DECEASED", "HEIR",
			"HEIRS", "HEIRS-AT-LAW", "HEIRESS", "COEXECUTOR", "CO-EXECUTOR", "CO-EXECUTRIX", "COEXECUTRIX", "EXECTRIX",
			"EXECUTOR", "EXECUTORS", "EXECUTRIX", "ADMINISTRATOR", "ADMINISTRATRIX", "LEGAL REPRESENTATIVE",
			"LEGAL HEIR", "SPECIAL ADMINISTRATOR", "TRUSTEE", "REPRESENTATIVE", "PERSONAL REPRESENTATIVE",
			"SURVIVING SPOUSE", "INHERITOR", "LEGAL REPRESENTATIVE AND HEIR", "HEIR AND LEGAL SUCCESSOR",
			"LEGAL AUTHORIZED HEIR", "SOLE HEIR", "ADMINISTRATRIX/EXECUTRIX"));

	public NameNode(Node itemNode) {
		super(itemNode);
	}

	@Override
	public Name read() {
		Node nameN = NAMEXP.selectSingleNode(itemNode);
		String fullName = nameN != null ? nameN.getText() : null;
		if (fullName == null) {
			return null;
		}

		List<String> nameParts = Splitter.onPattern(";").limit(2).trimResults().splitToList(fullName);

		Name entityName;
		if (nameParts.size() == 2) {
			String lastName = nameParts.get(0);
			String firstName = nameParts.get(1);
			String suffix = null;
			if (lastName.contains(",")) {
				String[] parts = lastName.split(",");
				if (parts.length == 2) {
					String suffixCheck = parts[1].trim().replaceFirst("\\.$", "").toUpperCase();
					if ((suffixCheck.length() < 4 && PERSON_SUFFIXES.contains(suffixCheck))
							|| PERSON_LONG_SUFFIXES.contains(suffixCheck)) {
						LOGGER.debug("Suffix Fixed, parsed common suffix '{}' from lastname: '{}'", suffixCheck,
								lastName);
						lastName = parts[0];
						suffix = suffixCheck;
					} else {
						LOGGER.info("Unmatched Suffix: {} :: {} -> {}", fullName, lastName, suffixCheck);
					}
				}
			}

			entityName = new NamePerson(firstName, lastName);
			entityName.setSuffix(suffix);
		} else {
			entityName = new NameOrg(fullName);
		}

		return entityName;
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
			entityName = new NamePerson(nameParts.get(1), nameParts.get(0));
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
