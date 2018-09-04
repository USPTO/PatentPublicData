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

import org.apache.commons.lang3.text.WordUtils;

import gov.uspto.patent.DateTextType;
import gov.uspto.patent.FreetextField;
import gov.uspto.patent.TextType;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.CitationType;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.DescSection;
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
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Assignee;
import gov.uspto.patent.model.entity.Entity;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.Inventor;
import gov.uspto.patent.model.entity.Name;
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

        builder.add("applicationId", patent.getApplicationId() != null ? patent.getApplicationId().toText() : "");
        if (patent.getApplicationDate() != null) {
            builder.add("applicationDateRaw", patent.getApplicationDate().getDateText(DateTextType.RAW));
            builder.add("applicationDateIso", patent.getApplicationDate().getDateText(DateTextType.ISO));
        }

        builder.add("priorityIds", mapDocIds(patent.getPriorityIds()));
        
        builder.add("relatedIds", mapDocIds(patent.getRelationIds()));

        // OtherIds contain [documentId, applicationId, relatedIds]
        builder.add("otherIds", mapDocIds(patent.getOtherIds()));

        builder.add("agent", mapEntity(patent.getAgent(), EntityField.NAME));
        builder.add("agentLastName", mapEntity(patent.getAgent(), EntityField.FIRSTNAME));
        builder.add("agentFirstName", mapEntity(patent.getAgent(), EntityField.LASTNAME));
        builder.add("agentAddress", mapEntity(patent.getAgent(), EntityField.ADDRESS));
        builder.add("agentRepType", mapAgentRep(patent.getAgent()));

        builder.add("applicant", mapEntity(patent.getApplicants(), EntityField.NAME));
        builder.add("applicantLastName", mapEntity(patent.getApplicants(), EntityField.FIRSTNAME));
        builder.add("applicantFirstName", mapEntity(patent.getApplicants(), EntityField.LASTNAME));
        builder.add("applicantAddress", mapEntity(patent.getApplicants(), EntityField.ADDRESS));
        builder.add("applicantCity", mapEntity(patent.getApplicants(), EntityField.CITY));
        builder.add("applicantState", mapEntity(patent.getApplicants(), EntityField.STATE));
        builder.add("applicantCountry", mapEntity(patent.getApplicants(), EntityField.COUNTRY));

        builder.add("inventor", mapEntity(patent.getInventors(), EntityField.NAME));
        builder.add("inventorLastName", mapEntity(patent.getInventors(), EntityField.FIRSTNAME));
        builder.add("inventorFirstName", mapEntity(patent.getInventors(), EntityField.LASTNAME));
        builder.add("inventorAddress", mapEntity(patent.getInventors(), EntityField.ADDRESS));
        builder.add("inventorCity", mapEntity(patent.getInventors(), EntityField.CITY));
        builder.add("inventorState", mapEntity(patent.getInventors(), EntityField.STATE));
        builder.add("inventorCountry", mapEntity(patent.getInventors(), EntityField.COUNTRY));
        builder.add("inventorNationality", mapInventor(patent.getInventors(), InventorField.NATIONALITY));
        builder.add("inventorResidency", mapInventor(patent.getInventors(), InventorField.RESIDENCE));

        builder.add("assignee", mapEntity(patent.getAssignee(), EntityField.NAME));
        builder.add("assigneeRoles", mapAssigneeRoles(patent.getAssignee()));
        builder.add("assigneeAddress", mapEntity(patent.getAssignee(), EntityField.ADDRESS));
        builder.add("assigneeCity", mapEntity(patent.getAssignee(), EntityField.CITY));
        builder.add("assigneeState", mapEntity(patent.getAssignee(), EntityField.STATE));
        builder.add("assigneeCountry", mapEntity(patent.getAssignee(), EntityField.COUNTRY));

        builder.add("examiner", mapEntity(patent.getExaminers(), EntityField.NAME));
        builder.add("examinerDepartment", mapExaminerDepartment(patent.getExaminers()));

        builder.add("title", valueOrEmpty(patent.getTitle()));

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

        builder.add("citationsExaminerNpl", mapCitations(patent.getCitations(), true, CitationType.NPLCIT));
        builder.add("citationsExaminerPat", mapCitations(patent.getCitations(), true, CitationType.PATCIT));

        builder.add("citationsApplicantNpl", mapCitations(patent.getCitations(), false, CitationType.NPLCIT));
        builder.add("citationsApplicantClientPat", mapCitations(patent.getCitations(), false, CitationType.PATCIT));

        mapClassifications(patent.getClassification(), builder);

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

    private void mapClassifications(Collection<? extends PatentClassification> classes, JsonObjectBuilder builder) {

        @SuppressWarnings("unchecked")
        Set<IpcClassification> ipcClasses = (Set<IpcClassification>) PatentClassification.filterByType(classes, ClassificationType.IPC);
        for (IpcClassification claz : ipcClasses) {
            builder.add("ClassificationIpcMainRaw", claz.toText());
            builder.add("ClassificationIpcMainNormalized", claz.getTextNormalized());
            builder.add("ClassificationIpcMainFacets", toJsonArray(claz.toFacet()));

            JsonArrayBuilder futherRawAr = Json.createArrayBuilder();
            JsonArrayBuilder futherNormAr = Json.createArrayBuilder();
            SortedSet<String> futherFacets = new TreeSet<String>();

            for (PatentClassification furtherClassification : claz.getChildren()) {
                IpcClassification furtherClass = (IpcClassification) furtherClassification;
                futherRawAr.add(furtherClass.toText());
                futherNormAr.add(furtherClass.getTextNormalized());
                futherFacets.addAll(Arrays.asList(furtherClass.toFacet()));
            }
            builder.add("ClassificationIpcFurtherRaw", futherRawAr.build());
            builder.add("ClassificationIpcFurtherNormalized", futherNormAr.build());
            builder.add("ClassificationIpcFurtherFacets", toJsonArray(futherFacets));
        }

        @SuppressWarnings("unchecked")
        Set<UspcClassification> uspcClasses = (Set<UspcClassification>) PatentClassification.filterByType(classes,
                ClassificationType.USPC);
        for (UspcClassification claz : uspcClasses) {
            builder.add("ClassificationUspcMainRaw", claz.toText());
            builder.add("ClassificationUspcMainNormalized", claz.getTextNormalized());
            builder.add("ClassificationUspcMainFacets", toJsonArray(claz.toFacet()));

            JsonArrayBuilder futherRawAr = Json.createArrayBuilder();
            JsonArrayBuilder futherNormAr = Json.createArrayBuilder();
            SortedSet<String> futherFacets = new TreeSet<String>();

            for (PatentClassification furtherClassification : claz.getChildren()) {
                UspcClassification furtherClass = (UspcClassification) furtherClassification;
                futherRawAr.add(furtherClass.toText());
                futherNormAr.add(furtherClass.getTextNormalized());
                futherFacets.addAll(Arrays.asList(furtherClass.toFacet()));
            }
            builder.add("ClassificationUspcFurtherRaw", futherRawAr.build());
            builder.add("ClassificationUspcFurtherNormalized", futherNormAr.build());
            builder.add("ClassificationUspcFurtherFacets", toJsonArray(futherFacets));
        }

        @SuppressWarnings("unchecked")
        Set<CpcClassification> cpcClasses = (Set<CpcClassification>) PatentClassification.filterByType(classes,
                ClassificationType.CPC);
        for (CpcClassification claz : cpcClasses) {
            builder.add("ClassificationCpcMainRaw", claz.toText());
            builder.add("ClassificationCpcMainNormalized", claz.getTextNormalized());
            builder.add("ClassificationCpcMainFacets", toJsonArray(claz.toFacet()));

            JsonArrayBuilder futherRawAr = Json.createArrayBuilder();
            JsonArrayBuilder futherNormAr = Json.createArrayBuilder();
            SortedSet<String> futherFacets = new TreeSet<String>();

            for (PatentClassification furtherClassification : claz.getChildren()) {
                CpcClassification furtherClass = (CpcClassification) furtherClassification;
                futherRawAr.add(furtherClass.toText());
                futherNormAr.add(furtherClass.getTextNormalized());
                futherFacets.addAll(Arrays.asList(furtherClass.toFacet()));
            }
            builder.add("ClassificationIpcFurtherRaw", futherRawAr.build());
            builder.add("ClassificationIpcFurtherNormalized", futherNormAr.build());
            builder.add("ClassificationIpcFurtherFacets", toJsonArray(futherFacets));
        }
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
            case NATIONALITY:
                if (inventor.getNationality() != null) {
                    arBldr.add(inventor.getNationality().toString());
                }
                break;
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

    private String valueOrEmpty(Enum value) {
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    private JsonArray mapDate(DocumentDate date, DateTextType dateType) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();
        if (date != null) {
            arBldr.add(date.getDateText(dateType));
        }
        return arBldr.build();
    }

    private JsonArray mapEntity(Collection<? extends Entity> entities, EntityField entityField) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Entity entity : entities) {
            switch (entityField) {
            case NAME:
                Name name = entity.getName();
                if (name instanceof NamePerson) {
                    arBldr.add(((NamePerson) name).getName());
                } else {
                    arBldr.add(((NameOrg) name).getName());
                }
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
                    NamePerson name3 = (NamePerson) entity.getName();
                    if (name3.getLastName() != null) {
                        arBldr.add(name3.getLastName());
                    }
                }
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
                if (entity.getAddress() != null) {
                    arBldr.add(entity.getAddress().getState());
                }
                break;
            case CITY:
                if (entity.getAddress() != null && entity.getAddress().getCity() != null) {
                    arBldr.add(entity.getAddress().getCity());
                }
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

    private JsonArray mapCitations(Collection<Citation> CitationList, boolean examinerCited, CitationType citeType) {
        JsonArrayBuilder arBldr = Json.createArrayBuilder();

        for (Citation cite : CitationList) {
            if (cite.isExaminerCited() == examinerCited) {
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
        NAME, FIRSTNAME, LASTNAME, ADDRESS, COUNTRY, STATE, CITY
    }

    private enum InventorField {
        NATIONALITY, RESIDENCE
    }

    private String base64(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
    }

}
