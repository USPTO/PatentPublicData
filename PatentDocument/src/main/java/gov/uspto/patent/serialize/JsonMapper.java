package gov.uspto.patent.serialize;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import gov.uspto.patent.DateTextType;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.CitationType;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.DescSection;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.DocumentDate;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.entity.Address;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Applicant;
import gov.uspto.patent.model.entity.Assignee;
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
 */
public class JsonMapper implements DocumentBuilder<Patent, String> {

    private boolean pretty;

    public JsonMapper(boolean pretty) {
        this.pretty = pretty;
    }

    @Override
    public String build(Patent patent) throws IOException {
        JsonObject json = buildJson(patent);
        if (pretty) {
            return getPrettyPrint(json);
        } else {
            return json.toString();
        }
    }

    public JsonObject buildJson(Patent patent) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("patentCorpus", patent.getPatentCorpus().toString());
        builder.add("documentId", patent.getDocumentId().toText()); // Patent ID or Public Application ID.

        builder.add("patentType", patent.getPatentType().toString());

        builder.add("applicationId", patent.getApplicationId() != null ? patent.getApplicationId().toText() : "");

        builder.add("relatedIds", mapDocIds(patent.getRelationIds()));

        // OtherIds contain [documentId, applicationId, relatedIds]
        builder.add("otherIds", mapDocIds(patent.getOtherIds()));

        builder.add("productionDate", mapDate(patent.getDateProduced()));
        builder.add("publishedDate", mapDate(patent.getDatePublished()));

        builder.add("agent", mapAgent(patent.getAgent()));
        builder.add("applicant", mapApplicant(patent.getApplicants()));
        builder.add("inventors", mapInventors(patent.getInventors()));
        builder.add("assignees", mapAssignees(patent.getAssignee()));
        builder.add("examiners", mapExaminers(patent.getExaminers()));

        builder.add("title", valueOrEmpty(patent.getTitle()));

        builder.add("abstract",
                Json.createObjectBuilder().add("raw", patent.getAbstract().getRawText())
                        .add("normalized", patent.getAbstract().getSimpleHtml())
                        .add("plain", patent.getAbstract().getPlainText()));

        builder.add("description", mapDescription(patent.getDescription()));
        builder.add("claims", mapClaims(patent.getClaims()));
        builder.add("citations", mapCitations(patent.getCitations()));

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

    private String valueOrEmpty(String value) {
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    private String valueOrEmpty(Enum value) {
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

    private JsonArray mapAgent(List<Agent> agents) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Agent agent : agents) {
            JsonObjectBuilder jsonObj = Json.createObjectBuilder();
            jsonObj.add("name", mapName(agent.getName()));
            jsonObj.add("address", mapAddress(agent.getAddress()));
            arBldr.add(jsonObj);
        }

        return arBldr.build();
    }

