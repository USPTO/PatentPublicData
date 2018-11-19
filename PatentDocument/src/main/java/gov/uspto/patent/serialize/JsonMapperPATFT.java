package gov.uspto.patent.serialize;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import gov.uspto.patent.DateTextType;
import gov.uspto.patent.OrgSynonymGenerator;
import gov.uspto.patent.TextType;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.CitationType;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.ExaminerType;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.PatentCorpus;
import gov.uspto.patent.model.classification.ClassificationType;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.entity.Entity;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

/**
 * Serialize Patent as Json, flat with multi-valued fields in an json array.
 *
 * <p>
 * Field Names match the PATFT system at the USPTO.
 * </p>
 *
 * <p>
 * <li>"DOCID" field has been added, it is unique to the document.</li>
 * <li>Additional *_token and *_facet fields for search index creation.</li>
 * </p>
 *
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 * @see http://patft.uspto.gov/netahtml/PTO/help/helpflds.htm
 *
 */
public class JsonMapperPATFT implements DocumentBuilder<Patent> {

	private final boolean pretty;
	private final boolean base64;
	
	private final static DateTextType DATE_FORMAT = DateTextType.RAW;

	private final static TextType BODY_FORMAT = TextType.NORMALIZED; // TextType.NORMALIZED is valid HTML.

	public JsonMapperPATFT(boolean pretty, boolean base64) {
		this.pretty = pretty;
		this.base64 = base64;
	}

	@Override
	public void write(Patent patent, Writer writer) throws IOException {
		JsonObject json = buildJson(patent);
		if (pretty) {
			writer.write(getPrettyPrint(json));
		} else if (base64) {
			writer.write(base64(json.toString()));
		} else {
			writer.write(json.toString());
		}
		writer.write("\n");
	}

