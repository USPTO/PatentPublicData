package gov.uspto.patent.serialize;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import gov.uspto.patent.InvalidDataException;
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
import gov.uspto.patent.model.PatentApplication;
import gov.uspto.patent.model.PatentGranted;
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
public class JsonMapper {

    public String buildJson(Patent patent) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("patentCorpus", patent.getPatentCorpus().toString());
        builder.add("documentId", patent.getDocumentId().toText());

        builder.add("applicationId", patent.getApplicationId() != null ? patent.getApplicationId().toText() : "");

        builder.add("relatedIds", mapDocIds(patent.getRelationIds()));

        builder.add("otherIds", mapDocIds(patent.getOtherIds()));

        builder.add("productionDate", mapDate(patent.getDateProduced()));
        builder.add("publishedDate", mapDate(patent.getDatePublished()));

        builder.add("agent", mapAgent(patent.getAgent()));
        builder.add("applicant", mapApplicant(patent.getApplicants()));
        builder.add("inventors", mapInventors(patent.getInventors()));
        builder.add("assignees", mapAssignees(patent.getAssignee()));
        builder.add("examiners", mapExaminers(patent.getExaminers()));

        builder.add("title", valueOrEmpty(patent.getTitle()));

        builder.add("abstract", Json.createObjectBuilder().add("raw", patent.getAbstract().getRawText())
                .add("normalized", patent.getAbstract().getPlainText()));

        builder.add("description", mapDescription(patent.getDescription()));
        builder.add("claims", mapClaims(patent.getClaims()));
        builder.add("citations", mapCitations(patent.getCitations()));

        return builder.build().toString();
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
            builder.add("raw", date.getRawDate());
            builder.add("iso", date.getISOString());
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
            jsonObj.add("type", assignee.getEntityType().toString());
            jsonObj.add("address", mapAddress(assignee.getAddress()));

            arBldr.add(jsonObj);
        }

        return arBldr.build();
    }

    private JsonArray mapInventors(List<Inventor> inventors) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Inventor inventor : inventors) {
            JsonObjectBuilder jsonObj = Json.createObjectBuilder();
            jsonObj.add("name", mapName(inventor.getName()));
            jsonObj.add("address", mapAddress(inventor.getAddress()));
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
            // jsonObj.add("abbreviated",
            // valueOrEmpty(perName.getAbbreviatedName()));
            JsonArrayBuilder synonyms = Json.createArrayBuilder();
            for (String syn : perName.getSynonyms()) {
                synonyms.add(syn);
            }
            jsonObj.add("synonyms", synonyms);
        } else {
            NameOrg orgName = (NameOrg) name;
            jsonObj.add("type", "org");
            jsonObj.add("raw", valueOrEmpty(name.getName()));
            JsonArrayBuilder synonyms = Json.createArrayBuilder();
            for (String syn : orgName.getSynonyms()) {
                synonyms.add(syn);
            }
            jsonObj.add("synonyms", synonyms);
        }
        return jsonObj.build();
    }

    private JsonObject mapAddress(Address address) {
        JsonObjectBuilder jsonObj = Json.createObjectBuilder();
        jsonObj.add("street", valueOrEmpty(address.getStreet()));
        jsonObj.add("city", valueOrEmpty(address.getCity()));
        jsonObj.add("state", valueOrEmpty(address.getState()));
        jsonObj.add("country", valueOrEmpty(address.getCountry()));
        jsonObj.add("email", valueOrEmpty(address.getEmail()));
        jsonObj.add("fax", valueOrEmpty(address.getFaxNumber()));
        jsonObj.add("phone", valueOrEmpty(address.getPhoneNumber()));
        jsonObj.add("tokens", mapStringCollection(address.getTokenSet()));
        return jsonObj.build();
    }

    private JsonArray mapStringCollection(Collection<String> strings) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();
        for (String tok : strings) {
            arBldr.add(tok);
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
            arBldr.add(Json.createObjectBuilder().add("id", claim.getId()).add("type", claim.getClaimType().toString())
                    .add("raw", claim.getRawText()).add("normalized", claim.getSimpleHtml()));
        }

        return arBldr.build();
    }

    private JsonArray mapCitations(List<Citation> CitationList) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Citation cite : CitationList) {

            if (cite.getCitType() == CitationType.NPLCIT) {
                NplCitation nplCite = (NplCitation) cite;

                arBldr.add(Json.createObjectBuilder().add("num", nplCite.getNum())
                        .add("type", nplCite.getCitType().toString()).add("examinerCited", nplCite.isExaminerCited())
                        .add("text", nplCite.getCiteText()).add("quotedText", nplCite.getQuotedText()));
            } else if (cite.getCitType() == CitationType.PATCIT) {
                PatCitation patCite = (PatCitation) cite;
                arBldr.add(Json.createObjectBuilder().add("num", patCite.getNum())
                        .add("type", patCite.getCitType().toString()).add("examinerCited", patCite.isExaminerCited())
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
