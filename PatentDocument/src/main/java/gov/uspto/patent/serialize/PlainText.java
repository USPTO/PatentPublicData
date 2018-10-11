package gov.uspto.patent.serialize;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.uspto.patent.DateTextType;
import gov.uspto.patent.OrgSynonymGenerator;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.CitationType;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.entity.Agent;
import gov.uspto.patent.model.entity.Entity;
import gov.uspto.patent.model.entity.Examiner;
import gov.uspto.patent.model.entity.NamePerson;

/**
 * Serialize Patent as Plain Text
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class PlainText implements DocumentBuilder<Patent> {

    private static String FIELD_FORMAT = "%-30S : ";
    private static String LIST_ITEM_SEPERATOR = " || "; // note comma and semicolons are used in citations.
    private static String MISSING_DATE = "99999999";
    private static String MISSING_VALUE = "null";

    private static Map<String, WriteFieldMethod> METHODS;
    static {
    	METHODS = PlainText.setup();
    }

    private String[] wantedFieldNames;
    
	public PlainText() {
		// empty.
    }

	public PlainText(String... wantedFieldNames) {
    	this.wantedFieldNames = wantedFieldNames;
    }

    @Override
    public void write(Patent patent, Writer writerIn) throws IOException {
    	PrintWriter writer = new PrintWriter(writerIn);
    	if (wantedFieldNames == null || wantedFieldNames.length == 0) {
    		for(String fieldName: METHODS.keySet()) {
    			invokeMethod(patent, writer, fieldName);
    		}
    	} else {
    		for(String fieldName: wantedFieldNames) {
    			invokeMethod(patent, writer, fieldName);
    		}
    	}
    }

    /**
     * Get fields which are available to printout.
     * 
     * @return
     */
    public Set<String> definedFields(){
    	return METHODS.keySet();
    }

    public void invokeMethod(Patent patent, PrintWriter writer, String fieldName) throws IOException {
    	writer.printf(FIELD_FORMAT, fieldName);
		METHODS.get(fieldName.toLowerCase()).invoke(patent, writer);
		writer.write("\n");
    }

	private static Map<String, WriteFieldMethod> setup() {
		Map<String, WriteFieldMethod> methods = new LinkedHashMap<String, WriteFieldMethod>();

		WriteFieldMethod writeDocId = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				writer.write(patent.getDocumentId().toText());
			}
		};
		methods.put("doc_id", writeDocId);
		
		WriteFieldMethod writeDateProduced = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				writer.write(patent.getDateProduced().getDateText(DateTextType.RAW));
			}
		};
		methods.put("doc_production_date", writeDateProduced);

		WriteFieldMethod writeDatePublish = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				writer.write(patent.getDatePublished().getDateText(DateTextType.RAW));
			}
		};
		methods.put("doc_published_date", writeDatePublish);

		WriteFieldMethod writeAppId = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				writer.write(patent.getApplicationId().toTextNoKind());
			}
		};
		methods.put("application_id", writeAppId);
		
		WriteFieldMethod writeAppDate = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				writer.write(patent.getApplicationId().getDate().getDateText(DateTextType.RAW));
			}
		};
		methods.put("application_date", writeAppDate);

		WriteFieldMethod writePriorityIds = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				PlainText.WriteDocIds(patent.getPriorityIds(), writer);
			}
		};
		methods.put("priority_id", writePriorityIds);

		WriteFieldMethod writeRelatedIds = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				PlainText.WriteDocIds(patent.getRelationIds(), writer);
			}
		};
		methods.put("related_id", writeRelatedIds);
		
		WriteFieldMethod writeOthers = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				PlainText.WriteDocIds(patent.getOtherIds(), writer);
			}
		};
		methods.put("other_id", writeOthers);
		
		WriteFieldMethod writeApplicants = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				PlainText.WriteEntity(patent.getApplicants(), writer);
			}
		};
		methods.put("applicant", writeApplicants);

		WriteFieldMethod writeInventors = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				PlainText.WriteEntity(patent.getInventors(), writer);
			}
		};
		methods.put("inventor", writeInventors);

		WriteFieldMethod writeAssignees = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				PlainText.WriteEntity(patent.getAssignee(), writer);
			}
		};
		methods.put("assignee", writeAssignees);

		WriteFieldMethod writeAgent = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				PlainText.WriteEntity(patent.getAgent(), writer);
			}
		};
		methods.put("agent", writeAgent);

		WriteFieldMethod writeExaminer = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				List<Examiner> examiners = patent.getExaminers();
				for(int i=0; i < examiners.size(); i++) {
					Examiner examiner = examiners.get(i);
					writer.write(examiner.getName().getName());
					writer.write(" (");
					writer.write(examiner.getDepartment());
					writer.write(")");
					if (i != examiners.size()-1) {
						writer.write(LIST_ITEM_SEPERATOR);
					}
				}
			}
		};
		methods.put("examiner", writeExaminer);

		WriteFieldMethod writeCitations = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				List<Citation> citations = patent.getCitations();
				for(int i=0; i < citations.size(); i++) {
					Citation cite = citations.get(i);
					
					writer.write(cite.getNum());
					writer.write(" ");
					writer.write(cite.getCitType().name());
					writer.write(" ");
					writer.write(cite.isExaminerCited() ? "examiner" : "applicant");
					writer.write(" ");
	                if (cite.getCitType() == CitationType.NPLCIT) {
	                    NplCitation nplCite = (NplCitation) cite;
	                    if (nplCite.getPatentId() != null) {
	                    	writer.write("patent(");
	                    	writer.write(nplCite.getPatentId().toTextNoKind());
	                    	writer.write(")");
	                    	writer.write(" ");
	                    }
	                    writer.write(nplCite.getCiteText());
	                } else if (cite.getCitType() == CitationType.PATCIT) {
	                    PatCitation patCite = (PatCitation) cite;
	                    writer.write(patCite.getDocumentId().toTextNoKind());
	        			writer.write(" ");
	        			if (patCite.getDocumentId() != null){
	        				writer.write(patCite.getDocumentId().getDate().getDateText(DateTextType.RAW));
	        			} else {
	        				writer.write(MISSING_DATE);
	        			}
	        			writer.write(" ");
	        			if (patCite.getClassification() != null){
	        				PlainText.writeClassifications(patCite.getClassification(), writer);
	        			} else {
	        				writer.write(MISSING_VALUE);
	        			}
	                }

					if (i != citations.size()-1) {
						writer.write(LIST_ITEM_SEPERATOR);
					}
				}
			}
		};
		methods.put("citation", writeCitations);

		WriteFieldMethod writeClasses = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				PlainText.writeClassifications(patent.getClassification(), writer);
			}
		};
		methods.put("classification", writeClasses);
		
		WriteFieldMethod writeTitle = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				writer.write(patent.getTitle());
			}
		};
		methods.put("title", writeTitle);

		WriteFieldMethod writeAbstract = new WriteFieldMethod() { // TODO check abstract field
			public void invoke(Patent patent, Writer writer) throws IOException {
				writer.write(patent.getAbstract().getRawText());
			}
		};
		methods.put("abstract", writeAbstract);

		WriteFieldMethod writeDescription = new WriteFieldMethod() { // TODO check description field
			public void invoke(Patent patent, Writer writer) throws IOException {
				writer.write(patent.getDescription().getAllRawText());
			}
		};
		methods.put("description", writeDescription);
		
		WriteFieldMethod writeClaim = new WriteFieldMethod() {
			public void invoke(Patent patent, Writer writer) throws IOException {
				List<Claim> claims = patent.getClaims(); // TODO check claim field
				for(int i=0; i < claims.size(); i++) {
					Claim claim = claims.get(i);
					writer.write(claim.getId());
					writer.write(" ");
					writer.write(claim.getClaimType().toString());
					writer.write(" ");
					writer.write(claim.getRawText());
					if (i != claims.size()-1) {
						writer.write(LIST_ITEM_SEPERATOR);
					}
				}
			}
		};
		methods.put("claim", writeClaim);
		
		return methods;
	}

	public static Boolean isDefinedField(String fieldName) {
		return METHODS.containsKey(fieldName.toLowerCase());
	}

	public static void writeClassifications(Collection<PatentClassification> classes, Writer writer) throws IOException {
		int count = 0;
		for(PatentClassification clazz: classes) {
			writer.write(clazz.getType().name());
			writer.write("/");
			writer.write(clazz.getTextNormalized());
			if (count != classes.size()-1) {
				writer.write(","); // not LIST_ITEM_SEPERATOR since can be nested within another list.
			}
			count++;			
		}
	}

	public static void WriteDocIds(Set<DocumentId> docIds, Writer writer) throws IOException {
		int count = 0;
		for(DocumentId docId: docIds) {
			writer.write(docId.getType().toString());
			writer.write(" ");
			writer.write(docId.toTextNoKind());
			writer.write(" ");
			if (docId.getDate() != null){
				writer.write(docId.getDate().getDateText(DateTextType.RAW));
			} else {
				writer.write(MISSING_DATE); // date is missing.
			}
			if (count != docIds.size()-1) {
				writer.write(LIST_ITEM_SEPERATOR);
			}
			count++;
		}
	}

	public static <T extends Entity> void WriteEntity(List<T> entities, Writer writer) throws IOException {
		for(int i=0; i < entities.size(); i++) {
			Entity entity = entities.get(i);
			
			if (entity instanceof Agent) {
				if (entity.getName() instanceof NamePerson) {
					writer.write("PERSON/");
				} else {
					writer.write("ORG/");
					new OrgSynonymGenerator().computeSynonyms(entity);
				}
				writer.write(((Agent) entity).getRepType().toString());
				writer.write(" ");
			}
			writer.write(entity.getName().getName());

			List<String> synonyms = (List<String>) entity.getName().getSynonyms();
			if (!synonyms.isEmpty()) {
				writer.write(" {");
				writer.write(synonyms.get(0));
				writer.write("}");
			}

			writer.write(" (");
			if (entity.getAddress() != null) {
				writer.write(entity.getAddress().toText());
			} else {
				writer.write(MISSING_VALUE);
			}
			writer.write(")");

			if (i != entities.size()-1) {
				writer.write(LIST_ITEM_SEPERATOR);
			}
		}
	}

    interface WriteFieldMethod {
    	void invoke(Patent patent, Writer writer) throws IOException;
    }
}
