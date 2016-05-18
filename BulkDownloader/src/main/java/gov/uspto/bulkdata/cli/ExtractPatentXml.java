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

import gov.uspto.bulkdata.DumpXmlReader;
import gov.uspto.patent.PatentParserException;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.PatentXmlParser;
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
public class ExtractPatentXml {

	public void run(File inputFile, int skip, int limit, Path outdir) throws IOException, PatentParserException, DocumentException{
		Preconditions.checkArgument(Files.isDirectory(outdir, LinkOption.NOFOLLOW_LINKS),
				"Output directory does not exist: " + outdir);

		DumpXmlReader dxml = new DumpXmlReader(inputFile, "us-patent");
		dxml.open();
		dxml.skip(skip);

		PatentXmlParser patentParser = new PatentXmlParser();
		
		for (int i = 1; dxml.hasNext() && i <= limit; i++) {
			System.out.println(dxml.getCurrentRecCount() + 1 + " --------------------------");

			String xmlDocStr;
			try {
				xmlDocStr = dxml.next();
			} catch (NoSuchElementException e) {
				break;
			}

			Patent patent = patentParser.parse(xmlDocStr);

			write(xmlDocStr, outdir, patent.getDocumentId().toText() + ".xml");

			//LOGGER.info(xmlDocStr);

			/*
			 Match matcher = new MatchValueRegex("//classification-national/main-classification", "^166.+");
			 if (matcher.match(xmlDocStr){
				LOGGER.info("found matching document.");
			 }
			*/

		}

		dxml.close();
	}

	public void run(File inputFile, String docId, Path outdir) throws IOException, PatentParserException, DocumentException{
		Preconditions.checkArgument(Files.isDirectory(outdir, LinkOption.NOFOLLOW_LINKS),
				"Output directory does not exist: " + outdir);

		DumpXmlReader dxml = new DumpXmlReader(inputFile, "us-patent");
		dxml.open();

		while (dxml.hasNext()) {

			String xmlDocStr;
			try {
				xmlDocStr = dxml.next();
			} catch (NoSuchElementException e) {
				break;
			}

			PatentXmlParser patentParser = new PatentXmlParser();
			Patent patent = patentParser.parse(xmlDocStr);

			if (patent.getDocumentId().toText().equals(docId)) {
				System.out.println("--- Found --- ");
				write(xmlDocStr, outdir, patent.getDocumentId().toText() + ".xml");
				break;
			}

		}

		dxml.close();
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

	public static void main(String[] args) throws IOException, PatentParserException, DocumentException{
		System.out.println("--- Start ---");

		OptionParser parser = new OptionParser() {
			{
				accepts("source").withRequiredArg().ofType(String.class).describedAs("zip file").required();
				accepts("outdir").withRequiredArg().ofType(String.class).describedAs("Output directory").required();
				accepts("num").withOptionalArg().ofType(Integer.class).describedAs("Record Number to retrive");
				accepts("id").withOptionalArg().ofType(String.class).describedAs("Patent Id");
				accepts("limit").withOptionalArg().ofType(Integer.class).describedAs("record limit");
				accepts("skip").withOptionalArg().ofType(Integer.class).describedAs("records to skip");
			}
		};

		OptionSet options = parser.parse(args);
		String inFileStr = (String) options.valueOf("source");
		File inputFile = new File(inFileStr);
		String outdirStr = (String) options.valueOf("outdir");
		Path outdir = Paths.get(outdirStr);

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

		ExtractPatentXml extract = new ExtractPatentXml();

		if (options.has("id")) {
			String docid = (String) options.valueOf("id");
			extract.run(inputFile, docid, outdir);
		} else {
			extract.run(inputFile, skip, limit, outdir);
		}

		System.out.println("--- Finished ---");

	}

}