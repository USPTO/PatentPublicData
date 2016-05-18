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

import gov.uspto.bulkdata.DumpXmlReader;
import gov.uspto.patent.PatentParserException;
import gov.uspto.patent.PatentXmlParser;
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

	public void look(File inputFile, int skip, int limit, String[] fields, Writer writer)
			throws PatentParserException, IOException {

		DumpXmlReader dxml = new DumpXmlReader(inputFile, "us-patent");
		dxml.open();
		dxml.skip(skip);

		PatentXmlParser patentParser = new PatentXmlParser();

		for (int i = 1; dxml.hasNext() && i <= limit; i++) {
			System.out.println(dxml.getCurrentRecCount() + 1 + " --------------------------");

			try {
				xmlDocStr = dxml.next();
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

			Patent patent = patentParser.parse(xmlDocStr);
			show(patent, fields, writer);

		}

		dxml.close();
	}

	public void look(File inputFile, String docId, String[] fields, Writer writer)
			throws PatentParserException, IOException {
		DumpXmlReader dxml = new DumpXmlReader(inputFile, "us-patent");
		dxml.open();

		while (dxml.hasNext()) {
			try {
				xmlDocStr = dxml.next();
			} catch (NoSuchElementException e) {
				break;
			}

			PatentXmlParser patentParser = new PatentXmlParser();
			Patent patent = patentParser.parse(xmlDocStr);
			if (patent.getDocumentId().toText().equals(docId)) {
				show(patent, fields, writer);
				break;
			}
		}

		dxml.close();
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
				System.out.println("Patent XML:\n");
				String prettyXml = prettyFormatXml(xmlDocStr);
				writer.write(prettyXml + "\n");
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
			case "classification":
				writer.write("CLASSIFICATION:\t" + patent.getClassification() + "\n");
				writer.flush();
				//System.out.println("CLASSIFICATION:\t" + patent.getClassification());
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

	public static void main(String[] args) throws PatentParserException, IOException {
		System.out.println("--- Start ---");

		OptionParser parser = new OptionParser() {
			{
				accepts("source").withRequiredArg().ofType(String.class).describedAs("zip file").required();
				accepts("fields").withOptionalArg().ofType(String.class).describedAs("comma seperated list of fields")
						.required();
				accepts("num").withOptionalArg().ofType(Integer.class).describedAs("Record Number to retrive");
				accepts("id").withOptionalArg().ofType(String.class).describedAs("Patent Id");
				accepts("limit").withOptionalArg().ofType(Integer.class).describedAs("record limit");
				accepts("skip").withOptionalArg().ofType(Integer.class).describedAs("records to skip");
				accepts("out").withOptionalArg().ofType(String.class).describedAs("out file");
			}
		};

		OptionSet options = parser.parse(args);
		String inFileStr = (String) options.valueOf("source");
		File inputFile = new File(inFileStr);

		int skip = 0;
		if (options.has("skip")) {
			skip = (Integer) options.valueOf("skip");
		}

		int limit = 1;
		if (options.has("limit")) {
			limit = (Integer) options.valueOf("limit");
		}

		if (options.has("num")) {
			skip = ((Integer) options.valueOf("num")) - 1;
			limit = 1;
		}

		String[] fields = ((String) options.valueOf("fields")).split(",");
		Look look = new Look();

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
				look.look(inputFile, docid, fields, writer);
			} else {
				look.look(inputFile, skip, limit, fields, writer);
			}
		} finally {
			writer.close();
		}

		System.out.println("--- Finished ---");
	}

}
