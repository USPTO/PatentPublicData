package gov.uspto.patent.serialize;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import org.apache.commons.text.WordUtils;

import gov.uspto.common.text.StringCaseUtil;
import gov.uspto.patent.DateTextType;
import gov.uspto.patent.FreetextField;
import gov.uspto.patent.OrgSynonymGenerator;
import gov.uspto.patent.TextType;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.CitationType;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DescSection;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.ClassificationType;
import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.IpcClassification;
import gov.uspto.patent.model.classification.UspcClassification;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Entity;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

/**
 * Serialize Patent as Json, flat with multi-valued fields in an json array.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class JsonMapperFlat implements DocumentBuilder<Patent> {

    private final boolean pretty;
    private final boolean base64;

    public JsonMapperFlat(boolean pretty, boolean base64) {
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
    }

    public JsonObject buildJson(Patent patent) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("patentCorpus", patent.getPatentCorpus().toString());
        builder.add("patentType", patent.getPatentType().toString());

        if (patent.getDateProduced() != null) {
            builder.add("productionDateRaw", patent.getDateProduced().getDateText(DateTextType.RAW));
            builder.add("productionDateIso", patent.getDateProduced().getDateText(DateTextType.ISO));
        }

        if (patent.getDatePublished() != null) {
            builder.add("publishedDateRaw", patent.getDatePublished().getDateText(DateTextType.RAW));
            builder.add("publishedDateIso", patent.getDatePublished().getDateText(DateTextType.ISO));
        }

        builder.add("documentId", patent.getDocumentId().toText()); // Patent ID or Public Application ID.
        if (patent.getDocumentDate() != null) {
            builder.add("documentDateRaw", patent.getDocumentDate().getDateText(DateTextType.RAW));
            builder.add("documentDateIso", patent.getDocumentDate().getDateText(DateTextType.ISO));
        }
        builder.add("documentId_tokens", mapDocIdVariations(patent.getDocumentId()));

        builder.add("applicationId", patent.getApplicationId() != null ? patent.getApplicationId().toText() : "");
        if (patent.getApplicationDate() != null) {
            builder.add("applicationDateRaw", patent.getApplicationDate().getDateText(DateTextType.RAW));
            builder.add("applicationDateIso", patent.getApplicationDate().getDateText(DateTextType.ISO));
        }
        builder.add("applicationId_tokens", mapDocIdVariations(patent.getApplicationId()));

        builder.add("priorityIds", mapDocIds(patent.getPriorityIds()));
        builder.add("priorityIds_tokens", mapDocIdVariations(patent.getPriorityIds()));
        
        builder.add("relatedIds", mapDocIds(patent.getRelationIds()));
        builder.add("relatedIds_tokens", mapDocIdVariations(patent.getRelationIds()));

        // OtherIds contain [documentId, applicationId, relatedIds]
        builder.add("otherIds", mapDocIds(patent.getOtherIds()));
        builder.add("otherIds_tokens", mapDocIdVariations(patent.getOtherIds()));

        builder.add("agent", mapEntity(patent.getAgent(), EntityField.NAME));
        builder.add("agentLastName", mapEntity(patent.getAgent(), EntityField.FIRSTNAME));
        builder.add("agentFirstName", mapEntity(patent.getAgent(), EntityField.LASTNAME));
        builder.add("agentAddress", mapEntity(patent.getAgent(), EntityField.ADDRESS));
        builder.add("agentRepType", mapAgentRep(patent.getAgent()));

        builder.add("applicantRaw", mapEntity(patent.getApplicants(), EntityField.RAWNAME));
        builder.add("applicant", mapEntity(patent.getApplicants(), EntityField.NAME));
        builder.add("applicantLastName", mapEntity(patent.getApplicants(), EntityField.FIRSTNAME));
        builder.add("applicantFirstName", mapEntity(patent.getApplicants(), EntityField.LASTNAME));
        builder.add("applicantSynonyms", mapEntity(patent.getApplicants(), EntityField.SYNONYMS));
        builder.add("applicantAbbrev", mapEntity(patent.getApplicants(), EntityField.NAMEABBREV));
        builder.add("applicantInitials", mapEntity(patent.getApplicants(), EntityField.NAMEINITIALS));
        builder.add("applicantCity", mapEntity(patent.getApplicants(), EntityField.CITY));
        builder.add("applicantState", mapEntity(patent.getApplicants(), EntityField.STATE));
        builder.add("applicantCountry", mapEntity(patent.getApplicants(), EntityField.COUNTRY));

        builder.add("inventorRaw", mapEntity(patent.getInventors(), EntityField.RAWNAME));
        builder.add("inventor", mapEntity(patent.getInventors(), EntityField.NAME));
        builder.add("inventorLastName", mapEntity(patent.getInventors(), EntityField.FIRSTNAME));
        builder.add("inventorFirstName", mapEntity(patent.getInventors(), EntityField.LASTNAME));
        builder.add("inventorSynonyms", mapEntity(patent.getInventors(), EntityField.SYNONYMS));
        builder.add("inventorAbbrev", mapEntity(patent.getInventors(), EntityField.NAMEABBREV));
        builder.add("inventorInitials", mapEntity(patent.getInventors(), EntityField.NAMEINITIALS));
        builder.add("inventorCity", mapEntity(patent.getInventors(), EntityField.CITY));
        builder.add("inventorState", mapEntity(patent.getInventors(), EntityField.STATE));
        builder.add("inventorCountry", mapEntity(patent.getInventors(), EntityField.COUNTRY));
        builder.add("inventorResidency", mapInventor(patent.getInventors(), InventorField.RESIDENCE));

        builder.add("assigneeRaw", mapEntity(patent.getAssignee(), EntityField.RAWNAME));
        builder.add("assignee", mapEntity(patent.getAssignee(), EntityField.NAME));
        builder.add("assigneeSynonyms", mapEntity(patent.getApplicants(), EntityField.SYNONYMS));
        builder.add("assigneeRoles", mapAssigneeRoles(patent.getAssignee()));
        builder.add("assigneeAddress", mapEntity(patent.getAssignee(), EntityField.ADDRESS));
        builder.add("assigneeCity", mapEntity(patent.getAssignee(), EntityField.CITY));
        builder.add("assigneeState", mapEntity(patent.getAssignee(), EntityField.STATE));
        builder.add("assigneeCountry", mapEntity(patent.getAssignee(), EntityField.COUNTRY));

        builder.add("examiner", mapEntity(patent.getExaminers(), EntityField.NAME));
        builder.add("examinerDepartment", mapExaminerDepartment(patent.getExaminers()));

        builder.add("title", valueOrEmpty(patent.getTitle()));
        builder.add("titleNorm", valueOrEmpty(StringCaseUtil.toTitleCase(patent.getTitle())));

        mapFreetextField(patent.getAbstract(), "abstract", builder);

        builder.add("descFullRaw", patent.getDescription().getAllRawText());

        DescriptionSection descSection = patent.getDescription().getSection(DescSection.REL_APP_DESC);
        if (descSection != null) {
            mapFreetextField(descSection, "descRelApp", builder);
        }

        descSection = patent.getDescription().getSection(DescSection.DRAWING_DESC);
        if (descSection != null) {
            mapFreetextField(descSection, "descDraw", builder);
        }

        descSection = patent.getDescription().getSection(DescSection.BRIEF_SUMMARY);
        if (descSection != null) {
            mapFreetextField(descSection, "descBrief", builder);
        }

        descSection = patent.getDescription().getSection(DescSection.DETAILED_DESC);
        if (descSection != null) {
            mapFreetextField(descSection, "descDetailed", builder);
        }

        mapClaimText(patent.getClaims(), builder);

        builder.add("citationsExaminerNpl", mapCitations(patent.getCitations(), Citation.CitedBy.EXAMINER, CitationType.NPLCIT));
        builder.add("citationsExaminerPat", mapCitations(patent.getCitations(), Citation.CitedBy.EXAMINER, CitationType.PATCIT));

        builder.add("citationsApplicantNpl", mapCitations(patent.getCitations(), Citation.CitedBy.APPLICANT, CitationType.NPLCIT));
        builder.add("citationsApplicantPat", mapCitations(patent.getCitations(), Citation.CitedBy.APPLICANT, CitationType.PATCIT));

        builder.add("citationsThirdPartyNpl", mapCitations(patent.getCitations(), Citation.CitedBy.THIRD_PARTY, CitationType.NPLCIT));
        builder.add("citationsThirdPartyPat", mapCitations(patent.getCitations(), Citation.CitedBy.THIRD_PARTY, CitationType.PATCIT));

        builder.add("citationsOtherNpl", mapCitations(patent.getCitations(), Citation.CitedBy.OTHER, CitationType.NPLCIT));
        builder.add("citationsOtherPat", mapCitations(patent.getCitations(), Citation.CitedBy.OTHER, CitationType.PATCIT));

        builder.add("citations_tokens", mapCitationsVariations(patent.getCitations()));

        mapClassifications("classification", patent.getClassification(), builder);
        
        mapClassifications("searchClassification", patent.getSearchClassification(), builder);

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

    private JsonArray mapDocIds(Iterable<DocumentId> docIds) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();
        if (docIds != null) {
            for (DocumentId docId : docIds) {
                if (docId != null) {
                    arBldr.add(docId.toText());
                }
            }
        }
        return arBldr.build();
    }

    private JsonArray mapDocIdVariations(Iterable<DocumentId> docIds) {
    	Set<String> docIdVarations = new LinkedHashSet<String>();
    	if (docIds != null) {
            for (DocumentId docId : docIds) {
            	if (docId != null) {
            		docIdVarations.addAll(getDocIdTokens(docId));
            	}
            }
    	}

        return toJsonArray(docIdVarations);
    }
    
    private JsonArray mapDocIdVariations(DocumentId docId) {
        return toJsonArray(getDocIdTokens(docId));
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
			idTokens.add(docId.getCountryCode() + docId.getDocNumber()); // without Kindcode.
			//idTokens.add(docId.getRawText());

			if (docId.getCountryCode() == CountryCode.US) {
				// within corpus of US Patents don't need US country code prefix. examiners and others prefer to search without it.
				idTokens.add(docId.getDocNumber()); 
			}
		}
		return idTokens;
	}

    private void mapClassifications(String prefixFieldName, Collection<PatentClassification> classes, JsonObjectBuilder builder) {

        Set<IpcClassification> ipcClasses = PatentClassification.filterByType(classes, ClassificationType.IPC);
        for (IpcClassification claz : ipcClasses) {
            JsonArrayBuilder futherRawAr = Json.createArrayBuilder();
            JsonArrayBuilder futherNormAr = Json.createArrayBuilder();
            SortedSet<String> futherFacets = new TreeSet<String>();
        	
        	if (claz.isMainOrInventive()) {
        		builder.add(prefixFieldName + "IpcInventiveRaw", claz.toText());
        		builder.add(prefixFieldName + "IpcInventiveNormalized", claz.getTextNormalized());
        		builder.add(prefixFieldName + "IpcInventiveFacets", toJsonArray(claz.getTree().getLeafFacets()));
        	}
        	else {
                futherRawAr.add(claz.toText());
                futherNormAr.add(claz.getTextNormalized());
                futherFacets.addAll(claz.getTree().getLeafFacets());
        	}

            builder.add(prefixFieldName + "IpcAdditionalRaw", futherRawAr.build());
            builder.add(prefixFieldName + "IpcAdditionalNormalized", futherNormAr.build());
            builder.add(prefixFieldName + "IpcAdditionalFacets", toJsonArray(futherFacets));
        }

        Set<UspcClassification> uspcClasses = PatentClassification.filterByType(classes,
                ClassificationType.USPC);
        for (UspcClassification claz : uspcClasses) {
            builder.add(prefixFieldName + "UspcMainRaw", claz.toText());
            builder.add(prefixFieldName + "UspcMainNormalized", claz.getTextNormalized());
            builder.add(prefixFieldName + "UspcMainFacets", toJsonArray(claz.getTree().getLeafFacets()));

            JsonArrayBuilder futherRawAr = Json.createArrayBuilder();
            JsonArrayBuilder futherNormAr = Json.createArrayBuilder();
            SortedSet<String> futherFacets = new TreeSet<String>();

            builder.add(prefixFieldName + "UspcAdditionalRaw", futherRawAr.build());
            builder.add(prefixFieldName + "UspcAdditionalNormalized", futherNormAr.build());
            builder.add(prefixFieldName + "UspcAdditionalFacets", toJsonArray(futherFacets));
        }

        Set<CpcClassification> cpcClasses = PatentClassification.filterByType(classes,
                ClassificationType.CPC);
        JsonArrayBuilder futherRawAr = Json.createArrayBuilder();
        JsonArrayBuilder futherNormAr = Json.createArrayBuilder();
        SortedSet<String> futherFacets = new TreeSet<String>();
        for (CpcClassification claz : cpcClasses) {
        	if (claz.isMainOrInventive()) {
        		builder.add(prefixFieldName + "CpcInventiveRaw", claz.toText());
        		builder.add(prefixFieldName + "CpcInventiveNormalized", claz.getTextNormalized());
        		builder.add(prefixFieldName + "CpcInventiveFacets", toJsonArray(claz.getTree().getLeafFacets()));
        	} else {
                futherRawAr.add(claz.toText());
                futherNormAr.add(claz.getTextNormalized());
                futherFacets.addAll(claz.getTree().getLeafFacets());
        	}
        }
        builder.add(prefixFieldName + "CpcAdditionalRaw", futherRawAr.build());
        builder.add(prefixFieldName + "CpcAdditionalNormalized", futherNormAr.build());
        builder.add(prefixFieldName + "CpcAdditionalFacets", toJsonArray(futherFacets));
        
        
    }

    private JsonArray mapExaminerDepartment(Collection<Examiner> examiners) {

        Set<String> depts = new HashSet<String>();
        for (Examiner examiner : examiners) {
            depts.add(examiner.getDepartment());
        }

        return toJsonArray(depts);
    }

    private JsonArray mapAgentRep(Collection<Agent> agents) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();
        for (Agent agent : agents) {
            arBldr.add(agent.getRepType().toString());
        }
        return arBldr.build();
    }

    private JsonArray mapInventor(Collection<Inventor> inventors, InventorField inventorField) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Inventor inventor : inventors) {
            switch (inventorField) {
            case RESIDENCE:
                if (inventor.getResidency() != null) {
                    arBldr.add(valueOrEmpty(inventor.getResidency()));
                }
                break;
            }
        }

        return arBldr.build();
    }

    private JsonArray mapAssigneeRoles(Collection<Assignee> assignees) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Assignee assignee : assignees) {
            arBldr.add(valueOrEmpty(assignee.getRole()));
            //arBldr.add(valueOrEmpty(assignee.getRoleDesc())); // "roleDefinition", 
        }

        return arBldr.build();
    }

    private void mapFreetextField(FreetextField field, String fieldName, JsonObjectBuilder builder) {
        for (TextType textType : TextType.values()) {
            builder.add(fieldName + "" + WordUtils.capitalize(textType.name().toLowerCase()), field.getText(textType));
        }
    }

    private void mapClaimText(Collection<Claim> claimList, JsonObjectBuilder builder) {
        for (TextType textType : TextType.values()) {
            JsonArrayBuilder arBldr = Json.createArrayBuilder();
            for (Claim claim : claimList) {
                if (claim != null) {
                    arBldr.add(claim.getText(textType));
                }
            }
            builder.add("claim" + WordUtils.capitalize(textType.name().toLowerCase()), arBldr.build());
        }
    }

    private String valueOrEmpty(String value) {
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    private String valueOrEmpty(Enum<?> value) {
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    private JsonArray mapEntity(Collection<? extends Entity> entities, EntityField entityField) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Entity entity : entities) {
            switch (entityField) {
			case RAWNAME:
	            arBldr.add(entity.getName().getName());
	            break;
            case NAME:
	            arBldr.add(entity.getName().getNameNormalizeCase());
                break;
            case FIRSTNAME:
                if (entity.getName() instanceof NamePerson) {
                    NamePerson name2 = (NamePerson) entity.getName();
                    if (name2.getFirstName() != null) {
                        arBldr.add(name2.getFirstName());
                    }
                }
                break;
            case LASTNAME:
                if (entity.getName() instanceof NamePerson) {
                    NamePerson name4 = (NamePerson) entity.getName();
                    if (name4.getLastName() != null) {
                        arBldr.add(name4.getLastName());
                    }
                }
                break;
            case NAMESUFFIX:
                if (entity.getName() instanceof NamePerson) {
                    NamePerson name5 = (NamePerson) entity.getName();
                    if (name5.getLastName() != null) {
                        arBldr.add(name5.getSuffix());
                    }
                }
                break;
            case NAMEABBREV:
                if (entity.getName() instanceof NamePerson) {
                    NamePerson name5 = (NamePerson) entity.getName();
                    if (name5.getLastName() != null) {
                        arBldr.add(name5.getAbbreviatedName());
                    }
                }
                break;
            case NAMEINITIALS:
	            arBldr.add(entity.getName().getInitials());
                break;
            case ADDRESS:
                if (entity.getAddress() != null) {
                    arBldr.add(entity.getAddress().toText());
                }
                break;
            case COUNTRY:
                if (entity.getAddress() != null) {
                    arBldr.add(entity.getAddress().getCountry().toString());
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
            case SYNONYMS:
            	 if (entity.getName() instanceof NamePerson) {
            		 NamePerson perName = (NamePerson) entity.getName();
            		 arBldr.add(toJsonArray(perName.getSynonyms()));
            	 } else {
            		 new OrgSynonymGenerator().computeSynonyms(entity);
            		 NameOrg orgName = (NameOrg) entity.getName();
            		 arBldr.add(toJsonArray(orgName.getSynonyms()));
            	 }
			default:
				break;
            }
        }

        return arBldr.build();
    }

    private JsonArray toJsonArray(Collection<String> strings) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();
        if (strings != null) {
            for (String tok : strings) {
            	if (tok != null) {
            		arBldr.add(tok);
            	}
            }
        }
        return arBldr.build();
    }

    private JsonArray toJsonArray(String... strings) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();
        if (strings != null) {
            for (String tok : strings) {
                arBldr.add(tok);
            }
        }
        return arBldr.build();
    }

    private JsonArray mapCitationsVariations(Collection<Citation> CitationList) {
    	Set<DocumentId> docIds = new LinkedHashSet<DocumentId>();
    	for (Citation cite : CitationList) {
    		 if (cite.getCitType() == CitationType.NPLCIT) {
    			 NplCitation nplCite = (NplCitation) cite;
    			 docIds.add(nplCite.getPatentId());
    		 }
    		 else if (cite.getCitType() == CitationType.PATCIT) {
                 PatCitation patCite = (PatCitation) cite;
    			 docIds.add(patCite.getDocumentId());
    		 }
    	}
    	return mapDocIdVariations(docIds);
    }

    private JsonArray mapCitations(Collection<Citation> CitationList, Citation.CitedBy citedBy, CitationType citeType) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Citation cite : CitationList) {
            if (citedBy == cite.getCitedBy()) {
                if (citeType == CitationType.NPLCIT && cite.getCitType() == CitationType.NPLCIT) {
                    NplCitation nplCite = (NplCitation) cite;
                    arBldr.add(nplCite.getCiteText());
                } else if (citeType == CitationType.PATCIT && cite.getCitType() == CitationType.PATCIT) {
                    PatCitation patCite = (PatCitation) cite;
                    arBldr.add(patCite.getDocumentId().toText());
                }
            }
        }

        return arBldr.build();
    }

    private enum EntityField {
    	RAWNAME, NAME, NAMESUFFIX, NAMEABBREV, NAMEINITIALS, FIRSTNAME, LASTNAME, ADDRESS, COUNTRY, STATE, CITY, SYNONYMS
    }

    private enum InventorField {
        RESIDENCE
    }

    private String base64(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
    }

}
