package gov.uspto.bulkdata.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.google.common.base.Preconditions;

import gov.uspto.bulkdata.DumpFileAps;
import gov.uspto.bulkdata.DumpFileXml;
import gov.uspto.bulkdata.DumpReader;
import gov.uspto.common.file.filter.FileFilterChain;
import gov.uspto.common.file.filter.SuffixFileFilter;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Extract individual patents from Patent Dump file to output directory.
 * 
 * Example CLI Usage:
 * 	--source="download/ipa150101.zip" --limit 5 --skip 0 --outDir="download"
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ExtractPatent {

	public void run(DumpReader dumpReader, int limit, Path outdir) throws IOException, PatentReaderException, DocumentException{
		Preconditions.checkArgument(Files.isDirectory(outdir, LinkOption.NOFOLLOW_LINKS),
				"Output directory does not exist: " + outdir);

		for (int i = 1; dumpReader.hasNext() && i <= limit; i++) {
			System.out.println(dumpReader.getCurrentRecCount() + 1 + " --------------------------");

			String xmlDocStr;
			try {
				xmlDocStr = dumpReader.next();
			} catch (NoSuchElementException e) {
				break;
			}

			try(PatentReader patentReader = new PatentReader(xmlDocStr, dumpReader.getPatentType())){
				Patent patent = patentReader.read();
				write(xmlDocStr, outdir, patent.getDocumentId().toText() + ".xml");
			}

			//LOGGER.info(xmlDocStr);

			/*
			 Match matcher = new MatchValueRegex("//classification-national/main-classification", "^166.+");
			 if (matcher.match(xmlDocStr){
				LOGGER.info("found matching document.");
			 }
			*/

		}

		dumpReader.close();
	}

	public void run(DumpReader dumpReader, String docId, Path outdir) throws IOException, PatentReaderException, DocumentException{
		Preconditions.checkArgument(Files.isDirectory(outdir, LinkOption.NOFOLLOW_LINKS),
				"Output directory does not exist: " + outdir);

		while (dumpReader.hasNext()) {

			String xmlDocStr;
			try {
				xmlDocStr = dumpReader.next();
			} catch (NoSuchElementException e) {
				break;
			}

			try(PatentReader patentReader = new PatentReader(xmlDocStr, dumpReader.getPatentType())){
				Patent patent = patentReader.read();
				if (patent.getDocumentId().toText().equals(docId)) {
					System.out.println("--- Found --- ");
					write(xmlDocStr, outdir, patent.getDocumentId().toText() + ".xml");
					break;
				}
			}
		}

		dumpReader.close();
	}

	private void write(String xmlDocStr, Path directory, String filename) throws DocumentException, IOException {
		Path outPath = directory.resolve(filename);

		org.dom4j.Document doc = DocumentHelper.parseText(xmlDocStr);
		Writer outfile = new FileWriter(outPath.toFile());

		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter xw = new XMLWriter(outfile, format);
		xw.write(doc);
		xw.close();
	}

	public static void main(String[] args) throws IOException, PatentReaderException, DocumentException{
		System.out.println("--- Start ---");

		OptionParser parser = new OptionParser() {
			{
				accepts("source").withRequiredArg().ofType(String.class).describedAs("zip file").required();
				accepts("outdir").withRequiredArg().ofType(String.class).describedAs("Output directory").required();
				accepts("num").withOptionalArg().ofType(Integer.class).describedAs("Record Number to retrive");
				accepts("id").withOptionalArg().ofType(String.class).describedAs("Patent Id");
				accepts("limit").withOptionalArg().ofType(Integer.class).describedAs("record limit").defaultsTo(1);
				accepts("skip").withOptionalArg().ofType(Integer.class).describedAs("records to skip").defaultsTo(0);
				accepts("xmlBodyTag").withOptionalArg().ofType(String.class).describedAs("XML Body Tag which wrapps document: [us-patent, PATDOC, patent-application-publication]").defaultsTo("us-patent");
				accepts("addHtmlEntities").withOptionalArg().ofType(Boolean.class).describedAs("Add Html Entities DTD to XML; Needed when reading Patents in PAP format.").defaultsTo(false);
				accepts("aps").withOptionalArg().ofType(Boolean.class).describedAs("Read APS - Greenbook Patent Document Format").defaultsTo(false);				
			}
		};

		OptionSet options = parser.parse(args);
		String inFileStr = (String) options.valueOf("source");
		File inputFile = new File(inFileStr);
		String outdirStr = (String) options.valueOf("outdir");
		Path outdir = Paths.get(outdirStr);

		int skip = (Integer) options.valueOf("skip");
		int limit = (Integer) options.valueOf("limit");
		String xmlBodyTag = (String) options.valueOf("xmlBodyTag");
		boolean addHtmlEntities = (Boolean) options.valueOf("addHtmlEntities");
		boolean aps = (Boolean) options.valueOf("aps");

		if (options.has("num")) {
			skip = ((Integer) options.valueOf("num")) - 1;
			limit = 1;
		}

		ExtractPatent extract = new ExtractPatent();

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

		FileFilterChain filter = new FileFilterChain();
		//filter.addRule(new SuffixFileFilter("xml"));
		dumpReader.setFileFilter(filter);
		
		dumpReader.open();
		dumpReader.skip(skip);

		if (options.has("id")) {
			String docid = (String) options.valueOf("id");
			extract.run(dumpReader, docid, outdir);
		} else {
			extract.run(dumpReader, limit, outdir);
		}

		System.out.println("--- Finished ---");

	}

}