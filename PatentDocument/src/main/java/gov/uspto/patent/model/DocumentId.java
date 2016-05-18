package gov.uspto.patent.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.naming.directory.InvalidAttributesException;

import com.google.common.base.Preconditions;

/**
 * Document ID for only Patents and Patent Applications. see WIPO ST.14.
 *
 */
public class DocumentId {
	private DocumentIdType docIdType;
	private CountryCode countryCode;
	private String docNumber;
	private String name; // name used in citation.
	private String kindCode; // different patent offices have different kindcodes.
	private DocumentDate date;

	public DocumentId(CountryCode countryCode, String docNumber) throws IllegalArgumentException {
		this(countryCode, docNumber, null);
	}

	public DocumentId(CountryCode countryCode, String docNumber, String kindCode){
		Preconditions.checkNotNull(countryCode, "CountryCode can not be set to Null");
		Preconditions.checkNotNull(docNumber, "DocNumber can not be set to Null");

		this.countryCode = countryCode;
		setDocNumber(docNumber);
		this.kindCode = kindCode;
	}

	public void setType(DocumentIdType docIdType) {
		Preconditions.checkNotNull(docIdType, "DocumentIdType can not be set to Null");
		this.docIdType = docIdType;
	}

	public DocumentIdType getType() {
		return docIdType;
	}

	private void setDocNumber(String publicationId) {
		Preconditions.checkNotNull(publicationId, "DocNumber can not be set to Null!");
		// Remove Leading Zeros.
		this.docNumber = publicationId.replaceFirst("^0+(?!$)", "");
	}

	public String getDocNumber() {
		return docNumber;
	}

	/**
	 * Full String ID Representation, example: US12345A1
	 * @return
	 */
	public String getId() {
		StringBuilder strb = new StringBuilder().append(countryCode).append(docNumber);

		if (kindCode != null) {
			strb.append(kindCode);
		}

		return strb.toString();
	}

	public CountryCode getCountryCode() {
		return countryCode;
	}

	public String getKindCode() {
		return kindCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DocumentDate getDate() {
		return date;
	}

	public void setDate(DocumentDate date) {
		this.date = date;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (!(o instanceof DocumentId)) {
			return false;
		}

		DocumentId other = (DocumentId) o;
		if (this.docNumber != null && this.docNumber != null) {
			if (this.docNumber.equals(other.docNumber) && this.docNumber.equals(other.docNumber)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public String toText() {
		return getId();
	}

	@Override
	public String toString() {
		return "DocumentId [docIdType=" + docIdType + ", countryCode=" + countryCode + ", docNumber=" + docNumber
				+ ", name=" + name + ", kindCode=" + kindCode + ", date=" + date + ", getId()=" + getId() + "]";
	}

	public static List<DocumentId> getByType(Collection<DocumentId> docIds, DocumentIdType type) {
		List<DocumentId> redIds = new LinkedList<DocumentId>();

		for (DocumentId docId : docIds) {
			if (docId != null && docId.getType() == type) {
				redIds.add(docId);
			}
		}

		return redIds;
	}
}