	public JsonObject buildJson(Patent patent) {
		JsonObjectBuilder builder = Json.createObjectBuilder();

		builder.add("DOCID", patent.getDocumentId().toText());

		int applicationTypeNum = 0;
		switch (patent.getPatentType().toString().toLowerCase()) {
		case "utility":
			applicationTypeNum = 1;
			break;
		case "reissue":
			applicationTypeNum = 2;
			break;
		case "design":
			applicationTypeNum = 4;
			break;
		case "defensive publication":
			applicationTypeNum = 5;
			break;
		case "plant":
			applicationTypeNum = 6;
			break;
		case "statutory invention registration":
			applicationTypeNum = 7;
			break;
		}
		builder.add("APT", applicationTypeNum);

		if (patent.getPatentCorpus() == PatentCorpus.USPAT) {
			builder.add("PN", patent.getDocumentId().getDocNumber());
			builder.add("PN_TOKENS", usDocIdTokens(patent.getDocumentId()));

			if (patent.getDateProduced() != null) {
				builder.add("ISD", patent.getDateProduced().getDateText(DATE_FORMAT) );
			}
			builder.add("APN", patent.getApplicationId().getDocNumber());
			builder.add("APN_TOKENS", usDocIdTokens(patent.getApplicationId()));
		} else {
			builder.add("DN", patent.getDocumentId().getDocNumber());
			builder.add("DN_TOKENS", usDocIdTokens(patent.getDocumentId()));
		}

		builder.add("PD", patent.getDatePublished().getDateText(DATE_FORMAT));
		builder.add("KD", patent.getDocumentId().getKindCode());

		if (patent.getApplicationDate() != null) {
			builder.add("APD", patent.getApplicationDate().getDateText(DATE_FORMAT));
		}

		// FOREIGN PRIORITY PRIR
		builder.add("PRIR", mapDocIds(patent.getPriorityIds(), true, false));
		builder.add("PRIR_TOKENS", mapDocIds(patent.getPriorityIds(), true, true));

		if (!patent.getPriorityIds().isEmpty()) {
			DocumentId docId = patent.getPriorityIds().iterator().next(); // TODO check if correct
			builder.add("PRAD", docId.getDate().getDateText(DATE_FORMAT));
		} else {
			builder.add("PRAD", "");
		}

		// RELATED US APPLICATIONS
		builder.add("RLAP", mapDocIds(patent.getRelationIds(), false, false));
		builder.add("RLAP_TOKENS", mapDocIds(patent.getRelationIds(), false, true));	
		builder.add("RLFD", valueOrEmpty(getFirstDate(patent.getRelationIds(), false, DATE_FORMAT)));

		// RELATED PCT APPLICATIONS
		builder.add("PCT", mapDocIds(patent.getRelationIds(), true, false));
		builder.add("PCT_TOKENS", mapDocIds(patent.getRelationIds(), true, true));
		builder.add("PTAD", valueOrEmpty(getFirstDate(patent.getRelationIds(), true, DATE_FORMAT)));

		// OtherIds contain [documentId, applicationId, relatedIds]
		// builder.add("otherIds", mapDocIds(patent.getOtherIds()));

		// AGENT/ATTORNEY
		builder.add("LREP", mapEntity(patent.getAgent(), EntityField.NAME, false));
		builder.add("LREP_TOKENS", mapEntity(patent.getAgent(), EntityField.NAME, true));

		// APPLICANT
		builder.add("AANM", mapEntity(patent.getApplicants(), EntityField.NAME, false));
		builder.add("AANM_TOKENS", mapEntity(patent.getApplicants(), EntityField.NAME, true));
		builder.add("AACI", mapEntity(patent.getApplicants(), EntityField.CITY, false));
		builder.add("AAST", mapEntity(patent.getApplicants(), EntityField.STATE, false));
		builder.add("AACO", mapEntity(patent.getApplicants(), EntityField.COUNTRY, false));
		// AAAT applicat type.

		// INVENTOR
		builder.add("IN", mapEntity(patent.getInventors(), EntityField.NAME, false));
		builder.add("IN_TOKENS", mapEntity(patent.getInventors(), EntityField.NAME, true));
		builder.add("IC", mapEntity(patent.getInventors(), EntityField.CITY, false));
		builder.add("IS", mapEntity(patent.getInventors(), EntityField.STATE, false));
		builder.add("ICN", mapEntity(patent.getInventors(), EntityField.COUNTRY, false));

		// ASSIGNEE
		builder.add("AN", mapEntity(patent.getAssignee(), EntityField.NAME, false));
		builder.add("AN_TOKENS", mapEntity(patent.getAssignee(), EntityField.NAME, true));
		builder.add("AC", mapEntity(patent.getAssignee(), EntityField.CITY, false));
		builder.add("AS", mapEntity(patent.getAssignee(), EntityField.STATE, false));
		builder.add("ACN", mapEntity(patent.getAssignee(), EntityField.COUNTRY, false));

		// EXAMINER
		List<Examiner> assistantExaminer = patent.getExaminers().stream()
				.filter(e -> ExaminerType.ASSISTANT.equals(e.getExaminerType())).collect(Collectors.toList());
		List<Examiner> primaryExaminer = patent.getExaminers().stream()
				.filter(e -> ExaminerType.PRIMARY.equals(e.getExaminerType())).collect(Collectors.toList());
		builder.add("EXA", mapEntity(assistantExaminer, EntityField.NAME, false));
		builder.add("EXA_TOKENS", mapEntity(assistantExaminer, EntityField.NAME, true));
		builder.add("EXP", mapEntity(primaryExaminer, EntityField.NAME, false));
		builder.add("EXP_TOKENS", mapEntity(primaryExaminer, EntityField.NAME, true));

		// CLAIMS ACLM
		builder.add("ACLM", mapClaimText(patent.getClaims()));

		// CITATIONS
		builder.add("OREF", mapCitations(patent.getCitations(), false, false));
		builder.add("OREF_TOKENS", mapCitations(patent.getCitations(), false, true));

		// CITATIONS Foreign (FREF)
		builder.add("FREF", mapCitations(patent.getCitations(), true, false));
		builder.add("FREF_TOKENS", mapCitations(patent.getCitations(), true, true));

		// Classifications
		builder.add("CCL", mapClassifications(patent.getClassification(), ClassificationType.USPC, false));
		builder.add("CCL_TOKENS", mapClassifications(patent.getClassification(), ClassificationType.USPC, true));

		builder.add("CPC", mapClassifications(patent.getClassification(), ClassificationType.CPC, false));
		builder.add("CPC_TOKENS", mapClassifications(patent.getClassification(), ClassificationType.CPC, true));

		builder.add("ICL", mapClassifications(patent.getClassification(), ClassificationType.IPC, false));
		builder.add("ICL_TOKENS", mapClassifications(patent.getClassification(), ClassificationType.IPC, true));

		builder.add("CCL_FACETS", mapClassificationsFacets(patent.getClassification(), ClassificationType.USPC));
		builder.add("CPC_FACETS", mapClassificationsFacets(patent.getClassification(), ClassificationType.CPC));
		builder.add("ICL_FACETS", mapClassificationsFacets(patent.getClassification(), ClassificationType.IPC));

		// TITLE
		builder.add("TTL", valueOrEmpty(patent.getTitle()));

		builder.add("ABST", patent.getAbstract().getText(BODY_FORMAT));

		builder.add("SPEC", descriptionSections(patent.getDescription(), BODY_FORMAT));

		return builder.build();
	}

