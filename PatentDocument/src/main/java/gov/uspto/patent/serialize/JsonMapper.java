package gov.uspto.patent.serialize;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
import gov.uspto.patent.model.Abstract;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.CitationType;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DescSection;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.ClassificationType;
import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.IpcClassification;
import gov.uspto.patent.model.classification.UspcClassification;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Applicant;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Entity;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.Name;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;

/**
 * Serialize Patent as Json.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 * @deprecated will be replaced with JsonMapperStream
 * 
 */
public class JsonMapper implements DocumentBuilder<Patent> {

    private final boolean pretty;
    private final boolean base64;

    public JsonMapper(boolean pretty, boolean base64) {
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

        builder.add("productionDate", mapDate(patent.getDateProduced()));
        builder.add("publishedDate", mapDate(patent.getDatePublished()));

        builder.add("documentId", patent.getDocumentId() != null ? patent.getDocumentId().toText() : ""); // Patent ID or Public Application ID.
        builder.add("documentId_tokens", mapDocumentIdVariations(patent.getDocumentId()));
        
        builder.add("documentDate", mapDate(patent.getDocumentDate()));

        builder.add("applicationId", patent.getApplicationId() != null ? patent.getApplicationId().toText() : "");
        builder.add("applicationId_tokens", mapDocumentIdVariations(patent.getDocumentId()));
        builder.add("applicationDate", mapDate(patent.getApplicationDate()));

        builder.add("priorityIds", mapDocumentIds(patent.getPriorityIds()));
        builder.add("priorityIds_tokens", mapDocumentIdVariations(patent.getPriorityIds()));
        
        builder.add("relatedIds", mapDocIds(patent.getRelationIds()));
        builder.add("relatedIds_tokens", mapDocumentIdVariations(patent.getRelationIds()));

        // OtherIds contain [documentId, applicationId, relatedIds]
        builder.add("otherIds", mapDocIds(patent.getOtherIds()));
        builder.add("otherIds_tokens", mapDocumentIdVariations(patent.getOtherIds()));

        builder.add("agent", mapAgent(patent.getAgent()));
        builder.add("applicant", mapApplicant(patent.getApplicants()));
        builder.add("inventors", mapInventors(patent.getInventors()));
        builder.add("assignees", mapAssignees(patent.getAssignee()));
        builder.add("examiners", mapExaminers(patent.getExaminers()));

        builder.add("title", valueOrEmpty(patent.getTitle()));

        builder.add("abstract", mapAbstract(patent.getAbstract()));

        builder.add("description", mapDescription(patent.getDescription()));
        builder.add("claims", mapClaims(patent.getClaims()));
        builder.add("citations", mapCitations(patent.getCitations()));

        builder.add("classification", mapClassifications(patent.getClassification()));
        
        builder.add("search_classification", mapClassifications(patent.getSearchClassification()));

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
    
    private JsonArray mapDocumentIdVariations(Collection<DocumentId> docIds) {
    	
    	Set<String> docIdVariations = new LinkedHashSet<String>();
    	for(DocumentId docId : docIds) {
    		docIdVariations.addAll(getDocIdTokens(docId));
    	}
    	    	
        JsonArrayBuilder arBldr = Json.createArrayBuilder();
       
        for (String docid : docIdVariations) {
            arBldr.add(docid);
        }

        return arBldr.build();
    }
	
    private JsonArray mapDocumentIdVariations(DocumentId docId) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();
        Set<String> docIds = getDocIdTokens(docId);
        
        for (String docid : docIds) {
            arBldr.add(docid);
        }

        return arBldr.build();
    }

    private JsonArray mapDocumentIds(Collection<DocumentId> docIds) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (DocumentId docid : docIds) {
            JsonObjectBuilder jsonObj = Json.createObjectBuilder();
            jsonObj.add("id", docid.toText());
            jsonObj.add("date", mapDate(docid.getDate()));
            arBldr.add(jsonObj);
        }

