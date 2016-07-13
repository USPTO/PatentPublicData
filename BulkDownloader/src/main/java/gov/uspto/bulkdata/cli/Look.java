package gov.uspto.bulkdata.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import gov.uspto.bulkdata.DumpFileAps;
import gov.uspto.bulkdata.DumpFileXml;
import gov.uspto.bulkdata.DumpReader;
import gov.uspto.bulkdata.zip.FileFilter;
import gov.uspto.bulkdata.zip.SuffixRule;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.DocumentId;
import gov.uspto.patent.model.Patent;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Look is a CLI tool to a view a single patent document from Bulk Patent XML.
 *
 * View contents of listed fields
 * 	--source="download/ipa150101.zip" --limit 5 --skip 5 --fields=id,title,family
 * 
 * Dump a single Patent XML Document by location in zipfile; the 3rd document:
 * --source="download/ipa150305.zip" --num=3 --fields=xml --out=download/patent.xml
 * 
 * Dump a single Patent XML Document by ID (note it may be slow as it parse each document to check its id):
 * --source="download/ipa150305.zip" --id=3 --fields=xml --out=download/patent.xml
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class Look {

	private String xmlDocStr;

	public void look(DumpReader dumpReader, int limit, Writer writer, String[] fields)
			throws PatentReaderException, IOException {

		for (int i = 1; dumpReader.hasNext() && i <= limit; i++) {
			System.out.println(dumpReader.getCurrentRecCount() + 1 + " --------------------------");

			try {
				xmlDocStr = dumpReader.next();
			} catch (NoSuchElementException e) {
				break;
			}

			//LOGGER.info(xmlDocStr);

			/*
			 Match matcher = new MatchValueRegex("//classification-national/main-classification", "^166.+");
			 if (matcher.match(xmlDocStr){
				LOGGER.info("found matching document.");
			 }
			*/

			if (fields.length == 1 && "xml".equalsIgnoreCase(fields[0])) {
				show(null, fields, writer);
			} else {
				try (PatentReader patentReader = new PatentReader(xmlDocStr, dumpReader.getPatentType())) {
					Patent patent = patentReader.read();
					show(patent, fields, writer);
				}
			}
		}

		dumpReader.close();
	}

	public void look(DumpReader dumpReader, String docId,  Writer writer, String[] fields)
			throws PatentReaderException, IOException {

		while (dumpReader.hasNext()) {
			try {
				xmlDocStr = dumpReader.next();
			} catch (NoSuchElementException e) {
				break;
			}

			try (PatentReader patentReader = new PatentReader(xmlDocStr, dumpReader.getPatentType())) {
				Patent patent = patentReader.read();
				if (patent.getDocumentId().toText().equals(docId)) {
					show(patent, fields, writer);
					break;
				}
			}
		}

		dumpReader.close();
	}

	/**
	 * STDOUT requested Patent fields.
	 * 
	 * @param patent
	 * @param fields
	 * @throws IOException 
	 * 
	 */
	private void show(Patent patent, String[] fields, Writer writer) throws IOException {
		for (String field : fields) {
			switch (field) {
			case "xml":
				writer.write("Patent XML:\n");
				//String prettyXml = prettyFormatXml(xmlDocStr);
				//writer.write(prettyXml + "\n");
				writer.write(xmlDocStr);
				writer.flush();
				//System.out.println(prettyXml);
				break;
			case "id":
				writer.write("ID:\t" + patent.getDocumentId() + "\n");
				writer.flush();
				//System.out.println("ID:\t" + patent.getDocumentId());
				break;
			case "title":
				writer.write("TITLE:\t" + patent.getTitle() + "\n");
				writer.flush();
				//System.out.println("TITLE:\t" + patent.getTitle());
				break;
			case "abstract":
				writer.write("ABSTRACT:\t" + patent.getAbstract() + "\n");
				writer.flush();
				//System.out.println("ABSTRACT:\t" + patent.getAbstract());
				break;
			case "description":
				writer.write("DESCRIPTION:\t" + patent.getDescription().getAllProcessedText() + "\n");
				writer.flush();
				//System.out.println("DESCRIPTION:\t" + patent.getDescription().getPlainText());
				break;
			case "citations":
				writer.write("CITATIONS:\t" + patent.getCitations() + "\n");
				writer.flush();
				//System.out.println("CITATIONS:\t" + patent.getCitations());
				break;
			case "claims":
				writer.write("CLAIMS:\t" + patent.getClaims() + "\n");
				writer.flush();
				//System.out.println("CLAIMS:\t" + patent.getClaims());
				break;
			case "assignee":
				writer.write("ASSIGNEE:\t" + patent.getAssignee() + "\n");
				writer.flush();
				break;
			case "inventor":
				writer.write("INVENTORS:\t" + patent.getInventors() + "\n");
				writer.flush();
				break;
			case "classification":
				writer.write("CLASSIFICATION:\t" + patent.getClassification() + "\n");
				writer.flush();
				//System.out.println("CLASSIFICATION:\t" + patent.getClassification());
				break;
			case "object":
				writer.write(patent.toString());
				writer.flush();
				break;
			case "family":
				writer.write("FAMILY:\t\n");
				//System.out.println("FAMILY:\t");
				for (DocumentId docId : patent.getRelationIds()) {
					//System.out.println("\t" + docId.getDocIdType().name() + " : " + docId.toText() );
					writer.write("\t" + docId.getType().name() + " : " + docId.toText() + "\n");
				}
				writer.flush();
				break;
			}
		}
	}

	/**
	 * Pretty Print XML
	 * 
	 * @param xmlDocStr
	 */
	private String prettyFormatXml(String xmlDocStr) {
		String result = null;
		try {

			org.dom4j.Document doc = DocumentHelper.parseText(xmlDocStr);
			StringWriter sw = new StringWriter();
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter xw = new XMLWriter(sw, format);
			xw.write(doc);
			xw.close();
			result = sw.toString();
			System.out.println(result);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) throws PatentReaderException, IOException {
		System.out.println("--- Start ---");

		OptionParser parser = new OptionParser() {
			{
				accepts("source").withRequiredArg().ofType(String.class).describedAs("zip file").required();
				accepts("fields").withOptionalArg().ofType(String.class)
						.describedAs(
								"comma seperated list of fields; options: [xml,object,id,title,abstract,description,citations,claims,assignee,inventor,classsification,family]")
						.defaultsTo("object");
				accepts("num").withOptionalArg().ofType(Integer.class).describedAs("Record Number to retrive");
				accepts("id").withOptionalArg().ofType(String.class).describedAs("Patent Id");
				accepts("limit").withOptionalArg().ofType(Integer.class).describedAs("record limit").defaultsTo(1);
				accepts("skip").withOptionalArg().ofType(Integer.class).describedAs("records to skip").defaultsTo(0);
				accepts("out").withOptionalArg().ofType(String.class).describedAs("out file");
				accepts("xmlBodyTag").withOptionalArg().ofType(String.class)
						.describedAs("XML Body Tag which wrapps document: [us-patent, PATDOC, patent-application]")
						.defaultsTo("us-patent");
				accepts("out").withOptionalArg().ofType(String.class).describedAs("out file");
				accepts("addHtmlEntities").withOptionalArg().ofType(Boolean.class).describedAs("Add Html Entities DTD to XML; Needed when reading Patents in PAP format.").defaultsTo(false);
				accepts("aps").withOptionalArg().ofType(Boolean.class).describedAs("Read APS - Greenbook Patent Document Format").defaultsTo(false);
			}
		};

		OptionSet options = parser.parse(args);
		String inFileStr = (String) options.valueOf("source");
		File inputFile = new File(inFileStr);

		int skip = (Integer) options.valueOf("skip");
		int limit = (Integer) options.valueOf("limit");
		String xmlBodyTag = (String) options.valueOf("xmlBodyTag");
		boolean addHtmlEntities = (Boolean) options.valueOf("addHtmlEntities");
		boolean aps = (Boolean) options.valueOf("aps");

		if (options.has("num")) {
			skip = ((Integer) options.valueOf("num")) - 1;
			limit = 1;
		}

		String[] fields = ((String) options.valueOf("fields")).split(",");
		Look look = new Look();

		DumpReader dumpReader;
		if (!aps){
			DumpFileXml dumpXml = new DumpFileXml(inputFile);
			if (addHtmlEntities){
				dumpXml.addHTMLEntities();
			}
			dumpReader = dumpXml;
		} else {
			dumpReader = new DumpFileAps(inputFile);
		}

		FileFilter filter = new FileFilter();
		//filter.addRule(new SuffixRule("xml"));
		//filter.addRule(new SuffixRule("txt"));
		dumpReader.setFileFilter(filter);

		dumpReader.open();
		dumpReader.skip(skip);

		Writer writer = null;
		if (options.has("out")) {
			String outStr = (String) options.valueOf("out");
			Path outFilePath = Paths.get(outStr);
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outFilePath.toFile()), Charset.forName("UTF-8")));
		} else {
			writer = new BufferedWriter(new OutputStreamWriter(System.out, Charset.forName("UTF-8")));
		}

		try {
			if (options.has("id")) {
				String docid = (String) options.valueOf("id");
				look.look(dumpReader, docid, writer, fields);
			} else {
				look.look(dumpReader, limit, writer, fields);
			}
		} finally {
			dumpReader.close();
			writer.close();
		}

		System.out.println("--- Finished ---");
	}

}
