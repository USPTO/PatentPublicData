package gov.uspto.patent.doc.greenbook.items;

import java.util.List;

import javax.naming.directory.InvalidAttributesException;

import org.dom4j.Node;
import com.google.common.base.Splitter;
import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.InvalidDataException;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

public class NameNode extends ItemReader<Name> {

	public NameNode(Node itemNode) {
		super(itemNode);
	}

	@Override
	public Name read() {
		Node nameN = itemNode.selectSingleNode("NAM");
		String fullName = nameN != null ? nameN.getText() : null;
		if (fullName == null) {
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