	public String getPrettyPrint(JsonObject jsonObject) throws IOException {
		Map<String, Boolean> config = new HashMap<String, Boolean>();
		config.put(JsonGenerator.PRETTY_PRINTING, true);

		JsonWriterFactory writerFactory = Json.createWriterFactory(config);

		String output = null;
		try (StringWriter sw = new StringWriter(); JsonWriter jsonWriter = writerFactory.createWriter(sw)) {
			jsonWriter.writeObject(jsonObject);
			output = sw.toString();
		}

		return output;
	}

	private JsonArray descriptionSections(Description desc, TextType textType) {
		JsonArrayBuilder arBldr = Json.createArrayBuilder();
		for(DescriptionSection section: desc.getSections()) {
			arBldr.add(section.getText(textType));
		}
		return arBldr.build();
	}

	/**
	 * US Doc Id Variations
	 * 
	 * [CountryCode+Number+KindCode, CountryCode+Number, NUMBER, OriginalRaw]
	 *
	 * @param docId
	 * @return
	 */
	private JsonArray usDocIdTokens(DocumentId docId) {
		JsonArrayBuilder arBldr = Json.createArrayBuilder();
		if (docId != null) {
			Set<String> ids = new LinkedHashSet<String>();
			ids.add(docId.toText());
			ids.add(docId.getCountryCode() + docId.getDocNumber());
			ids.add(docId.getRawText());
			ids.add(docId.getDocNumber());
			for(String id: ids) {
				if (id != null) {
					arBldr.add(id);
				}
			}
		}
		return arBldr.build();
	}

	
	/*
	 * DocumentId collection is already sorted by date, filter foreign or US.
	 */
	private String getFirstDate(Collection<DocumentId> docIds, boolean foreign, DateTextType dateType) {
		for (DocumentId docId : docIds) {
			if (docId.getDate() != null) {
				if (foreign && !CountryCode.US.equals(docId.getCountryCode())) {
					return docId.getDate().getDateText(dateType);
				} else if (!foreign && CountryCode.US.equals(docId.getCountryCode())) {
					return docId.getDate().getDateText(dateType);		
				}
			}
		}
		return null;
	}

	private JsonArray mapDocIds(Collection<DocumentId> docIds, boolean foreign, boolean createTokens) {
		JsonArrayBuilder arBldr = Json.createArrayBuilder();
		if (docIds != null) {
			for (DocumentId docId : docIds) {
				if (docId != null) {
					if (foreign && !CountryCode.US.equals(docId.getCountryCode())) {
						arBldr.add(docId.toText());
					} else if (!foreign && CountryCode.US.equals(docId.getCountryCode())) {
						if (createTokens) {
							JsonArray ids = usDocIdTokens(docId);
							for (int i=0; i < ids.size(); i++) {
								arBldr.add(ids.get(i));
							}
						} else {
							arBldr.add(docId.getDocNumber());
						}
					}
				}
			}
		}
		return arBldr.build();
	}

	
	private JsonArray mapClassificationsFacets(Collection<? extends PatentClassification> classes,
			ClassificationType classType) {
		JsonArrayBuilder retArray = Json.createArrayBuilder();
		@SuppressWarnings("unchecked")
		Set<String> facets = PatentClassification.getFacetByType( ((Collection<PatentClassification>)classes),
				classType);
		for (String facet : facets) {
			retArray.add(facet);
		}
		return retArray.build();
	}

	private JsonArray mapClassifications(Collection<? extends PatentClassification> classes,
			ClassificationType classType, boolean createTokens) {

		@SuppressWarnings("unchecked")
		Set<PatentClassification> patClasses = (Set<PatentClassification>) PatentClassification.filterByType(classes,
				classType);

		JsonArrayBuilder retArray = Json.createArrayBuilder();

		Set<String> classSet = new HashSet<String>();
		for (PatentClassification claz : patClasses) {
			if (claz == null) {
				continue;
			}

			classSet.add(claz.getTextNormalized());
			if (createTokens) {
				classSet.add(claz.getTextOriginal());
			}
		}

		for (String claz : classSet) {
			if (claz != null) {
				retArray.add(claz);
			}
		}

		return retArray.build();
	}

