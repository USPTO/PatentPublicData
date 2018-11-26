package gov.uspto.patent.serialize;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import gov.uspto.patent.DateTextType;
import gov.uspto.patent.OrgSynonymGenerator;
import gov.uspto.patent.model.Abstract;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.CitationType;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.Description;
import gov.uspto.patent.model.DescriptionSection;
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

    private JsonGenerator jGenerator;
	private JsonFactory jfactory = new JsonFactory();
	private final boolean pretty;

    public JsonMapperStream(boolean pretty) {
        this.pretty = pretty;
    }

    @Override
    public void write(Patent patent, Writer writer) throws IOException {
    	jGenerator = jfactory.createGenerator(writer);
    	if (pretty) {
    		jGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
    	}

    	output(patent, writer);
    }

    private void output(Patent patent, Writer writer) throws IOException {
    	jGenerator.writeStartObject(); // root.

    	jGenerator.writeStringField("patentCorpus", patent.getPatentCorpus().toString());
    	jGenerator.writeStringField("patentType", patent.getPatentType().toString());
    	writeDateObj("productionDate", patent.getDateProduced());
    	writeDateObj("publishedDate", patent.getDatePublished());

    	jGenerator.writeStringField("documentId", patent.getDocumentId() != null ? patent.getDocumentId().toText() : ""); // Patent ID or Public Application ID.
    	writeDateObj("documentDate", patent.getDocumentDate());

    	jGenerator.writeStringField("applicationId", patent.getApplicationId() != null ? patent.getApplicationId().toText() : "");
    	writeDateObj("applicationDate", patent.getApplicationDate());
    	
    	writeDocArray("priorityIds", patent.getPriorityIds(), true);

    	writeDocArray("relatedIds", patent.getRelationIds(), false);

        // OtherIds contain [documentId, applicationId, relatedIds]
    	writeDocArray("otherIds", patent.getOtherIds(), false);
   	
    	writeEntity("agent", patent.getAgent());
    	writeEntity("applicant", patent.getApplicants());

    	writeEntity("inventors", patent.getInventors());
    	writeEntity("assignees", patent.getAssignee());
    	writeEntity("examiners", patent.getExaminers());

    	jGenerator.writeStringField("title", valueOrEmpty(patent.getTitle()));

    	writeAbstract(patent.getAbstract());
    	
    	writeDescription(patent.getDescription());
        
        writeClaims(patent.getClaims());

        writeCitations(patent.getCitations());

        writeClassifications(patent.getClassification());

    	jGenerator.writeEndObject(); // root.

    	jGenerator.flush();
    }

    private void writeArray(String fieldName, Collection<String> strings) throws IOException {
    	jGenerator.writeFieldName(fieldName);
    	jGenerator.writeStartArray();
        if (strings != null) {
            for (String tok : strings) {
            	jGenerator.writeString(valueOrEmpty(tok));
            }
        }
        jGenerator.writeEndArray();
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

    private void writeDocArray(String fieldName, Collection<DocumentId> docIds, boolean withDate) throws IOException {
    	if (docIds.isEmpty()) {
    		return;
    	}
  
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
     * @param entities
     * @throws IOException 
     */
    private <T extends Entity> void writeEntity(String fieldName, Collection<T> entities) throws IOException {
    	jGenerator.writeFieldName(fieldName);
    	jGenerator.writeStartArray();

    	for(Entity entity: entities) {
            jGenerator.writeStartObject();

        	jGenerator.writeFieldName("name");
        	writeName(entity);

        	if (entity instanceof Inventor) {
        		Inventor inventor = (Inventor) entity;
            	jGenerator.writeStringField("sequence", inventor.getSequence());
        	}
        	else if (entity instanceof Assignee) {
        		Assignee assignee = (Assignee) entity;
            	jGenerator.writeStringField("role", valueOrEmpty(assignee.getRole()));
            	jGenerator.writeStringField("roleDefinition", valueOrEmpty(assignee.getRoleDesc()));
        	} 
        	else if (entity instanceof Examiner) {
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

    	//jGenerator.writeFieldName("name");
        jGenerator.writeStartObject();

        if (name instanceof NamePerson) {
            NamePerson perName = (NamePerson) name;
        	jGenerator.writeStringField("type", "person");
        	jGenerator.writeStringField("raw", valueOrEmpty(perName.getName()));
        	jGenerator.writeStringField("prefix", valueOrEmpty(perName.getPrefix()));
        	jGenerator.writeStringField("firstName", valueOrEmpty(perName.getFirstName()));
        	jGenerator.writeStringField("middleName", valueOrEmpty(perName.getMiddleName()));
        	jGenerator.writeStringField("lastName", valueOrEmpty(perName.getLastName()));
        	jGenerator.writeStringField("suffix", valueOrEmpty(perName.getSuffix()));
        	jGenerator.writeStringField("abbreviated", valueOrEmpty(perName.getAbbreviatedName()));
        	writeArray("synonyms", perName.getSynonyms());
        } else {
            NameOrg orgName = (NameOrg) name;
        	jGenerator.writeStringField("type", "org");
        	jGenerator.writeStringField("raw", valueOrEmpty(orgName.getName()));
        	jGenerator.writeStringField("prefix", valueOrEmpty(orgName.getPrefix()));
        	jGenerator.writeStringField("suffix", valueOrEmpty(orgName.getSuffix()));

            new OrgSynonymGenerator().computeSynonyms(entity);
        	writeArray("synonyms", orgName.getSynonyms());
        }
        
    	jGenerator.writeEndObject();
    }

    private void writeAddress(Entity entity) throws IOException {
    	Address address = entity.getAddress();
        if (address != null) {
        	jGenerator.writeFieldName("Address");
            jGenerator.writeStartObject();
        	//jGenerator.writeStringField("street", valueOrEmpty(address.getStreet()));
        	jGenerator.writeStringField("city", valueOrEmpty(address.getCity()));
        	jGenerator.writeStringField("state", valueOrEmpty(address.getState()));
        	//jGenerator.writeStringField("zipCode", valueOrEmpty(address.getZipCode()));
        	jGenerator.writeStringField("country", valueOrEmpty(address.getCountry()));
        	//jGenerator.writeStringField("email", valueOrEmpty(address.getEmail()));
        	//jGenerator.writeStringField("fax", valueOrEmpty(address.getFaxNumber()));
        	//jGenerator.writeStringField("phone", valueOrEmpty(address.getPhoneNumber()));
        	//writeArray("tokens", address.getTokenSet());
        	jGenerator.writeEndObject();
        }
    }

    private void writeAbstract(Abstract abstractObj) throws IOException {
    	jGenerator.writeFieldName("Abstract");
        jGenerator.writeStartObject();
 
        if (abstractObj != null){
        	jGenerator.writeStringField("raw", abstractObj.getRawText());
        	jGenerator.writeStringField("normalized", abstractObj.getSimpleHtml());
        	jGenerator.writeStringField("plain", abstractObj.getPlainText());
        }
        else {
        	jGenerator.writeStringField("raw", "");
        	jGenerator.writeStringField("normalized", "");
        	jGenerator.writeStringField("plain", "");       
        }

    	jGenerator.writeEndObject();
    }

    private void writeClaims(Collection<Claim> claimList) throws IOException {
    	jGenerator.writeFieldName("claims");
    	jGenerator.writeStartArray();

        for (Claim claim : claimList) {
            jGenerator.writeStartObject(); // start claim
        	jGenerator.writeStringField("id", claim.getId());
        	jGenerator.writeStringField("type", claim.getClaimType().toString());
        	jGenerator.writeStringField("raw",  claim.getRawText());
        	jGenerator.writeStringField("raw",  claim.getSimpleHtml());
        	jGenerator.writeStringField("plain",  claim.getPlainText());
           
        	jGenerator.writeFieldName("claimTree");
            jGenerator.writeStartObject();
            writeArray("parentIds", claim.getDependentIds());
        	jGenerator.writeNumberField("parentCount",  claim.getDependentIds() != null ? claim.getDependentIds().size() : 0);
        	jGenerator.writeNumberField("childCount",  claim.getChildClaims() != null ? claim.getChildClaims().size() : 0);
        	jGenerator.writeNumberField("claimTreelevel",  claim.getClaimTreeLevel());
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
    
    private void writeCitations(Collection<Citation> CitationList) throws IOException {
    	jGenerator.writeFieldName("citations");
    	jGenerator.writeStartArray();

        for (Citation cite : CitationList) {
            jGenerator.writeStartObject(); // start cited            
        	jGenerator.writeStringField("num", cite.getNum());

            if (cite.getCitType() == CitationType.NPLCIT) {
                NplCitation nplCite = (NplCitation) cite;

            	jGenerator.writeStringField("type", "NPL");
            	jGenerator.writeStringField("citedBy", nplCite.getCitedBy().toString());
            	jGenerator.writeStringField("text", nplCite.getCiteText());

            	jGenerator.writeFieldName("extracted");
                jGenerator.writeStartObject();
            	jGenerator.writeStringField("quotedText", nplCite.getQuotedText());
            	jGenerator.writeStringField("patentId", nplCite.getPatentId() != null ? nplCite.getPatentId().toText() : "");
            	jGenerator.writeEndObject(); // end extracted.

            } else if (cite.getCitType() == CitationType.PATCIT) {
                PatCitation patCite = (PatCitation) cite;
            	jGenerator.writeStringField("type", "PATENT");
            	jGenerator.writeStringField("citedBy", patCite.getCitedBy().toString());
            	jGenerator.writeStringField("raw", patCite.getDocumentId().getRawText());
            	jGenerator.writeStringField("text", patCite.getDocumentId().toTextNoKind());
            	jGenerator.writeFieldName("classification");
            	jGenerator.writeStartObject();
	                writeSingleClassificationType(patCite.getClassification(), ClassificationType.USPC);
	                writeSingleClassificationType(patCite.getClassification(), ClassificationType.CPC);
	                writeSingleClassificationType(patCite.getClassification(), ClassificationType.IPC);
                jGenerator.writeEndObject();
            }

        	jGenerator.writeEndObject(); // end cite
        }

    	jGenerator.writeEndArray(); // end citation array
    }

    private void writeDescription(Description patentDescription) throws IOException {
    	jGenerator.writeFieldName("description");
    	jGenerator.writeStartObject();

    	jGenerator.writeStringField("full_raw", patentDescription.getAllRawText());

    	for(DescriptionSection section: patentDescription.getSections()) {
            if (section != null) {
            	jGenerator.writeFieldName(section.getSection().toString());
            	jGenerator.writeStartObject();
            	jGenerator.writeStringField("raw", section.getRawText());
            	jGenerator.writeStringField("normalized", section.getSimpleHtml());
            	jGenerator.writeStringField("plain", section.getPlainText());
                jGenerator.writeEndObject();
            }
    	}

        jGenerator.writeEndObject();
    }

	@Override
	public void close() throws IOException {
		if (jGenerator != null && !jGenerator.isClosed()) {
			jGenerator.close();
		}
	}

    private void writeClassifications(Collection<PatentClassification> classes) throws IOException {
    	jGenerator.writeFieldName("classification");
    	jGenerator.writeStartObject();

    	writeUspcClassification(classes);
    	writeCpcClassification(classes);
        writeIpcClassification(classes);
        writeSingleClassificationType(classes, ClassificationType.LOCARNO);

        jGenerator.writeEndObject();
    }

    private <T extends PatentClassification> void writeIpcClassification(Collection<PatentClassification> classes) throws IOException {

        Map<String, List<IpcClassification>> retClasses = IpcClassification.filterCpc(classes);

    	jGenerator.writeFieldName("ipc_inventive");
    	jGenerator.writeStartArray();
    	if (retClasses.containsKey("inventive")) {
	    	for (IpcClassification ipc : retClasses.get("inventive")) {
	        	jGenerator.writeStartObject();
	        	jGenerator.writeStringField("type", "main");
	        	jGenerator.writeStringField("raw", ipc.toText());
	        	jGenerator.writeStringField("normalized", ipc.getTextNormalized());
	        	writeArray("facets", ipc.toFacet());
	            jGenerator.writeEndObject();
	
	            for (PatentClassification furtherClass : ipc.getChildren()) {
	            	jGenerator.writeStartObject();
	            	jGenerator.writeStringField("type", "further");
	            	jGenerator.writeStringField("raw", furtherClass.toText());
	            	jGenerator.writeStringField("normalized", furtherClass.getTextNormalized());
	            	writeArray("facets", furtherClass.toFacet());
	                jGenerator.writeEndObject();
	            }
	    	}
    	}
    	jGenerator.writeEndArray();
 
    	jGenerator.writeFieldName("ipc_additional");
    	jGenerator.writeStartArray();
    	if (retClasses.containsKey("additional")) {
	    	for (IpcClassification ipc : retClasses.get("additional")) {
	        	jGenerator.writeStartObject();
	        	jGenerator.writeStringField("type", "main");
	        	jGenerator.writeStringField("raw", ipc.toText());
	        	jGenerator.writeStringField("normalized", ipc.getTextNormalized());
	        	writeArray("facets", ipc.toFacet());
	            jGenerator.writeEndObject();
	
	            for (PatentClassification furtherClass : ipc.getChildren()) {
	            	jGenerator.writeStartObject();
	            	jGenerator.writeStringField("type", "further");
	            	jGenerator.writeStringField("raw", furtherClass.toText());
	            	jGenerator.writeStringField("normalized", furtherClass.getTextNormalized());
	            	writeArray("facets", furtherClass.toFacet());
	                jGenerator.writeEndObject();
	            }
	    	}
    	}
    	jGenerator.writeEndArray();
    }
    
    private <T extends PatentClassification> void writeCpcClassification(Collection<PatentClassification> classes) throws IOException {

        Map<String, List<CpcClassification>> retClasses = CpcClassification.filterCpc(classes);

    	jGenerator.writeFieldName("cpc_inventive");
    	jGenerator.writeStartArray();
    	if (retClasses.containsKey("inventive")) {
	    	for (CpcClassification cpci : retClasses.get("inventive")) {
	        	jGenerator.writeStartObject();
	        	jGenerator.writeStringField("type", "main");
	        	jGenerator.writeStringField("raw", cpci.toText());
	        	jGenerator.writeStringField("normalized", cpci.getTextNormalized());
	        	writeArray("facets", cpci.toFacet());
	            jGenerator.writeEndObject();
	
	            for (PatentClassification furtherClass : cpci.getChildren()) {
	            	jGenerator.writeStartObject();
	            	jGenerator.writeStringField("type", "further");
	            	jGenerator.writeStringField("raw", furtherClass.toText());
	            	jGenerator.writeStringField("normalized", furtherClass.getTextNormalized());
	            	writeArray("facets", furtherClass.toFacet());
	                jGenerator.writeEndObject();
	            }
	    	}
    	}
    	jGenerator.writeEndArray();
 
    	jGenerator.writeFieldName("cpc_additional");
    	jGenerator.writeStartArray();
    	if (retClasses.containsKey("additional")) {
	    	for (CpcClassification cpci : retClasses.get("additional")) {
	        	jGenerator.writeStartObject();
	        	jGenerator.writeStringField("type", "main");
	        	jGenerator.writeStringField("raw", cpci.toText());
	        	jGenerator.writeStringField("normalized", cpci.getTextNormalized());
	        	writeArray("facets", cpci.toFacet());
	            jGenerator.writeEndObject();
	
	            for (PatentClassification furtherClass : cpci.getChildren()) {
	            	jGenerator.writeStartObject();
	            	jGenerator.writeStringField("type", "further");
	            	jGenerator.writeStringField("raw", furtherClass.toText());
	            	jGenerator.writeStringField("normalized", furtherClass.getTextNormalized());
	            	writeArray("facets", furtherClass.toFacet());
	                jGenerator.writeEndObject();
	            }
	    	}
    	}
    	jGenerator.writeEndArray();
    }
    
    private <T extends PatentClassification> void writeUspcClassification(Collection<PatentClassification> classes) throws IOException {
    	jGenerator.writeFieldName("uspc");
    	jGenerator.writeStartArray();

        Set<UspcClassification> classesOfType = PatentClassification.filterByType(classes, ClassificationType.USPC);

        for (PatentClassification mainClass : classesOfType) {
        	jGenerator.writeStartObject();
        	jGenerator.writeStringField("type", "main");
        	jGenerator.writeStringField("raw", mainClass.toText());
        	jGenerator.writeStringField("normalized", mainClass.getTextNormalized());
        	writeArray("facets", mainClass.toFacet());
            jGenerator.writeEndObject();

            for (PatentClassification furtherClass : mainClass.getChildren()) {
            	jGenerator.writeStartObject();
            	jGenerator.writeStringField("type", "further");
            	jGenerator.writeStringField("raw", furtherClass.toText());
            	jGenerator.writeStringField("normalized", furtherClass.getTextNormalized());
            	writeArray("facets", furtherClass.toFacet());
                jGenerator.writeEndObject();
            }
        }

    	jGenerator.writeEndArray();
    }
    
    private void writeSingleClassificationType(Collection<PatentClassification> classes, ClassificationType classType) throws IOException {
    	jGenerator.writeFieldName(classType.name().toLowerCase());
    	jGenerator.writeStartArray();

        Set<PatentClassification> classesOfType = (Set<PatentClassification>) PatentClassification.filterByType(classes, classType.getJavaClass());

        for (PatentClassification mainClass : classesOfType) {
        	jGenerator.writeStartObject();
        	jGenerator.writeStringField("type", "main");
        	jGenerator.writeStringField("raw", mainClass.toText());
        	jGenerator.writeStringField("normalized", mainClass.getTextNormalized());
        	writeArray("facets", mainClass.toFacet());
            jGenerator.writeEndObject();

            for (PatentClassification furtherClass : mainClass.getChildren()) {
            	jGenerator.writeStartObject();
            	jGenerator.writeStringField("type", "further");
            	jGenerator.writeStringField("raw", furtherClass.toText());
            	jGenerator.writeStringField("normalized", furtherClass.getTextNormalized());
            	writeArray("facets", furtherClass.toFacet());
                jGenerator.writeEndObject();
            }
        }

    	jGenerator.writeEndArray();
    }

}
