package gov.uspto.patent.serialize.solr;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import gov.uspto.common.text.StringCaseUtil;
import gov.uspto.patent.OrgSynonymGenerator;
import gov.uspto.patent.model.Citation;
import gov.uspto.patent.model.Citation.CitedBy;
import gov.uspto.patent.model.CitationType;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.CountryCode;
import gov.uspto.patent.model.DescSection;
import gov.uspto.patent.model.DescriptionSection;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.NplCitation;
import gov.uspto.patent.model.PatCitation;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.model.classification.CpcClassification;
import gov.uspto.patent.model.classification.IpcClassification;
import gov.uspto.patent.model.classification.LocarnoClassification;
import gov.uspto.patent.model.classification.PatentClassification;
import gov.uspto.patent.model.classification.UspcClassification;
import gov.uspto.patent.model.entity.Entity;
import gov.uspto.patent.model.entity.NameOrg;
import gov.uspto.patent.model.entity.NamePerson;
import gov.uspto.patent.serialize.DocumentBuilder;

/**
 * Output a Patent as a Stream in Solr's JSON format.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class JsonMapperSolr implements DocumentBuilder<Patent>, Closeable {

	private final SolrJson json;

	public JsonMapperSolr(boolean pretty, boolean useDynamicFieldEndings) {
		json = new SolrJson(pretty, useDynamicFieldEndings);
	}

	@Override
	public void write(Patent patent, Writer writer) throws IOException {
		json.open(writer);
		json.startDocument();
		output(patent, writer);
		json.endDocument();
		json.close();
	}

	private void output(Patent patent, Writer writer) throws IOException {
		json.addStringField("id", patent.getDocumentId().toText());
		json.addStringField("id_variation", getDocIdTokens(patent.getDocumentId()));

		json.addStringField("corpus", patent.getPatentCorpus().toString());

		json.addStringField("type", patent.getPatentType().toString());

		json.addStringField("application_id", getDocIdTokens(patent.getApplicationId()));

		json.addStringField("related_id", getDocIds(patent.getRelationIds()));
		json.addStringField("priority_id", getDocIds(patent.getPriorityIds()));

		List<Date> priorityDates = getDocDates(patent.getPriorityIds());
		json.addDateField("priority_date", priorityDates.isEmpty() ? null : priorityDates.get(0));

		json.addDateField("filing_date", patent.getApplicationId().getDate().getDate());
		json.addDateField("production_date", patent.getDateProduced().getDate());
		json.addDateField("published_date", patent.getDatePublished().getDate());

		json.addStringField("agent_name", getEntityNames(patent.getAgent()));
		json.addStringField("agent_name_abrev" , getEntityNamesAbrev(patent.getAgent()).keySet());
		json.addStringField("agent_name_initials" , getEntityNamesAbrev(patent.getAgent()).values());

		json.addStringField("assignee_name", getEntityNames(patent.getAssignee()));
		json.addStringField("assignee_name_abbrev" , getEntityNamesAbrev(patent.getAssignee()).keySet());
		json.addStringField("assignee_name_initials" , getEntityNamesAbrev(patent.getAssignee()).values());
		json.addStringField("assignee_address", getEntityAddress(patent.getAssignee()));
	
		json.addStringField("applicant_name", getEntityNames(patent.getApplicants()));
		json.addStringField("applicant_name_abrev" , getEntityNamesAbrev(patent.getApplicants()).keySet());
		json.addStringField("applicant_name_initials" , getEntityNamesAbrev(patent.getApplicants()).values());
		json.addStringField("applicant_address", getEntityAddress(patent.getApplicants()));

		json.addStringField("inventor_name", getEntityNames(patent.getInventors()));
		json.addStringField("inventor_name_abrev" , getEntityNamesAbrev(patent.getInventors()).keySet());
		json.addStringField("inventor_name_initials" , getEntityNamesAbrev(patent.getInventors()).values());
		json.addStringField("inventors_address", getEntityAddress(patent.getInventors()));

		json.addStringField("examiner_name", getEntityNames(patent.getExaminers()));
		json.addStringField("examiner_name_abrev" , getEntityNamesAbrev(patent.getExaminers()).keySet());
		json.addStringField("examiner_name_initials" , getEntityNamesAbrev(patent.getExaminers()).values());

		List<String> examiner_dep = patent.getExaminers().stream().map(e -> e.getDepartment()).distinct()
				.collect(Collectors.toList());
		json.addStringField("art_unit", examiner_dep.get(0));

		/*
		 * Citations
		 */
		Map<CitationType, Map<CitedBy, List<Citation>>> citationMap = getCitationMap(patent.getCitations());

		List<String> examinerPatCite = null;
		if (citationMap.containsKey(CitationType.PATCIT) && citationMap.get(CitationType.PATCIT).containsKey(CitedBy.EXAMINER)) {
			examinerPatCite = citationMap.get(CitationType.PATCIT).get(CitedBy.EXAMINER).stream()
				.map(o -> (PatCitation) o).map(p -> p.getDocumentId().toTextNoKind()).sorted().collect(Collectors.toList());
		}
		json.addStringField("citation_examiner_pat", examinerPatCite == null ? Collections.emptyList() : examinerPatCite);

		List<String> examinerNplCite = null;
		if (citationMap.containsKey(CitationType.NPLCIT) && citationMap.get(CitationType.NPLCIT).containsKey(CitedBy.EXAMINER)) {
			examinerNplCite = citationMap.get(CitationType.NPLCIT).get(CitedBy.EXAMINER).stream()
				.map(o -> (NplCitation) o).map(p -> p.getCiteText()).collect(Collectors.toList());
		}
		json.addStringField("citation_examiner_npl", examinerNplCite == null ? Collections.emptyList() : examinerNplCite);

		List<String> applicantPatCite = null;
		if (citationMap.containsKey(CitationType.PATCIT) && citationMap.get(CitationType.PATCIT).containsKey(CitedBy.APPLICANT)) {
			applicantPatCite = citationMap.get(CitationType.PATCIT).get(CitedBy.APPLICANT).stream()
				.map(o -> (PatCitation) o).map(p -> p.getDocumentId().toTextNoKind()).collect(Collectors.toList());
		}
		json.addStringField("citation_applicant_pat", applicantPatCite == null ? Collections.emptyList() : applicantPatCite);

		List<String> applicantNplCite = null;
		if (citationMap.containsKey(CitationType.NPLCIT) && citationMap.get(CitationType.NPLCIT).containsKey(CitedBy.APPLICANT)) {
			applicantNplCite = citationMap.get(CitationType.NPLCIT).get(CitedBy.APPLICANT).stream()
				.map(o -> (NplCitation) o).map(p -> p.getCiteText()).collect(Collectors.toList());
		}
		json.addStringField("citation_applicant_npl", applicantNplCite == null ? Collections.emptyList() : applicantNplCite);

		/*
		 * Classifications
		 */
		List<String> uspcMain = getClassifications(patent.getClassification(), UspcClassification.class,
				cl -> cl.isInventive());
		json.addStringField("uspc_main", uspcMain);

		List<String> uspcFurther = getClassifications(patent.getClassification(), UspcClassification.class,
				cl -> !cl.isInventive());
		json.addStringField("uspc_further", uspcFurther);

		List<String> cpcInventive = getClassifications(patent.getClassification(), CpcClassification.class,
				cl -> cl.isInventive());
		json.addStringField("cpc_inventive", cpcInventive);

		List<String> cpcAdditional = getClassifications(patent.getClassification(), CpcClassification.class,
				cl -> !cl.isInventive());
		json.addStringField("cpc_additional", cpcAdditional);

		List<String> ipcInventive = getClassifications(patent.getClassification(), IpcClassification.class,
				cl -> cl.isInventive());
		json.addStringField("ipc_inventive", ipcInventive);

		List<String> ipcAdditional = getClassifications(patent.getClassification(), IpcClassification.class,
				cl -> !cl.isInventive());
		json.addStringField("ipc_additional", ipcAdditional);

		Set<String> locarno = getClassifications(patent.getClassification(), LocarnoClassification.class);
		json.addStringField("locarno", locarno);

		json.addStringField("cpc_facet", getClassificationFacets(patent.getClassification(), CpcClassification.class));
		json.addStringField("uspc_facet",
				getClassificationFacets(patent.getClassification(), UspcClassification.class));

		/*
		 * Specification Fields
		 */
		json.addStringField("title_text", StringCaseUtil.toTitleCase(patent.getTitle()));

		json.addTextField("abstract_text", removeNewline(patent.getAbstract().getSimpleHtml()));

		DescriptionSection relAppDesc = patent.getDescription().getSection(DescSection.REL_APP_DESC);
		json.addTextField("desc_relapp_text", relAppDesc != null ? removeNewline(relAppDesc.getSimpleHtml()) : null);

		DescriptionSection briefSummary = patent.getDescription().getSection(DescSection.BRIEF_SUMMARY);
		json.addTextField("desc_summary_text", briefSummary != null ? removeNewline(briefSummary.getSimpleHtml()) : null);

		DescriptionSection drawingDesc = patent.getDescription().getSection(DescSection.DRAWING_DESC);
		json.addTextField("desc_draw_text", drawingDesc != null ? removeNewline(drawingDesc.getSimpleHtml()) : null);

		DescriptionSection detailedDesc = patent.getDescription().getSection(DescSection.DETAILED_DESC);
		json.addTextField("desc_detail_text", detailedDesc != null ? removeNewline(detailedDesc.getSimpleHtml()) : null);

		List<String> claims = patent.getClaims().stream().map(Claim::getSimpleHtml).map(e -> e.replaceAll("\n", ""))
				.collect(Collectors.toList());
		json.addTextField("claim_text", claims);
	}

	private List<String> getDocIds(Collection<DocumentId> docIds) {
		return docIds.stream().map(d -> d.toTextNoKind()).collect(Collectors.toList());
	}

	private List<Date> getDocDates(Collection<DocumentId> docIds) {
		return docIds.stream().map(d -> d.getDate().getDate()).sorted().collect(Collectors.toList());
	}

	/**
	 * Entities[Agents,Applicants,Inventor,Assignee,Examiner]
	 * 
	 * @param entities
	 * @return
	 */
	private <T extends Entity> List<String> getEntityNames(Collection<T> entities) {
		return entities.stream().map(e -> e.getName().getNameNormalizeCase()).collect(Collectors.toList());
	}

	private <T extends Entity> Map<String, String> getEntityNamesAbrev(Collection<T> entities) {
		Map<String, String> abbrevs = new LinkedHashMap<String, String>();
		for(Entity entity: entities) {
			if (entity.getName() instanceof NamePerson) {
				NamePerson perName = (NamePerson) entity.getName();
				abbrevs.put(perName.getAbbreviatedName(), perName.getInitials());				
			} else {
				NameOrg orgName = (NameOrg) entity.getName();
				new OrgSynonymGenerator().computeSynonyms(entity);
				abbrevs.put(orgName.getShortestSynonym(), orgName.getInitials());
			}
		}
		return abbrevs;
	}

	private <T extends Entity> List<String> getEntityAddress(Collection<T> entities) {
		return entities.stream().map(e -> e.getAddress().getTokenSet()).flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	private <T extends PatentClassification> Set<String> getClassifications(Collection<PatentClassification> classes,
			Class<T> wantedClass) {
		return classes.stream().filter(wantedClass::isInstance).map(PatentClassification::getTextNormalized)
				.collect(Collectors.toSet());
	}

	private <T extends PatentClassification> Set<String> getClassificationFacets(
			Collection<PatentClassification> classes, Class<T> wantedClass) {
		return classes.stream().filter(wantedClass::isInstance).map(p -> Arrays.asList(p.toFacet()))
				.flatMap(Collection::stream).collect(Collectors.toSet());
	}

	private <T extends PatentClassification> List<String> getClassifications(Collection<PatentClassification> classes,
			Class<T> wantedClass, Predicate<? super PatentClassification> predicate) {
		return classes.stream().filter(wantedClass::isInstance).filter(predicate)
				.map(PatentClassification::getTextNormalized).collect(Collectors.toList());
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

	private Map<CitationType, Map<CitedBy, List<Citation>>> getCitationMap(Collection<Citation> CitationList) {
		return CitationList.stream().collect(Collectors.groupingBy(Citation::getCitType,
				Collectors.groupingBy(Citation::getCitedBy, TreeMap::new, Collectors.toList())));
	}

	private String removeNewline(String line) {
		if (line == null) {
			return "";
		}
		return line.replaceAll("\n", "").replaceAll("\t", " ");
	}

	@Override
	public void close() throws IOException {
		json.close();
	}

}
