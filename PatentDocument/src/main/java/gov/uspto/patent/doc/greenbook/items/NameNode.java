package gov.uspto.patent.doc.greenbook.items;

import java.util.List;

import org.dom4j.Node;
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

	public NameNode(Node itemNode) {
		super(itemNode);
	}

	@Override
	public Name read() {
		Node nameN = itemNode.selectSingleNode("NAM");
		String fullName = nameN != null ? nameN.getText() : null;
		if (fullName == null){
			return null;
		}

		List<String> nameParts = Splitter.onPattern("[,;]").limit(2).trimResults().splitToList(fullName);

		Name entityName;
		if (nameParts.size() == 2) {
			entityName = new NamePerson(nameParts.get(1), nameParts.get(0));
			try {
				((NamePerson)entityName).validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("Person Name Invalid: {}", nameN.getParent().asXML(), e);
			}
		} else {
			entityName = new NameOrg(fullName);
			try {
				((NameOrg)entityName).validate();
			} catch (InvalidDataException e) {
				LOGGER.warn("Org Name Invalid: {}", nameN.getParent().asXML(), e);
			}
		}

		return entityName;
	}

	/**
	 * Parse string containing Full Name, break into name parts and build Name object.
	 * 
	 * @param fullName
	 * @return
	 * @throws InvalidAttributesException
	 */
	public Name createName(String fullName) throws InvalidDataException {
		if (fullName == null){
			return null;
		}
		List<String> nameParts = Splitter.onPattern("[,;]").limit(2).trimResults().splitToList(fullName);

		Name entityName;
		if (nameParts.size() == 2) {
			entityName = new NamePerson(nameParts.get(1), nameParts.get(0));
		} else {
			entityName = new NameOrg(fullName);
		}

		return entityName;
	}	
}
