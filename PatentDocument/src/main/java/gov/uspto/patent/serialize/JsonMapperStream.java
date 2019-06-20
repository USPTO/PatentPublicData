package gov.uspto.patent.serialize;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;

import gov.uspto.common.text.StringCaseUtil;
import gov.uspto.patent.DateTextType;
import gov.uspto.patent.OrgSynonymGenerator;
import gov.uspto.patent.doc.simplehtml.FreetextConfig;
import gov.uspto.patent.model.Abstract;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.CitationType;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.DocType;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.classification.ClassificationType;
import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.IpcClassification;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.UspcClassification;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Entity;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

/**
 * Output a Patent as a Stream in JSON format.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class JsonMapperStream implements DocumentBuilder<Patent>, Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonMapperStream.class);
	
	private JsonGenerator jGenerator;
	private JsonFactory jfactory = new JsonFactory();

	private final boolean pretty;
	private final boolean specOnly;
	private final boolean plainRemoveNewlines = true;

	private FreetextConfig freeTextConfig;

	public JsonMapperStream(boolean pretty) {
		this(pretty, false);
	}

	public JsonMapperStream(boolean pretty, boolean specOnly) {
		this(pretty, specOnly, new FreetextConfig(false, true));
	}

	public JsonMapperStream(boolean pretty, boolean specOnly, FreetextConfig freeTextConfig) {
		this.pretty = pretty;
		this.specOnly = specOnly;
		this.freeTextConfig = freeTextConfig;
	}

	@Override
	public void write(Patent patent, Writer writer) throws IOException {
		try(JsonGenerator jGenerator = jfactory.createGenerator(writer)){
			jGenerator.configure(Feature.ESCAPE_NON_ASCII, false);
			jGenerator.configure(Feature.AUTO_CLOSE_TARGET, false);
			if (pretty) {
				jGenerator.useDefaultPrettyPrinter();
				//jGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
			}
			this.jGenerator = jGenerator;
			output(patent, writer);
		}
	}

	private void output(Patent patent, Writer writer) throws IOException {
		jGenerator.writeStartObject(); // root.

		jGenerator.writeStringField("patentCorpus", patent.getPatentCorpus().toString());
		jGenerator.writeStringField("patentType", patent.getPatentType().toString());
		writeDateObj("productionDate", patent.getDateProduced());
		writeDateObj("publishedDate", patent.getDatePublished());

		writeDocId("documentId", patent.getDocumentId(), true);
		writeDocId("applicationId", patent.getApplicationId(), false);

		writeDocArray("priorityIds", patent.getPriorityIds(), true);
		writeDocTokens("priorityIds_tokens", patent.getPriorityIds());

		writeDocArray("relatedIds", patent.getRelationIds(), false);
		writeDocTokens("relatedIds_tokens", patent.getRelationIds());

		// OtherIds contain [documentId, applicationId, relatedIds]
		writeDocArray("otherIds", patent.getOtherIds(), false);
		writeDocTokens("otherIds_tokens", patent.getOtherIds());

		if (!specOnly) {
			writeEntity("agent", patent.getAgent());
			writeEntity("applicant", patent.getApplicants());

			writeEntity("inventors", patent.getInventors());
			writeEntity("original_assignees", patent.getAssignee());
			writeEntity("examiners", patent.getExaminers());
		}

		jGenerator.writeFieldName("title");
		jGenerator.writeStartObject();
		jGenerator.writeStringField("raw", valueOrEmpty(patent.getTitle()));
		jGenerator.writeStringField("normalized", valueOrEmpty(StringCaseUtil.toTitleCase(patent.getTitle())));
		jGenerator.writeEndObject();

		writeAbstract("abstract", patent.getAbstract());

		writeDescription("description", patent.getDescription());

		writeClaims("claims", patent.getClaims());

		writeCitations("citations", patent.getCitations());

		writeClassifications("original_classification", patent.getClassification());

		writeClassifications("search_classification", patent.getSearchClassification());

		jGenerator.writeEndObject(); // root.

		jGenerator.flush();
	}

	private void writeArray(String fieldName, Collection<String> strings) throws IOException {
		try {
			if (strings != null && strings.size() > 0) {
				jGenerator.writeFieldName(fieldName);
				jGenerator.writeStartArray(strings.size());
				for (String tok : strings) {
					if (tok == null || tok.length() == 0) {
						LOGGER.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! writeArray EMPTY STRING");
					} else {
						jGenerator.writeString(valueOrEmpty(tok));
					}
				}
				jGenerator.writeEndArray();
			}
		} catch (JsonGenerationException e) {
			LOGGER.error("Error writing field '{}' : {}", fieldName, strings.toString(), e);
		}
	}

	private void writeArray(String fieldName, String... strings) throws IOException {
		writeArray(fieldName, Arrays.asList(strings));
	}

	private void writeDateObj(String fieldName, DocumentDate date) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartObject();
		if (date != null) {
			jGenerator.writeStringField("raw", date.getDateText(DateTextType.RAW));
			jGenerator.writeStringField("iso", date.getDateText(DateTextType.ISO));
		} else {
			jGenerator.writeStringField("raw", "");
			jGenerator.writeStringField("iso", "");
		}
		jGenerator.writeEndObject();
	}

	private void writeDocId(String fieldName, DocumentId docId, boolean wantNoKind) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartObject();

		if (docId != null) {
			jGenerator.writeStringField("id", docId.toText());
			if (wantNoKind) {
				jGenerator.writeStringField("idNoKind", docId.toTextNoKind());
				jGenerator.writeStringField("kind", docId.getKindCode());
			}
			jGenerator.writeStringField("number", docId.getDocNumber());
			writeDateObj("date", docId.getDate());
		} else {
			jGenerator.writeStringField("id", "");
			if (wantNoKind) {
				jGenerator.writeStringField("idNoKind", "");
				jGenerator.writeStringField("kind", "");
			}
			jGenerator.writeStringField("number", "");
			writeDateObj("date", null);
		}

		jGenerator.writeEndObject();
	}

	private void writeDocArray(String fieldName, Iterable<DocumentId> docIds, boolean withDate) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartArray();

		for (DocumentId docid : docIds) {
			if (withDate) {
				jGenerator.writeStartObject();
				jGenerator.writeStringField("id", docid.toText());
				writeDateObj("date", docid.getDate());
				jGenerator.writeEndObject();
			} else {
				if (docid != null) {
					jGenerator.writeString(docid.toText());
				}
			}
		}

		jGenerator.writeEndArray();
	}

	private void writeDocTokens(String fieldName, Iterable<DocumentId> docIds) throws IOException {
		Set<String> idTokens = new LinkedHashSet<String>();

		for (DocumentId docId : docIds) {
			if (docId != null) {
				idTokens.addAll(getDocIdTokens(docId));
			}
		}

		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartArray();

		for (String docid : idTokens) {
			if (docid != null) {
				jGenerator.writeString(docid);
			}
		}

		jGenerator.writeEndArray();
	}

	private void writeDocTokens(String fieldName, DocumentId docId) throws IOException {
		Set<String> idTokens = new LinkedHashSet<String>();
		if (docId != null) {
			idTokens.addAll(getDocIdTokens(docId));
		}

		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartArray();

		for (String docid : idTokens) {
			if (docid != null) {
				jGenerator.writeString(docid);
			}
		}

		jGenerator.writeEndArray();
	}

	/**
	 * Doc Id Variations Tokens
	 * 
	 * [CountryCode+Number+KindCode, CountryCode+Number, NUMBER, OriginalRaw]
	 *
	 * @param docId
	 * @return
	 * @throws IOException
	 */
	private Set<String> getDocIdTokens(DocumentId docId) {
		Set<String> idTokens = new LinkedHashSet<String>();
		if (docId != null) {
			idTokens.add(docId.toText()); // full normalized.
			idTokens.add(docId.toTextNoKind()); // without Kindcode.
			// idTokens.add(docId.getRawText());

			if (docId.getCountryCode() == CountryCode.US) {
				// within corpus of US Patents don't need US country code prefix. examiners and
				// others prefer to search without it.
				idTokens.add(docId.getDocNumber());
			}
		}
		return idTokens;
	}

	/**
	 * JsonObjects can not set a null value so return empty string.
	 * 
	 * @param value
	 * @return
	 */
	private String valueOrEmpty(String value) {
		return value == null ? "" : value;
	}

	private String valueOrEmpty(Enum<?> value) {
		return value == null ? "" : value.toString();
	}

	/**
	 * Entities[Agents,Applicants,Inventor,Assignee,Examiner]
	 * 
	 * @param entities
	 * @throws IOException
	 */
	private <T extends Entity> void writeEntity(String fieldName, Collection<T> entities) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartArray();

		for (Entity entity : entities) {
			jGenerator.writeStartObject();

			jGenerator.writeFieldName("name");
			writeName(entity);

			if (entity instanceof Inventor) {
				Inventor inventor = (Inventor) entity;
				jGenerator.writeStringField("sequence", inventor.getSequence());
			} else if (entity instanceof Assignee) {
				Assignee assignee = (Assignee) entity;
				jGenerator.writeStringField("role", valueOrEmpty(assignee.getRole()));
				jGenerator.writeStringField("roleDefinition", valueOrEmpty(assignee.getRoleDesc()));
			} else if (entity instanceof Examiner) {
				Examiner examiner = (Examiner) entity;
				jGenerator.writeStringField("type", valueOrEmpty(examiner.getExaminerType()));
				jGenerator.writeStringField("department", valueOrEmpty(examiner.getDepartment()));
				jGenerator.writeEndObject();
				continue;
			}

			writeAddress(entity);

			jGenerator.writeEndObject();
		}

		jGenerator.writeEndArray();
	}

	private void writeName(Entity entity) throws IOException {
		Name name = entity.getName();

		// jGenerator.writeFieldName("name");
		jGenerator.writeStartObject();

		if (name instanceof NamePerson) {
			NamePerson perName = (NamePerson) name;
			jGenerator.writeStringField("type", "person");
			jGenerator.writeStringField("name", valueOrEmpty(perName.getName()));
			jGenerator.writeStringField("name_normcase", valueOrEmpty(perName.getNameNormalizeCase()));
			jGenerator.writeStringField("prefix", valueOrEmpty(perName.getPrefix()));
			jGenerator.writeStringField("firstName", valueOrEmpty(perName.getFirstName()));
			jGenerator.writeStringField("middleName", valueOrEmpty(perName.getMiddleName()));
			jGenerator.writeStringField("lastName", valueOrEmpty(perName.getLastName()));
			jGenerator.writeStringField("suffix", valueOrEmpty(perName.getSuffix()));
			jGenerator.writeStringField("abbreviated", valueOrEmpty(perName.getAbbreviatedName()));
			jGenerator.writeStringField("initials", valueOrEmpty(perName.getInitials()));
			writeArray("synonyms", perName.getSynonyms());
		} else {
			NameOrg orgName = (NameOrg) name;
			jGenerator.writeStringField("type", "org");
			jGenerator.writeStringField("name", valueOrEmpty(orgName.getName()));
			jGenerator.writeStringField("name_normcase", valueOrEmpty(orgName.getNameNormalizeCase()));
			jGenerator.writeStringField("prefix", valueOrEmpty(orgName.getPrefix()));
			jGenerator.writeStringField("suffix", valueOrEmpty(orgName.getSuffix()));

			new OrgSynonymGenerator().computeSynonyms(entity);
			jGenerator.writeStringField("abbreviated", valueOrEmpty(orgName.getShortestSynonym()));
			jGenerator.writeStringField("initials", valueOrEmpty(orgName.getInitials()));
			writeArray("synonyms", orgName.getSynonyms());
		}

		jGenerator.writeEndObject();
	}

	private void writeAddress(Entity entity) throws IOException {
		Address address = entity.getAddress();
		if (address != null) {
			jGenerator.writeFieldName("Address");
			jGenerator.writeStartObject();
			// jGenerator.writeStringField("street", valueOrEmpty(address.getStreet()));
			jGenerator.writeStringField("city", valueOrEmpty(address.getCity()));
			jGenerator.writeStringField("state", valueOrEmpty(address.getState()));
			// jGenerator.writeStringField("zipCode", valueOrEmpty(address.getZipCode()));
			jGenerator.writeStringField("country", valueOrEmpty(address.getCountry()));
			// jGenerator.writeStringField("email", valueOrEmpty(address.getEmail()));
			// jGenerator.writeStringField("fax", valueOrEmpty(address.getFaxNumber()));
			// jGenerator.writeStringField("phone", valueOrEmpty(address.getPhoneNumber()));
			// writeArray("tokens", address.getTokenSet());
			jGenerator.writeEndObject();
		}
	}

	private void writeAbstract(String fieldName, Abstract abstractObj) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartObject();

		if (abstractObj != null) {
			jGenerator.writeStringField("raw", abstractObj.getRawText());
			jGenerator.writeStringField("normalized", abstractObj.getSimpleHtml());
			jGenerator.writeStringField("plain",
					plainRemoveNewlines ? removeNewline(abstractObj.getPlainText(freeTextConfig))
							: abstractObj.getPlainText(freeTextConfig));
		} else {
			jGenerator.writeStringField("raw", "");
			jGenerator.writeStringField("normalized", "");
			jGenerator.writeStringField("plain", "");
		}

		jGenerator.writeEndObject();
	}

	private void writeClaims(String fieldName, Collection<Claim> claimList) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartArray();

		for (Claim claim : claimList) {
			jGenerator.writeStartObject(); // start claim
			jGenerator.writeStringField("id", claim.getId());
			jGenerator.writeStringField("type", claim.getClaimType().toString());
			jGenerator.writeStringField("raw", claim.getRawText());
			jGenerator.writeStringField("normalized", claim.getSimpleHtml());
			jGenerator.writeStringField("plain", plainRemoveNewlines ? removeNewline(claim.getPlainText(freeTextConfig))
					: claim.getPlainText(freeTextConfig));

			jGenerator.writeFieldName("claimTree");
			jGenerator.writeStartObject();
			writeArray("parentIds", claim.getDependentIds());
			jGenerator.writeNumberField("parentCount",
					claim.getDependentIds() != null ? claim.getDependentIds().size() : 0);
			jGenerator.writeNumberField("childCount",
					claim.getChildClaims() != null ? claim.getChildClaims().size() : 0);
			jGenerator.writeNumberField("claimTreelevel", claim.getClaimTreeLevel());
			jGenerator.writeEndObject(); // end claimTree

			List<String> childClaims = new ArrayList<String>();
			for (Claim childClaim : claim.getChildClaims()) {
				childClaims.add(childClaim.getId());
			}
			writeArray("childIds", childClaims);

			jGenerator.writeEndObject(); // end claim
		}

		jGenerator.writeEndArray();
	}

	private void writeCitations(String fieldName, Collection<Citation> CitationList) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartArray();

		Set<DocumentId> docIds = new LinkedHashSet<DocumentId>();
		for (Citation cite : CitationList) {
			jGenerator.writeStartObject(); // start cited
			jGenerator.writeStringField("num", cite.getNum());

			if (cite.getCitType() == CitationType.NPLCIT) {
				NplCitation nplCite = (NplCitation) cite;

				jGenerator.writeStringField("type", "NPLCITE");
				jGenerator.writeStringField("citedBy", nplCite.getCitedBy().toString());
				jGenerator.writeStringField("text", nplCite.getCiteText());

				jGenerator.writeFieldName("extracted");
				jGenerator.writeStartObject();
				jGenerator.writeStringField("quotedText", nplCite.getQuotedText());
				jGenerator.writeStringField("patentId",
						nplCite.getPatentId() != null ? nplCite.getPatentId().toText() : "");
				docIds.add(nplCite.getPatentId());
				jGenerator.writeEndObject(); // end extracted.

			} else if (cite.getCitType() == CitationType.PATCIT) {
				PatCitation patCite = (PatCitation) cite;
				DocType docType = patCite.getDocumentId().getDocType();

				docIds.add(patCite.getDocumentId());
				jGenerator.writeStringField("type", "PATCITE");
				jGenerator.writeStringField("doctype", docType != null ? docType.toString().toUpperCase() : "");
				jGenerator.writeStringField("citedBy", patCite.getCitedBy().toString());
				jGenerator.writeStringField("raw", patCite.getDocumentId().getRawText());
				jGenerator.writeStringField("text", patCite.getDocumentId().toTextNoKind());
				//jGenerator.writeStringField("name", patCite.getDocumentId().getName());
				/*
					jGenerator.writeFieldName("classification");
					jGenerator.writeStartObject();
					writeSingleClassificationType(patCite.getClassification(), ClassificationType.USPC);
					writeSingleClassificationType(patCite.getClassification(), ClassificationType.CPC);
					writeSingleClassificationType(patCite.getClassification(), ClassificationType.IPC);
					jGenerator.writeEndObject();
				*/
			}

			jGenerator.writeEndObject(); // end cite
		}

		jGenerator.writeEndArray(); // end citation array

		jGenerator.writeFieldName(fieldName + "_patent_tokens");
		jGenerator.writeStartArray();
		for (DocumentId docId : docIds) {
			for (String token : getDocIdTokens(docId)) {
				jGenerator.writeString(token);
			}
		}
		jGenerator.writeEndArray(); // end citation tokens array

	}

	private void writeDescription(String fieldName, Description patentDescription) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartObject();

		jGenerator.writeStringField("full_raw", patentDescription.getAllRawText());

		for (DescriptionSection section : patentDescription.getSections()) {
			if (section != null) {
				jGenerator.writeFieldName(section.getSection().toString());
				jGenerator.writeStartObject();
				jGenerator.writeStringField("raw", section.getRawText());
				jGenerator.writeStringField("normalized", section.getSimpleHtml());
				jGenerator.writeStringField("plain",
						plainRemoveNewlines ? removeNewline(section.getPlainText(freeTextConfig))
								: section.getPlainText(freeTextConfig));
				jGenerator.writeEndObject();
			}
		}

		jGenerator.writeEndObject();
	}

	private String removeNewline(String line) {
		return line.replaceAll("[\n\t]", " ");
	}

	@Override
	public void close() throws IOException {
		if (jGenerator != null && !jGenerator.isClosed()) {
			jGenerator.close();
		}
	}

	private void writeClassifications(String fieldName, Collection<PatentClassification> classes) throws IOException {
		jGenerator.writeFieldName(fieldName);
		jGenerator.writeStartObject();

		writeUspcClassification(classes);
		writeCpcClassification(classes);
		writeIpcClassification(classes);
		writeSingleClassificationType(classes, ClassificationType.LOCARNO);

		jGenerator.writeEndObject();
	}

	private <T extends PatentClassification> void writeIpcClassification(Collection<PatentClassification> classes)
			throws IOException {

		Map<String, List<IpcClassification>> retClasses = IpcClassification.filterIpc(classes);

		jGenerator.writeFieldName("ipc");
		jGenerator.writeStartObject(); // ipc start

		writeArray("facets_inventive", PatentClassification.getFacet(retClasses.get("inventive")));

		jGenerator.writeFieldName("inventive");
		jGenerator.writeStartArray();
		if (retClasses.containsKey("inventive")) {
			for (IpcClassification ipc : retClasses.get("inventive")) {
				jGenerator.writeStartObject();
				jGenerator.writeStringField("raw", ipc.toText());
				jGenerator.writeStringField("normalized", ipc.getTextNormalized());
				// writeArray("facets", ipc.toFacet());
				jGenerator.writeEndObject();
			}
		}
		jGenerator.writeEndArray();

		writeArray("facets_additional", PatentClassification.getFacet(retClasses.get("additional")));

		jGenerator.writeFieldName("additional");
		jGenerator.writeStartArray();
		if (retClasses.containsKey("additional")) {
			for (IpcClassification ipc : retClasses.get("additional")) {
				jGenerator.writeStartObject();
				jGenerator.writeStringField("raw", ipc.toText());
				jGenerator.writeStringField("normalized", ipc.getTextNormalized());
				// writeArray("facets", ipc.toFacet());
				jGenerator.writeEndObject();
			}
		}
		jGenerator.writeEndArray();

		jGenerator.writeEndObject(); // ipc end.
	}

	private <T extends PatentClassification> void writeCpcClassification(Collection<PatentClassification> classes)
			throws IOException {

		Map<String, List<CpcClassification>> retClasses = CpcClassification.filterCpc(classes);

		jGenerator.writeFieldName("cpc");
		jGenerator.writeStartObject();

		writeArray("facets_inventive", PatentClassification.getFacet(retClasses.get("inventive")));

		jGenerator.writeFieldName("inventive");
		jGenerator.writeStartArray();

		if (retClasses.containsKey("inventive")) {
			for (CpcClassification cpci : retClasses.get("inventive")) {
				jGenerator.writeStartObject();
				jGenerator.writeStringField("raw", cpci.toText());
				jGenerator.writeStringField("normalized", cpci.getTextNormalized());
				writeArray("facets", cpci.toFacet());
				jGenerator.writeEndObject();
			}
		}
		jGenerator.writeEndArray();

		writeArray("facets_additional", PatentClassification.getFacet(retClasses.get("additional")));

		jGenerator.writeFieldName("additional");
		jGenerator.writeStartArray();
		if (retClasses.containsKey("additional")) {
			for (CpcClassification cpci : retClasses.get("additional")) {
				jGenerator.writeStartObject();
				jGenerator.writeStringField("raw", cpci.toText());
				jGenerator.writeStringField("normalized", cpci.getTextNormalized());
				writeArray("facets", cpci.toFacet());
				jGenerator.writeEndObject();
			}
		}
		jGenerator.writeEndArray();

		jGenerator.writeEndObject();
	}

	private <T extends PatentClassification> void writeUspcClassification(Collection<PatentClassification> classes)
			throws IOException {

		jGenerator.writeFieldName("uspc");
		jGenerator.writeStartObject();

		Set<UspcClassification> classesOfType = PatentClassification.filterByType(classes, ClassificationType.USPC);

		List<UspcClassification> mainClasses = classesOfType.stream().filter(cl->cl.isMainOrInventive()).collect(Collectors.toList());
		
		writeArray("facets", PatentClassification.getFacet(classesOfType));

		jGenerator.writeFieldName("main");
		jGenerator.writeStartArray();
		for (PatentClassification mainClass : mainClasses) {
			jGenerator.writeStartObject();
			jGenerator.writeStringField("raw", mainClass.toText());
			jGenerator.writeStringField("normalized", mainClass.getTextNormalized());
			writeArray("facets", mainClass.toFacet());
			jGenerator.writeEndObject();
		}
		jGenerator.writeEndArray();

		List<UspcClassification> furtherClasses = classesOfType.stream().filter(cl->!cl.isMainOrInventive()).collect(Collectors.toList());
		
		jGenerator.writeFieldName("further");
		jGenerator.writeStartArray();
		for (PatentClassification furtherClass : furtherClasses) {
			jGenerator.writeStartObject();
			jGenerator.writeStringField("raw", furtherClass.toText());
			jGenerator.writeStringField("normalized", furtherClass.getTextNormalized());
			writeArray("facets", furtherClass.toFacet());
			jGenerator.writeEndObject();
		}
		jGenerator.writeEndArray();

		jGenerator.writeEndObject();
	}

	private void writeSingleClassificationType(Collection<PatentClassification> classes, ClassificationType classType)
			throws IOException {
		jGenerator.writeFieldName(classType.name().toLowerCase());
		jGenerator.writeStartArray();

		Set<PatentClassification> classesOfType = (Set<PatentClassification>) PatentClassification.filterByType(classes,
				classType.getJavaClass());

		for (PatentClassification mainClass : classesOfType) {
			jGenerator.writeStartObject();
			jGenerator.writeStringField("raw", mainClass.toText());
			jGenerator.writeStringField("normalized", mainClass.getTextNormalized());
			writeArray("facets", mainClass.toFacet());
			jGenerator.writeEndObject();
		}

		jGenerator.writeEndArray();
	}

}