    private JsonArray mapApplicant(List<Applicant> applicants) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Applicant applicant : applicants) {
            JsonObjectBuilder jsonObj = Json.createObjectBuilder();
            jsonObj.add("name", mapName(applicant.getName()));
            jsonObj.add("address", mapAddress(applicant.getAddress()));
            arBldr.add(jsonObj);
        }

        return arBldr.build();
    }

    private JsonArray mapAssignees(List<Assignee> assignees) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Assignee assignee : assignees) {
            JsonObjectBuilder jsonObj = Json.createObjectBuilder();
            jsonObj.add("name", mapName(assignee.getName()));
            jsonObj.add("address", mapAddress(assignee.getAddress()));
            jsonObj.add("role", valueOrEmpty(assignee.getRole()));
            jsonObj.add("roleDefinition", valueOrEmpty(assignee.getRoleDesc()));
            arBldr.add(jsonObj);
        }

        return arBldr.build();
    }

    private JsonArray mapInventors(List<Inventor> inventors) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Inventor inventor : inventors) {
            JsonObjectBuilder jsonObj = Json.createObjectBuilder();
            jsonObj.add("sequence", valueOrEmpty(inventor.getSequence()));
            jsonObj.add("name", mapName(inventor.getName()));
            jsonObj.add("address", mapAddress(inventor.getAddress()));
            jsonObj.add("residency", valueOrEmpty(inventor.getResidency()));
            jsonObj.add("nationality", valueOrEmpty(inventor.getNationality()));
            arBldr.add(jsonObj);
        }

        return arBldr.build();
    }

    private JsonArray mapExaminers(List<Examiner> examiners) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Examiner examiner : examiners) {
            JsonObjectBuilder jsonObj = Json.createObjectBuilder();
            jsonObj.add("name", examiner.getName().getName());
            jsonObj.add("type", examiner.getExaminerType().toString());
            jsonObj.add("department", examiner.getDepartment());
            arBldr.add(jsonObj);
        }

        return arBldr.build();
    }

    private JsonObject mapName(Name name) {
        JsonObjectBuilder jsonObj = Json.createObjectBuilder();
        if (name instanceof NamePerson) {
            NamePerson perName = (NamePerson) name;
            jsonObj.add("type", "person");
            jsonObj.add("raw", valueOrEmpty(name.getName()));
            jsonObj.add("prefix", valueOrEmpty(perName.getPrefix()));
            jsonObj.add("firstName", valueOrEmpty(perName.getFirstName()));
            jsonObj.add("middleName", valueOrEmpty(perName.getMiddleName()));
            jsonObj.add("lastName", valueOrEmpty(perName.getLastName()));
            jsonObj.add("suffix", valueOrEmpty(perName.getPrefix()));
            jsonObj.add("abbreviated", valueOrEmpty(perName.getAbbreviatedName()));
            jsonObj.add("synonyms", mapStringCollection(perName.getSynonyms()));
        } else {
            NameOrg orgName = (NameOrg) name;
            jsonObj.add("type", "org");
            jsonObj.add("raw", valueOrEmpty(name.getName()));
            jsonObj.add("suffix", valueOrEmpty(orgName.getSuffix()));
            jsonObj.add("synonyms", mapStringCollection(orgName.getSynonyms()));
        }
        return jsonObj.build();
    }

    private JsonObject mapAddress(Address address) {
        JsonObjectBuilder jsonObj = Json.createObjectBuilder();
        if (address != null) {
            jsonObj.add("street", valueOrEmpty(address.getStreet()));
            jsonObj.add("city", valueOrEmpty(address.getCity()));
            jsonObj.add("state", valueOrEmpty(address.getState()));
            jsonObj.add("zipCode", valueOrEmpty(address.getZipCode()));
            jsonObj.add("country", valueOrEmpty(address.getCountry()));
            jsonObj.add("email", valueOrEmpty(address.getEmail()));
            jsonObj.add("fax", valueOrEmpty(address.getFaxNumber()));
            jsonObj.add("phone", valueOrEmpty(address.getPhoneNumber()));
            //jsonObj.add("tokens", mapStringCollection(address.getTokenSet()));
        }
        return jsonObj.build();
    }

    private JsonArray mapStringCollection(Collection<String> strings) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();
        if (strings != null) {
            for (String tok : strings) {
                arBldr.add(tok);
            }
        }
        return arBldr.build();
    }

    private JsonArray mapDocIds(List<DocumentId> docIds) {
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

    private JsonArray mapClaims(List<Claim> claimList) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Claim claim : claimList) {

            JsonArrayBuilder childArBldr = Json.createArrayBuilder();
            for (Claim childClaim : claim.getChildClaims()) {
                childArBldr.add(childClaim.getId());
            }
            JsonArray childClaimJsonAr = childArBldr.build();

            JsonObjectBuilder claimTreeJson = Json.createObjectBuilder()
                    .add("parentIds", mapStringCollection(claim.getDependentIds()))
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

    private JsonArray mapCitations(List<Citation> CitationList) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Citation cite : CitationList) {

            if (cite.getCitType() == CitationType.NPLCIT) {
                NplCitation nplCite = (NplCitation) cite;

                arBldr.add(Json.createObjectBuilder().add("num", nplCite.getNum()).add("type", "NPL")
                        .add("citedBy", nplCite.getCitType().toString()).add("examinerCited", nplCite.isExaminerCited())
                        .add("text", nplCite.getCiteText()).add("quotedText", nplCite.getQuotedText()));
            } else if (cite.getCitType() == CitationType.PATCIT) {
                PatCitation patCite = (PatCitation) cite;
                arBldr.add(Json.createObjectBuilder().add("num", patCite.getNum()).add("type", "PATENT")
                        .add("citedBy", patCite.getCitType().toString()).add("examinerCited", patCite.isExaminerCited())
                        .add("text", patCite.getDocumentId().toText()));
            }
        }

        return arBldr.build();
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