	private JsonArray mapClaimText(Collection<Claim> claimList) {
		JsonArrayBuilder arBldr = Json.createArrayBuilder();
		for (Claim claim : claimList) {
			if (claim != null) {
				arBldr.add(claim.getSimpleHtml());
			}
		}
		return arBldr.build();
	}

	private String valueOrEmpty(String value) {
		if (value == null) {
			return "";
		} else {
			return value;
		}
	}

	private JsonArray mapEntity(Collection<? extends Entity> entities, EntityField entityField, boolean createTokens) {
		JsonArrayBuilder arBldr = Json.createArrayBuilder();

		for (Entity entity : entities) {
			switch (entityField) {
			case NAME:
				Name name = entity.getName();
				if (name instanceof NamePerson) {
					NamePerson namePerson = (NamePerson) entity.getName();
					arBldr.add(namePerson.getName());

					if (createTokens) {
						Set<String> variations = new HashSet<String>();

						// Last<comma>First Initial
						variations.add(namePerson.getAbbreviatedName());

						// Last - First - Middle
						StringBuilder stb = new StringBuilder();
						stb.append(namePerson.getLastName());
						if (namePerson.getFirstName() != null) {
							stb.append("-");
							stb.append(namePerson.getFirstName().replaceAll(" ", ""));
						}
						if (namePerson.getMiddleName() != null) {
							stb.append("-");
							stb.append(namePerson.getMiddleName());
						}
						variations.add(stb.toString());
	
						// Last - First
						StringBuilder stb2 = new StringBuilder();
						stb2.append(namePerson.getLastName());
						if (namePerson.getFirstName() != null) {
							stb2.append("-");
							stb2.append(namePerson.getFirstName().replaceAll(" ", ""));
						}
						variations.add(stb2.toString());
	
						// Last - First Initial
						StringBuilder stb3 = new StringBuilder();
						stb3.append(namePerson.getLastName());
						if (namePerson.getFirstName() != null) {
							stb3.append("-");
							stb3.append(namePerson.getFirstName().substring(0, 1));
						}
						variations.add(stb3.toString());
	
						for (String var : variations) {
							arBldr.add(var);
						}
					}
				} else {
					NameOrg orgName = ((NameOrg) name);
					arBldr.add(orgName.getName());
					if (createTokens) {
						new OrgSynonymGenerator().computeSynonyms(entity);
						for(String synName: orgName.getSynonyms()) {
							arBldr.add(synName);
						}
					}
				}
				break;
			case COUNTRY:
				if (entity.getAddress() != null) {
					arBldr.add(entity.getAddress().getCountry().toString());
				} else {
					arBldr.add("");
				}
				break;
			case STATE:
				if (entity.getAddress() != null && entity.getAddress().getState() != null) {
					arBldr.add(entity.getAddress().getState());
				}
				break;
			case CITY:
				if (entity.getAddress() != null && entity.getAddress().getCity() != null) {
					arBldr.add(entity.getAddress().getCity());
				}
				break;
			default:
				break;
			}
		}

		return arBldr.build();
	}

	private JsonArray mapCitations(Collection<Citation> CitationList, boolean onlyForeignPatents, boolean createTokens) {
		JsonArrayBuilder arBldr = Json.createArrayBuilder();

		for (Citation cite : CitationList) {
			if (!onlyForeignPatents && cite.getCitType() == CitationType.NPLCIT) {
				NplCitation nplCite = (NplCitation) cite;
				arBldr.add(nplCite.getCiteText());
			} else if (cite.getCitType() == CitationType.PATCIT) {
				PatCitation patCite = (PatCitation) cite;
				if (onlyForeignPatents && !CountryCode.US.equals(patCite.getDocumentId().getCountryCode())) {
					arBldr.add(patCite.getDocumentId().toText());
				} else if (!onlyForeignPatents) {
					// US Patent Cites also with only number.
					if (CountryCode.US.equals(patCite.getDocumentId().getCountryCode())) {
						if (createTokens) {
							JsonArray ids = usDocIdTokens(patCite.getDocumentId());
							for (int i=0; i < ids.size(); i++) {
								arBldr.add(ids.get(i));
							}
						} else {
							arBldr.add(patCite.getDocumentId().getDocNumber());
						}
					} else {
						arBldr.add(patCite.getDocumentId().toText());
					}
				}
			}
		}

		return arBldr.build();
	}

	private enum EntityField {
		NAME, FIRSTNAME, LASTNAME, ADDRESS, COUNTRY, STATE, CITY
	}

	private String base64(String string) {
		return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
	}

}