        return arBldr.build();
    }
    
    private JsonArray mapDocIds(Collection<DocumentId> docIds) {
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
    
    private JsonObject mapClassifications(Collection<PatentClassification> classes) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        Set<IpcClassification> ipcClasses = PatentClassification.filterByType(classes,
                ClassificationType.IPC);

        JsonArrayBuilder ipcAr = Json.createArrayBuilder();
        for (IpcClassification claz : ipcClasses) {
            JsonObjectBuilder ipcObj = Json.createObjectBuilder();
            ipcObj.add("type", claz.isInventive() ? "inventive" : "additional");
            ipcObj.add("raw", claz.toText());
            ipcObj.add("normalized", claz.getTextNormalized());
            ipcObj.add("facets", toJsonArray(claz.toFacet()));
            ipcAr.add(ipcObj.build());

            JsonObjectBuilder ipcObj2 = Json.createObjectBuilder();
            for (PatentClassification furtherClassification : claz.getChildren()) {
                IpcClassification furtherIpc = (IpcClassification) furtherClassification;
                ipcObj2.add("type", claz.isInventive() ? "inventive" : "additional");
                ipcObj2.add("raw", furtherIpc.toText());
                ipcObj2.add("normalized", furtherIpc.getTextNormalized());
                ipcObj2.add("facets", toJsonArray(furtherIpc.toFacet()));
            }
            ipcAr.add(ipcObj2.build());
        }
        builder.add("ipc", ipcAr.build());

        Set<UspcClassification> uspcClasses = PatentClassification.filterByType(classes,
                ClassificationType.USPC);
        JsonArrayBuilder uspcAr = Json.createArrayBuilder();
        for (UspcClassification claz : uspcClasses) {
            JsonObjectBuilder uspcObj = Json.createObjectBuilder();
            // note for type: inventive doesn't really fit USPTO's USPC.
            uspcObj.add("type", claz.isMainClassification() ? "main" : "additional");
            uspcObj.add("raw", claz.toText());
            uspcObj.add("normalized", claz.getTextNormalized());
            uspcObj.add("facets", toJsonArray(claz.toFacet()));
            uspcAr.add(uspcObj.build());

            JsonObjectBuilder uspcObj2 = Json.createObjectBuilder();
            for (PatentClassification furtherClassification : claz.getChildren()) {
                UspcClassification furtherIpc = (UspcClassification) furtherClassification;
                uspcObj.add("type", claz.isMainClassification() ? "main" : "additional");
                uspcObj2.add("raw", furtherIpc.toText());
                uspcObj2.add("normalized", furtherIpc.getTextNormalized());
                uspcObj2.add("facets", toJsonArray(furtherIpc.toFacet()));
            }
            uspcAr.add(uspcObj2.build());
        }
        builder.add("uspc", uspcAr.build());

        Set<CpcClassification> cpcClasses = PatentClassification.filterByType(classes,
                ClassificationType.CPC);
        JsonArrayBuilder cpcAr = Json.createArrayBuilder();
        for (CpcClassification claz : cpcClasses) {
            JsonObjectBuilder cpcObj = Json.createObjectBuilder();
            cpcObj.add("type", claz.isInventive() ? "inventive" : "additional");
            cpcObj.add("raw", claz.toText());
            cpcObj.add("normalized", claz.getTextNormalized());
            cpcObj.add("facets", toJsonArray(claz.toFacet()));
            cpcAr.add(cpcObj.build());

            JsonObjectBuilder cpcObj2 = Json.createObjectBuilder();
            for (PatentClassification furtherClassification : claz.getChildren()) {
                CpcClassification furtherIpc = (CpcClassification) furtherClassification;
                cpcObj2.add("type", furtherIpc.isInventive() ? "inventive" : "additional");
                cpcObj2.add("raw", furtherIpc.toText());
                cpcObj2.add("normalized", furtherIpc.getTextNormalized());
                cpcObj2.add("facets", toJsonArray(furtherIpc.toFacet()));
            }
            cpcAr.add(cpcObj2.build());
        }
        builder.add("cpc", cpcAr.build());

        return builder.build();
    }

    /**
     * JsonObjects can not set a null value so return empty string.
     * 
     * @param value
     * @return
     */
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

    private JsonObject mapDate(DocumentDate date) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (date != null) {
            builder.add("raw", date.getDateText(DateTextType.RAW));
            builder.add("iso", date.getDateText(DateTextType.ISO));
        } else {
            builder.add("raw", "");
            builder.add("iso", "");
        }
        return builder.build();
    }

    private JsonObject mapAbstract(Abstract abstractObj) {
        JsonObjectBuilder jsonObj = Json.createObjectBuilder();
        
        if (abstractObj != null){
            jsonObj.add("raw", abstractObj.getRawText());
            jsonObj.add("normalized", abstractObj.getSimpleHtml());
            jsonObj.add("plain", abstractObj.getPlainText());
        }
        else {
            jsonObj.add("raw", "");
            jsonObj.add("normalized", "");
            jsonObj.add("plain", "");            
        }

        return jsonObj.build();
    }
    
    private JsonObject mapDescription(Description patentDescription) {
        JsonObjectBuilder jsonObj = Json.createObjectBuilder();
        jsonObj.add("full_raw", patentDescription.getAllRawText());

        jsonObj.add(DescSection.REL_APP_DESC.toString(),
                mapDescriptionSection(patentDescription.getSection(DescSection.REL_APP_DESC)));
        jsonObj.add(DescSection.DRAWING_DESC.toString(),
                mapDescriptionSection(patentDescription.getSection(DescSection.DRAWING_DESC)));
        jsonObj.add(DescSection.BRIEF_SUMMARY.toString(),
                mapDescriptionSection(patentDescription.getSection(DescSection.BRIEF_SUMMARY)));
        jsonObj.add(DescSection.DETAILED_DESC.toString(),
                mapDescriptionSection(patentDescription.getSection(DescSection.DETAILED_DESC)));

        return jsonObj.build();
    }

    private JsonObject mapDescriptionSection(DescriptionSection section) {
        JsonObjectBuilder jsonObj = Json.createObjectBuilder();
        if (section != null) {
            jsonObj.add("raw", section.getRawText());
            jsonObj.add("normalized", section.getSimpleHtml());
            jsonObj.add("plain", section.getPlainText());
        }
        return jsonObj.build();
    }

    private JsonArray mapAgent(Collection<Agent> agents) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Agent agent : agents) {
            JsonObjectBuilder jsonObj = Json.createObjectBuilder();
            //jsonObj.add("sequence", valueOrEmpty(agent.getSequence()));
            jsonObj.add("name", mapName(agent));
            jsonObj.add("address", mapAddress(agent));
            arBldr.add(jsonObj);
        }

        return arBldr.build();
    }

    private JsonArray mapApplicant(Collection<Applicant> applicants) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Applicant applicant : applicants) {
            JsonObjectBuilder jsonObj = Json.createObjectBuilder();
            jsonObj.add("name", mapName(applicant));
            jsonObj.add("address", mapAddress(applicant));
            arBldr.add(jsonObj);
        }

        return arBldr.build();
    }

    private JsonArray mapAssignees(Collection<Assignee> assignees) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Assignee assignee : assignees) {
            JsonObjectBuilder jsonObj = Json.createObjectBuilder();
            jsonObj.add("name", mapName(assignee));
            jsonObj.add("address", mapAddress(assignee));
            jsonObj.add("role", valueOrEmpty(assignee.getRole()));
            jsonObj.add("roleDefinition", valueOrEmpty(assignee.getRoleDesc()));
            arBldr.add(jsonObj);
        }

        return arBldr.build();
    }

    private JsonArray mapInventors(Collection<Inventor> inventors) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Inventor inventor : inventors) {
            JsonObjectBuilder jsonObj = Json.createObjectBuilder();
            jsonObj.add("sequence", valueOrEmpty(inventor.getSequence()));
            jsonObj.add("name", mapName(inventor));
            jsonObj.add("address", mapAddress(inventor));
            jsonObj.add("residency", valueOrEmpty(inventor.getResidency()));
            arBldr.add(jsonObj);
        }

        return arBldr.build();
    }

    private JsonArray mapExaminers(Collection<Examiner> examiners) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Examiner examiner : examiners) {
            JsonObjectBuilder jsonObj = Json.createObjectBuilder();
            jsonObj.add("name", valueOrEmpty(examiner.getName().getName()));
            jsonObj.add("type", valueOrEmpty(examiner.getExaminerType().toString()));
            jsonObj.add("department", valueOrEmpty(examiner.getDepartment()));
            arBldr.add(jsonObj);
        }

        return arBldr.build();
    }

    private JsonObject mapName(Entity entity) {
    	Name name = entity.getName();
        JsonObjectBuilder jsonObj = Json.createObjectBuilder();
        if (name instanceof NamePerson) {
            NamePerson perName = (NamePerson) name;
            jsonObj.add("type", "person");
            jsonObj.add("raw", valueOrEmpty(perName.getName()));
            jsonObj.add("prefix", valueOrEmpty(perName.getPrefix()));
            jsonObj.add("firstName", valueOrEmpty(perName.getFirstName()));
            jsonObj.add("middleName", valueOrEmpty(perName.getMiddleName()));
            jsonObj.add("lastName", valueOrEmpty(perName.getLastName()));
            jsonObj.add("suffix", valueOrEmpty(perName.getPrefix()));
            jsonObj.add("abbreviated", valueOrEmpty(perName.getAbbreviatedName()));
            jsonObj.add("synonyms", toJsonArray(perName.getSynonyms()));
        } else {
            NameOrg orgName = (NameOrg) name;
            jsonObj.add("type", "org");
            jsonObj.add("raw", valueOrEmpty(orgName.getName()));
            jsonObj.add("prefix", valueOrEmpty(orgName.getPrefix()));
            jsonObj.add("suffix", valueOrEmpty(orgName.getSuffix()));
            
            new OrgSynonymGenerator().computeSynonyms(entity);
            jsonObj.add("synonyms", toJsonArray(orgName.getSynonyms()));
        }
        return jsonObj.build();
    }

    private JsonObject mapAddress(Entity entity) {
    	Address address = entity.getAddress();
        JsonObjectBuilder jsonObj = Json.createObjectBuilder();
        if (address != null) {
            //jsonObj.add("street", valueOrEmpty(address.getStreet()));
            jsonObj.add("city", valueOrEmpty(address.getCity()));
            jsonObj.add("state", valueOrEmpty(address.getState()));
            //jsonObj.add("zipCode", valueOrEmpty(address.getZipCode()));
            jsonObj.add("country", valueOrEmpty(address.getCountry()));
            //jsonObj.add("email", valueOrEmpty(address.getEmail()));
            //jsonObj.add("fax", valueOrEmpty(address.getFaxNumber()));
            //jsonObj.add("phone", valueOrEmpty(address.getPhoneNumber()));
            //jsonObj.add("tokens", mapStringCollection(address.getTokenSet()));
        }
        return jsonObj.build();
    }

    private JsonArray toJsonArray(Collection<String> strings) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();
        if (strings != null) {
            for (String tok : strings) {
                arBldr.add(tok);
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

    private JsonArray mapClaims(Collection<Claim> claimList) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Claim claim : claimList) {

            JsonArrayBuilder childArBldr = Json.createArrayBuilder();
            for (Claim childClaim : claim.getChildClaims()) {
                childArBldr.add(childClaim.getId());
            }
            JsonArray childClaimJsonAr = childArBldr.build();

            JsonObjectBuilder claimTreeJson = Json.createObjectBuilder()
                    .add("parentIds", toJsonArray(claim.getDependentIds()))
                    .add("parentCount", claim.getDependentIds() != null ? claim.getDependentIds().size() : 0)
                    .add("childIds", childClaimJsonAr)
                    .add("childCount", claim.getChildClaims() != null ? claim.getChildClaims().size() : 0)
                    .add("claimTreelevel", claim.getClaimTreeLevel());

            arBldr.add(Json.createObjectBuilder().add("id", claim.getId()).add("type", claim.getClaimType().toString())
                    .add("raw", claim.getRawText()).add("normalized", claim.getSimpleHtml())
                    .add("plain", claim.getPlainText()).add("claimTree", claimTreeJson));
        }

        return arBldr.build();
    }

    private JsonArray mapCitations(Collection<Citation> CitationList) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Citation cite : CitationList) {

            if (cite.getCitType() == CitationType.NPLCIT) {
                NplCitation nplCite = (NplCitation) cite;

                JsonObjectBuilder nplObj = Json.createObjectBuilder()
        		.add("num", nplCite.getNum())
        		.add("type", "NPL")
                .add("citedBy", nplCite.getCitedBy().toString())
                .add("text", nplCite.getCiteText());

                JsonObjectBuilder extractedObj = Json.createObjectBuilder()
                .add("quotedText", nplCite.getQuotedText())
                .add("patentId", nplCite.getPatentId() != null ? nplCite.getPatentId().toText() : "");
                nplObj.add("extracted", extractedObj);

                arBldr.add(nplObj);

            } else if (cite.getCitType() == CitationType.PATCIT) {
                PatCitation patCite = (PatCitation) cite;
                arBldr.add(Json.createObjectBuilder()
                		.add("num", patCite.getNum())
                		.add("type", "PATENT")
                        .add("citedBy", patCite.getCitedBy().toString())
                        .add("text", patCite.getDocumentId().toText()));
            }
        }

        return arBldr.build();
    }

    private String base64(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Instantiate Patent from JSON string.
     * 
     * @param jsonStr
     * @return
     * @throws InvalidDataException
     * 
     * @FIXME - complete me
     */
    /*
    public Patent readJson(String jsonStr) throws InvalidDataException {
        JsonReader reader = Json.createReader(new StringReader(jsonStr));
        JsonObject jsonObj = reader.readObject();
        String type = jsonObj.getString("type");
        String docId = jsonObj.getString("documentId");
    
        if (type.equalsIgnoreCase("USPAT")) {
            PatentGranted patent = new PatentGranted(DocumentId.fromText(docId));
            return readPatent(jsonObj, patent);
        } else if (type.equalsIgnoreCase("US-PGPUB")) {
            PatentApplication patent = new PatentApplication(DocumentId.fromText(docId));
            return readApplication(jsonObj, patent);
        }
        return null;
    }
    */

    /**
     * 
     * @param jsonObj
     * @param patent
     * @return
     * 
     * @FIXME - implement
     */
    //public Patent readPatent(JsonObject jsonObj, PatentGranted patent) {
    //    return patent;
    //}

    /**
     * 
     * @param jsonObj
     * @param patent
     * @return
     * 
     * @FIXME - implement
     */
    //public Patent readApplication(JsonObject jsonObj, PatentApplication patent) {
    //    return patent;
    //}

}
